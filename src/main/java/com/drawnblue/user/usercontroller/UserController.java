package com.drawnblue.user.usercontroller;

import com.drawnblue.entity.User;
import com.drawnblue.user.userClient.UserClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
public class UserController {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private UserClient userClient;

    @GetMapping("/test/{id}")
    public User findById(@PathVariable Long id) {
        System.out.println("調用kaishi");
        ResponseEntity<User> body = this.restTemplate.getForEntity("http://user-service/userService/findById/" + id, User.class);
        System.out.println("調用完成");
        return body.getBody();
    }

    @GetMapping("/getInstances")
    public List<ServiceInstance> getInstances() {
        return this.discoveryClient.getInstances("user-service");
    }

    @GetMapping("/test1/{id}")
    public User findById2(@PathVariable Long id) {
        List<ServiceInstance> instances = discoveryClient.getInstances("user-service");
        System.out.println("======================" + instances);
        String targetURL = instances.stream()
                .map(instance -> instance.getUri().toString() + "/userService/findById/{id}")
                .findFirst().orElseThrow(() -> new IllegalArgumentException("当前没有实例"));

        System.out.println(targetURL + "------targetUrl" + "    " + this.restTemplate);
        ResponseEntity<User> body = this.restTemplate.getForEntity(targetURL, User.class, 1);
        return body.getBody();
    }

    @GetMapping("/testFeign/{id}")
    public User testFeign(@PathVariable Long id) {
        logger.info("id: {}", id);
        return this.userClient.findById(id);
    }
}
