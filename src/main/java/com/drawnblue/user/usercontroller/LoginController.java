package com.drawnblue.user.usercontroller;

import com.alibaba.fastjson.JSONObject;
import com.drawnblue.common.Constant;
import com.drawnblue.common.ResponseObj;
import com.drawnblue.entity.User;
import com.drawnblue.user.userClient.UserClient;
import com.drawnblue.util.StringUtil;
import com.drawnblue.util.VerifyCodeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
public class LoginController {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    public RedisTemplate<String, String> redisTemplate;
    @Autowired
    private UserClient userClient;

    @GetMapping("verifyCode")
    public String verifyCode() {
        String veritfyCode = VerifyCodeUtil.getCode(4);
        redisTemplate.opsForValue().set(Constant.VERIFY_CODE_PREFIX, veritfyCode, 30, TimeUnit.MINUTES);
        logger.info("verifyCode: {}",veritfyCode);
        return veritfyCode;
    }

    @PostMapping("/login")
    public ResponseObj login(@RequestBody JSONObject obj) {
        logger.info("login param: {}", obj);
        ResponseObj response = new ResponseObj();
        String userName = obj.getString("userName");
        String password = obj.getString("password");
        String verifyCode = obj.getString("verifyCode");
        logger.info("{} {} {}",StringUtil.isEmpty(userName) , StringUtil.isEmpty(password) , StringUtil.isEmpty(verifyCode));
        if (StringUtil.isEmpty(userName) || StringUtil.isEmpty(password) || StringUtil.isEmpty(verifyCode)) {
            return new ResponseObj(1, "缺失必填参数", null);
        }
//      校验redis中的verifyCode
        String redis_verifyCode = redisTemplate.opsForValue().get(Constant.VERIFY_CODE_PREFIX);
        logger.info("redis_verifyCode: {}",redis_verifyCode);
        if (redis_verifyCode == null || !redis_verifyCode.equals(verifyCode)) {
            return new ResponseObj(1, "验证码不正确", null);
        }
        User user = new User();
        user.setPassword(password);
        if (StringUtil.checkMobileNumber(userName)) {
            logger.info("判断手机号");
            user.setMobile(userName);
        } else if (StringUtil.checkEmail(userName)) {
            logger.info("判断是否为邮箱");
            user.setMobile(userName);
        } else {
            return new ResponseObj(1, "手机号或邮箱格式不正确！", null);
        }
        User u = userClient.findByMobileOrEmail(user);
        if(u == null){
            return new ResponseObj(1,"该用户未注册！",null);
        }
        if(!u.getPassword().equals(password)){
            return new ResponseObj(1,"密码错误！",null);
        }
        String token = StringUtil.getUUID();
        Map<String, String> data = new HashMap<>(1);
        data.put("token", token);
        response.setData(data);
        return response;
    }
}
