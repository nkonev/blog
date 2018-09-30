package com.github.nkonev.blog.test.deploy;

import com.github.nkonev.blog.FailoverUtils;
import com.github.nkonev.blog.util.FileUtils;
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

import static com.github.nkonev.blog.test.utils.ProcessUtils.*;

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


    @Parameters({"testStack", "timeoutToStartSec", "baseUrl", "inContainerBlogUrl", "lbCheckTimes", "hostHeader"})
    @BeforeSuite
    public void startup(String testStack, int timeoutToStartSec, String baseUrl, String inContainerBlogUrl, int lbCheckTimes, String hostHeader) throws IOException, InterruptedException {
        checkImageExistence();
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

        waitForStart(timeoutToStartSec, lbCheckTimes, hostHeader);
    }

    private void checkImageExistence() throws IOException {
        ProcessInfo processInfo = get(launch("docker images -q --filter reference=nkonev/blog:current-test"));
        Assert.assertNotNull(processInfo.stdout, "Docker image not found");
        Assert.assertFalse(processInfo.stdout.isEmpty(), "Docker image not found");
    }

    private void dropVolumes(String stackName) throws InterruptedException, IOException {
        launch("docker volume rm "+stackName+"_redis_prod_data_dir", processBuilder -> {}).waitFor();
        launch("docker volume rm "+stackName+"_rabbitmq_prod_data_dir", processBuilder -> {}).waitFor();
        launch("docker volume rm "+stackName+"_postgresql_prod_data_dir", processBuilder -> {}).waitFor();
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
            Process swarmInit = launch("docker swarm init", b -> { });
            Assert.assertEquals(swarmInit.waitFor(), 0);

            final String nodeId = getSelfNodeId();
            Process addLabel = launch("docker node update --label-add blog.server.role=db " + nodeId, b -> { });
            Assert.assertEquals(addLabel.waitFor(), 0);

            Process addNetwork = launch("docker network create --driver=overlay proxy_backend", b -> { });
            Assert.assertEquals(addNetwork.waitFor(), 0);
        }
    }

    @Test
    @Parameters({"hostHeader"})
    public void testVersionGit(String hostHeader) throws IOException, InterruptedException {

        final Request request = new Request.Builder()
                .url(baseUrl+"/git.json")
                .header("Host", hostHeader)
                .build();

        final Response response = client.newCall(request).execute();
        final String json = response.body().string();
        LOGGER.info("Git version response: {}", json);

        ReadContext ctx = JsonPath.parse(json);
        String version = ctx.read("$.['build.version']", String.class);

        Assert.assertNotNull(version);
        Assert.assertFalse(version.isEmpty());
    }

    @Test
    @Parameters({"hostHeader"})
    public void testDocumentationIsPresent(String hostHeader) throws Exception {
        final Request request = new Request.Builder()
                .header("Host", hostHeader)
                .url(baseUrl+"/docs/index.html")
                .build();

        final Response response = client.newCall(request).execute();
        final String html = response.body().string();
        LOGGER.debug("docs html response: {}", html);
        Assert.assertTrue(html.contains("Blog API Reference"));
    }


    @Parameters({"testStack", "hostHeader"})
    @Test
    public void testPrerenderWorks(String testStack, String hostHeader) throws IOException, InterruptedException {
        FailoverUtils.retry(480, () -> {
            try {
                clearPrerenderRedisCache(testStack);

                final Request request = new Request.Builder()
                        .url(baseUrl)
                        .header("User-Agent", "googlebot")
                        .header("Accept", "text/html")
                        .header("X-FORWARDED-URL", inContainerBlogUrl) // url on which prerender container can invoke blog container. Header name must be set in blog application
                        .header("Host", hostHeader)
                        .build();

                final Response response = client.newCall(request).execute();
                final String html = response.body().string();
                LOGGER.info("Response for crawler: {}", html.substring(0, Math.min(600, html.length())));
                Assert.assertTrue(html.contains("<body>"));
                Assert.assertTrue(html.contains("Lorem Ipsum - это текст"));
                return null;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }  catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        });

        final Request request = new Request.Builder()
                .url(baseUrl)
                .header("Host", hostHeader)
                .build();

        try {
            final Response response = client.newCall(request).execute();
            final String html = response.body().string();
            LOGGER.info("Response for human: {}", html);
            Assert.assertTrue(html.contains("<body>"));
            Assert.assertFalse(html.contains("Lorem Ipsum - это текст"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    @Parameters({"hostHeader"})
    @Test
    public void testMetricsAreHidden(String hostHeader) {
        final Request request = new Request.Builder()
                .url(baseUrl + "/metrics")
                .header("Host", hostHeader)
                .build();

        try {
            final Response response = client.newCall(request).execute();
            final String html = response.body().string();
            LOGGER.info("Metrics response: {}", html);
            Assert.assertEquals(response.code(), 200);
            Assert.assertTrue(html.contains("<script type=\"application/javascript\" src=\"/build/main.js\">"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    public void testMetricsAreHiddenWithoutHost() {

        final Request request = new Request.Builder()
                .url(baseUrl + "/metrics")
                .build();

        try {
            final Response response = client.newCall(request).execute();
            final String html = response.body().string();
            LOGGER.info("Metrics response: {}", html);
            Assert.assertEquals(response.code(), 404);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }



    private void clearPrerenderRedisCache(String testStack) throws InterruptedException, IOException {
        LOGGER.info("Cleaning prerender redis cache");
        // clean redis cache
        launch("docker exec "+getPrerenderRedisContainerId(getRedisTaskId(testStack))+" redis-cli flushdb", processBuilder -> {}).waitFor();
    }

    private String getRedisTaskId(String testStack) throws IOException, InterruptedException {
        String[] lines = get(launch("docker service ps "+testStack+"_redis -q", c -> {}, false)).stdout.split("\\r?\\n");
        Assert.assertEquals(lines.length, 1, "Expected one task for prerender");
        return lines[0];
    }

    private String getPrerenderRedisContainerId(String taskId) throws IOException, InterruptedException {
        String[] lines = get(launch("docker inspect --format {{.Status.ContainerStatus.ContainerID}} "+taskId, c -> {}, false)).stdout.split("\\r?\\n");
        Assert.assertEquals(lines.length, 1, "Expected one container for prerender");
        return lines[0];
    }


    private void waitForStart(int timeoutToStartSec, int lbCheckTimes, String hostHeader) {
        LOGGER.info("Start waiting for start");
        FailoverUtils.retry(timeoutToStartSec, () -> {
            try {
                for (int i = 0; i < lbCheckTimes; ++i) {
                    final Request request = new Request.Builder()
                            .url(baseUrl + "/api/post?size=1")
                            .header("Host", hostHeader)
                            .build();
                    LOGGER.info("Requesting " + (i + 1) + "/" + lbCheckTimes + " " + request.toString());
                    final Response response = client.newCall(request).execute();
                    final String version = response.body().string();
                    Assert.assertEquals(200, response.code());
                    LOGGER.info("Successful get posts: \n" + version);
                }
                return null;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, 10);
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
        // Assert.assertEquals(rmStack.waitFor(), 0);
    }
}
