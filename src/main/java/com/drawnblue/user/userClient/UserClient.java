package com.drawnblue.user.userClient;

import com.drawnblue.entity.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(value = "user-center")
public interface UserClient {
    @RequestMapping(value = "/user-center/findByMobileOrEmail", method = RequestMethod.POST,consumes = "application/json")
    public User findByMobileOrEmail(@RequestBody User user);
//这里的路径一定得要写对，另一个服务类上面的路径一定要加上
    @GetMapping("/user-center/findById/{id}")
    public User findById(@PathVariable Long id);
}
