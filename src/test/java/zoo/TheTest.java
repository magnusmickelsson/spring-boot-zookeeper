package zoo;

import org.apache.curator.test.TestingServer;
import org.junit.Test;
import org.springframework.boot.Banner;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.SocketUtils;

import static org.junit.Assert.assertEquals;

public class TheTest {
    @Test
    public void aTest() throws Exception {
        int zkPort = SocketUtils.findAvailableTcpPort();
        int httpPort = SocketUtils.findAvailableTcpPort();
        Application.zkPort = String.valueOf(zkPort);
        TestingServer server = new TestingServer(zkPort);
        server.start();

        ConfigurableApplicationContext context = new SpringApplicationBuilder(Application.class)
                .bannerMode(Banner.Mode.OFF).build().run(
            "--server.port=" + httpPort,
            "--management.endpoints.web.exposure.include=*",
            "--spring.cloud.zookeeper.connect-string=localhost:" + zkPort,
            "--spring.profiles.active=zoo");
        DiscoveryClient discoveryClient = context.getBean(DiscoveryClient.class);

        ResponseEntity<String> response = new TestRestTemplate().getForEntity("http://localhost:" + httpPort + "/ping", String.class);
        assertEquals(response.getStatusCode(), HttpStatus.OK);

        response = new TestRestTemplate().getForEntity("http://localhost:" + httpPort + "/zoo", String.class);
        assertEquals(response.getStatusCode(), HttpStatus.OK);

        assertEquals(discoveryClient.getServices().size(), 0);
        context.close();
        server.close();
    }
}
