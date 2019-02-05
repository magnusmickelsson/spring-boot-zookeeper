package zoo;

import org.apache.curator.test.TestingServer;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationAutoConfiguration;
import org.springframework.cloud.client.serviceregistry.ServiceRegistryAutoConfiguration;
import org.springframework.cloud.zookeeper.ZookeeperAutoConfiguration;
import org.springframework.cloud.zookeeper.discovery.ZookeeperDiscoveryAutoConfiguration;
import org.springframework.cloud.zookeeper.serviceregistry.ZookeeperAutoServiceRegistrationAutoConfiguration;
import org.springframework.cloud.zookeeper.serviceregistry.ZookeeperServiceRegistryAutoConfiguration;
import org.springframework.cloud.zookeeper.support.CuratorServiceDiscoveryAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.util.SocketUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

@EnableAutoConfiguration(exclude = {
        ZookeeperAutoConfiguration.class,
        ZookeeperAutoServiceRegistrationAutoConfiguration.class,
        ZookeeperServiceRegistryAutoConfiguration.class,
        ServiceRegistryAutoConfiguration.class,
        ZookeeperServiceRegistryAutoConfiguration.class,
        AutoServiceRegistrationAutoConfiguration.class,
        ZookeeperDiscoveryAutoConfiguration.class,
        CuratorServiceDiscoveryAutoConfiguration.class
})
@SpringBootApplication(scanBasePackages = {"zoo.app"})
@Import({ZooConfig.class})
public class Application {

    public static String zkPort;
    private static TestingServer server;

    public static void main(String[] args) {
        System.out.println("*** Starting... ***");
        int zkPort = SocketUtils.findAvailableTcpPort();
        Application.zkPort = String.valueOf(zkPort);
        try {
            server = new TestingServer(zkPort);
            ZooKeeper zk = new ZKConnection().connect(server.getConnectString());
            System.out.println("ZK connected.");
            zk.create("/services", "Helloworld".getBytes("UTF-8"), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            zk.create("/services/application", "Helloworld".getBytes("UTF-8"), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            System.out.println("Topic created!");
            zk.close();
            System.out.println("*** Starting context/application ***");
            ConfigurableApplicationContext context = new SpringApplicationBuilder(Application.class)
                    .bannerMode(Banner.Mode.OFF).build()
                    .run("--management.endpoints.web.exposure.include=*",
                    "--spring.cloud.zookeeper.connect-string=localhost:" + zkPort,
                    "--spring.profiles.active=zoo");
            System.out.println("Started context: " + context.getApplicationName() + " " + context.getId());
        } catch (Exception e) {
            System.err.println(e);
            System.exit(-1);
        }
    }

    @PreDestroy
    public void shutdown() {
        try {
            System.out.println("*** Shutdown Zookeeper ***");
            server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public static class ZKConnection {
        private ZooKeeper zoo;
        CountDownLatch connectionLatch = new CountDownLatch(1);

        ZooKeeper connect(String host)
                throws IOException,
                InterruptedException {
            zoo = new ZooKeeper(host, 2000, we -> {
                if (we.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    connectionLatch.countDown();
                }
            });

            connectionLatch.await();
            return zoo;
        }
    }
}
