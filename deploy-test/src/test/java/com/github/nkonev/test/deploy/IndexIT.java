package com.github.nkonev.test.deploy;

import com.github.nkonev.util.FileUtils;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import java.io.*;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class IndexIT {

    final static OkHttpClient client = new OkHttpClient();

    private static final Logger LOGGER = LoggerFactory.getLogger(IndexIT.class);

    private String[] split(String s) {
        return s.split("\\s+");
    }

    private String baseUrl;
    private String inContainerBlogUrl;

    private Process launch(String line, Consumer<ProcessBuilder> builderCustomize) throws IOException {
        final ProcessBuilder processBuilder = new ProcessBuilder();
        final String[] splitted = split(line);
        LOGGER.debug("Will run {}", Arrays.toString(splitted));
        processBuilder.command(splitted);
        processBuilder.inheritIO();
        builderCustomize.accept(processBuilder);
        return processBuilder.start();
    }

    @Parameters({"testStack", "timeoutToStartSec", "baseUrl", "inContainerBlogUrl"})
    @BeforeSuite
    public void startup(String testStack, int timeoutToStartSec, String baseUrl, String inContainerBlogUrl) throws IOException, InterruptedException {
        // initilize swarm if need
        initializeSwarmIfNeed();

        // deploy stack
        deployStack(testStack);

        log("docker service logs -f "+testStack+"_blog", "stack-logs", false);
        //log("docker service ls", "services-ls-logs", true);
        //log("docker node inspect " + getSelfNodeId(), "node-inspect-logs", true);
        //log("docker inspect "+testStack+"__postgresql", "postgres-container-inspect-logs", true);
        //log("docker service inspect "+testStack+"_postgresql", "postgres-service-inspect-logs", true);

        this.baseUrl = baseUrl;
        this.inContainerBlogUrl = inContainerBlogUrl;

        waitForStart(timeoutToStartSec);
    }

    private void log(final String command, final String loggerName, final boolean cycle) {
        final Logger LOGGER = LoggerFactory.getLogger(loggerName);
        final int processPollIntervalSeconds = 1;
        final int processRecreateIntervalSeconds = 1;
        final Thread t = new Thread(() -> {
            if (cycle) {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        final ProcessBuilder processBuilder = new ProcessBuilder();
                        processBuilder.command(split(command));
                        LOGGER.debug("Starting cycling logger process");
                        final Process logs = processBuilder.start();
                        LOGGER.debug("Started cycling logger process");
                        final InputStream is = logs.getInputStream();
                        final InputStream es = logs.getErrorStream();

                        try {
                            logs.waitFor();
                            LOGGER.info("Stdout: \n" + readAndClose(is));
                            LOGGER.info("Stderr: \n" + readAndClose(es));
                        } catch (IOException e) {
                            LOGGER.error("Error on get logs", e);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }

                        try {
                            TimeUnit.SECONDS.sleep(processRecreateIntervalSeconds);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    } catch (IOException e) {
                        LOGGER.error("Error on get logs", e);
                    }
                }
            } else {
                try {
                    final ProcessBuilder processBuilder = new ProcessBuilder();
                    processBuilder.command(split(command));
                    LOGGER.debug("Starting long logger process");
                    final Process logs = processBuilder.start();
                    LOGGER.debug("Started long logger process");
                    final InputStream is = logs.getInputStream();

                    try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is))) {
                        while (!Thread.currentThread().isInterrupted() && logs.isAlive()) {
                            if (is.available() > 0) {
                                LOGGER.info(bufferedReader.readLine());
                            }
                            try {
                                TimeUnit.SECONDS.sleep(processPollIntervalSeconds);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }
                        LOGGER.debug("Logger is interrupted");
                    }
                } catch (IOException e) {
                    LOGGER.error("Error on get logs", e);
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void deployStack(String testStack) throws IOException, InterruptedException {
        final ProcessBuilder processBuilderDockerDeploy = new ProcessBuilder();
        final File dockerDirectory = FileUtils.getExistsFile("../docker", "docker");
        processBuilderDockerDeploy
                .command(split("docker stack deploy --compose-file docker-compose.template.yml " + testStack))
                .directory(dockerDirectory)
                .inheritIO();

        final Process process = processBuilderDockerDeploy.start();
        Assert.assertEquals(process.waitFor(), 0);
    }

    private String readAndClose(final InputStream is) throws IOException {
        final String s;
        try(BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is))) {
            s = bufferedReader.lines().collect(Collectors.joining("\n"));
        }
        return s;
    }

    private String getSelfNodeId() throws IOException, InterruptedException {
        final ProcessBuilder processBuilderN = new ProcessBuilder();
        processBuilderN.command(split("docker node ls -q"));
        Process getNodeId = processBuilderN.start();

        final InputStream is = getNodeId.getInputStream();
        Assert.assertEquals(getNodeId.waitFor(), 0);
        final String nodeId = readAndClose(is);
        return nodeId;
    }

    private void initializeSwarmIfNeed() throws IOException, InterruptedException {
        final Process swarmChecker = launch("docker node ls", processBuilder -> {});
        final int checkerExit = swarmChecker.waitFor();
        if (checkerExit!=0) {
            Process swarmInit = launch("docker swarm init", processBuilder -> { });
            Assert.assertEquals(swarmInit.waitFor(), 0);

            final String nodeId = getSelfNodeId();
            Process addLabel = launch("docker node update --label-add blog.server.role=db " + nodeId, processBuilder -> { });
            Assert.assertEquals(addLabel.waitFor(), 0);
        }
    }

    @Test
    public void testVersionGit() throws IOException, InterruptedException {

        final Request request = new Request.Builder()
                .url(baseUrl+"/git.json")
                .build();

        final Response response = client.newCall(request).execute();
        final String json = response.body().string();

        ReadContext ctx = JsonPath.parse(json);
        String version = ctx.read("$.['build.version']", String.class);

        Assert.assertNotNull(version);
        Assert.assertFalse(version.isEmpty());
    }

    @Test
    public void testPrerenderWorks() throws IOException, InterruptedException {
        {
            final Request request = new Request.Builder()
                    .url(baseUrl)
                    .header("User-Agent", "googlebot")
                    .header("Accept", "text/html")
                    .header("X-FORWARDED-URL", inContainerBlogUrl) // url on which prerender container can invoke blog container. Header name must be set in blog application
                    .build();

            final Response response = client.newCall(request).execute();
            final String html = response.body().string();
            LOGGER.info("Response for crowler: {}", html);
            Assert.assertTrue(html.contains("<body>"));
            Assert.assertTrue(html.contains("Lorem Ipsum - это текст"));
        }

        {
            final Request request = new Request.Builder()
                    .url(baseUrl)
                    .build();

            final Response response = client.newCall(request).execute();
            final String html = response.body().string();
            LOGGER.info("Response for human: {}", html);
            Assert.assertTrue(html.contains("<body>"));
            Assert.assertFalse(html.contains("Lorem Ipsum - это текст"));
        }

    }


    private void waitForStart(int timeoutToStartSec) throws InterruptedException {
        final Request request = new Request.Builder()
                .url(baseUrl+"/api/post?size=1")
                .build();

        Throwable t;
        int i = 0;
        do {
            t = null;
            i++;

            try {
                final Response response = client.newCall(request).execute();
                final String version = response.body().string();
                LOGGER.info("Get posts: \n" + version);
            } catch (Throwable e){
                t = e;
                LOGGER.debug(e.getMessage() + " " + i + "/" + (timeoutToStartSec));
                TimeUnit.SECONDS.sleep(1);
            }
        } while (t!=null && i<timeoutToStartSec);
        if (t!=null && i == timeoutToStartSec) {
            throw new RuntimeException("Exceed " + timeoutToStartSec + " for start");
        }
    }

    @Parameters({"testStack"})
    @AfterSuite
    public void destroy(String testStack) throws IOException, InterruptedException {
        // undeploy
        undeployStack(testStack);

        // drop swarm
        dropSwarm();
    }

    private void dropSwarm() throws IOException, InterruptedException {
        Process swarmLeft = launch("docker swarm leave --force", processBuilder -> { });
        Assert.assertEquals(swarmLeft.waitFor(), 0);
    }

    private void undeployStack(String testStack) throws IOException, InterruptedException {
        Process rmStack = launch("docker stack rm " + testStack, processBuilder -> { });
        Assert.assertEquals(rmStack.waitFor(), 0);
    }
}
