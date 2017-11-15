package com.github.nkonev.test.deploy;

import com.github.nkonev.FailoverUtils;
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
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import static com.github.nkonev.test.utils.ProcessUtils.*;

public class DeployIT {

    private final static OkHttpClient client = new OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60 / 2, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .cache(null)
            .build();

    private static final Logger LOGGER = LoggerFactory.getLogger(DeployIT.class);

    private String baseUrl;
    private String inContainerBlogUrl;


    @Parameters({"testStack", "timeoutToStartSec", "baseUrl", "inContainerBlogUrl"})
    @BeforeSuite
    public void startup(String testStack, int timeoutToStartSec, String baseUrl, String inContainerBlogUrl) throws IOException, InterruptedException {
        // initilize swarm if need
        initializeSwarmIfNeed();

        dropVolumes(testStack);

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

    private void dropVolumes(String stackName) throws InterruptedException, IOException {
        launch("docker volume rm "+stackName+"_redis_prerender_data_dir", processBuilder -> {}).waitFor();
        launch("docker volume rm "+stackName+"_redis_data_dir", processBuilder -> {}).waitFor();
        launch("docker volume rm "+stackName+"_rabbitmq_data_dir", processBuilder -> {}).waitFor();
        launch("docker volume rm "+stackName+"_postgresql_data_dir", processBuilder -> {}).waitFor();
    }



    private void deployStack(String testStack) throws IOException, InterruptedException {
        final File dockerDirectory = FileUtils.getExistsFile("../docker", "docker");

        final Process process = launch(
                "docker stack deploy --compose-file docker-compose.template.yml " + testStack,
                processBuilder -> {
                    processBuilder.directory(dockerDirectory);
                }
        );
        Assert.assertEquals(process.waitFor(), 0);
    }

    private String getSelfNodeId() throws IOException, InterruptedException {
        return get(launch("docker node ls -q", c -> {}, false)).stdout;
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
            FailoverUtils.retry(120, () -> {
                try {
                    final Request request = new Request.Builder()
                            .url(baseUrl)
                            .header("User-Agent", "googlebot")
                            .header("Accept", "text/html")
                            .header("X-FORWARDED-URL", inContainerBlogUrl) // url on which prerender container can invoke blog container. Header name must be set in blog application
                            .build();

                    final Response response = client.newCall(request).execute();
                    final String html = response.body().string();
                    LOGGER.info("Response for crawler: {}", html.substring(0, Math.min(600, html.length())));
                    Assert.assertTrue(html.contains("<body>"));
                    Assert.assertTrue(html.contains("Lorem Ipsum - это текст"));
                    return null;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
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


    private void waitForStart(int timeoutToStartSec) {
        FailoverUtils.retry(timeoutToStartSec, () -> {
            try {
                final Request request = new Request.Builder()
                        .url(baseUrl + "/api/post?size=1")
                        .build();
                final Response response = client.newCall(request).execute();
                final String version = response.body().string();
                LOGGER.info("Get posts: \n" + version);
                return null;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
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
