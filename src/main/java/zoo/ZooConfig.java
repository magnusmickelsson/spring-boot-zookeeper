package zoo;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationAutoConfiguration;
import org.springframework.cloud.client.serviceregistry.ServiceRegistryAutoConfiguration;
import org.springframework.cloud.zookeeper.ZookeeperAutoConfiguration;
import org.springframework.cloud.zookeeper.discovery.ZookeeperDiscoveryAutoConfiguration;
import org.springframework.cloud.zookeeper.serviceregistry.ZookeeperAutoServiceRegistrationAutoConfiguration;
import org.springframework.cloud.zookeeper.serviceregistry.ZookeeperServiceRegistryAutoConfiguration;
import org.springframework.cloud.zookeeper.support.CuratorServiceDiscoveryAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("zoo")
@Import({
    ZookeeperAutoConfiguration.class,
    ZookeeperAutoServiceRegistrationAutoConfiguration.class,
    ZookeeperServiceRegistryAutoConfiguration.class,
    ServiceRegistryAutoConfiguration.class,
    ZookeeperServiceRegistryAutoConfiguration.class,
    AutoServiceRegistrationAutoConfiguration.class,
    ZookeeperDiscoveryAutoConfiguration.class,
    CuratorServiceDiscoveryAutoConfiguration.class
})
@EnableDiscoveryClient
public class ZooConfig {
}
