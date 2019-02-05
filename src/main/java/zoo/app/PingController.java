package zoo.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PingController {
    @Autowired
    private DiscoveryClient discoveryClient;

    @GetMapping("/ping")
    public String ping() {
        return "PONG";
    }

    @GetMapping("/zoo")
    public String zoo() {
        return "Services: " + discoveryClient.getServices();
    }
}
