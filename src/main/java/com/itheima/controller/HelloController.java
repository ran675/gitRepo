package com.itheima.controller;

import com.itheima.pojo.User;
import com.itheima.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.sql.DataSource;

//@RestController
@Controller
public class HelloController {

    @Autowired
    private DataSource dataSource;

    @Value("${itcast.url}")
    private String itcastUrl;

    @Value("${itheima.url}")
    private String itheimaUrl;

    @Autowired
    private UserService userService;

    private Logger logger = LoggerFactory.getLogger(HelloController.class);

    @RequestMapping("/home")
    public String showHome() {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        logger.info("当前登陆用户：" + name);

        return "home.html";
//        return "pages/main.html";
    }

    /**
     * 根据用户id查询用户
     *
     * @param id 用户id
     * @return 用户
     */
    @GetMapping("/user/{id}")
    public User queryById(@PathVariable Long id) {
        return userService.findById(id);
    }

    @GetMapping("/main")
    public String toMain() {
        return "pages/main.html";
    }

    @GetMapping("hello")
    public String hello() {
        System.out.println(" itcastUrl = " + itcastUrl);
        System.out.println(" itheimaUrl = " + itheimaUrl);
        System.out.println(" DataSource = " + dataSource);
        return "Hello, Spring Boot!";
    }

    @RequestMapping("/login")
    public String showLogin() {
        return "login.html";
    }

    @RequestMapping("/admin")
    @ResponseBody
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String printAdmin() {
        return "如果你看见这句话，说明你有ROLE_ADMIN角色";
    }

    @RequestMapping("/user")
    @ResponseBody
    @PreAuthorize("hasRole('ROLE_HEALTH_MANAGER')")
    public String printUser() {
        return "如果你看见这句话，ROLE_HEALTH_MANAGER";
    }

}
