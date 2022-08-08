package com.itheima;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * spring boot工程都有一个启动引导类，这是工程的入口类
 * 并在引导类上添加@SpringBootApplication
 *
 * @author v-rr
 * // 扫描mybatis所有的业务mapper接口
 */
@SpringBootApplication
@MapperScan("com.itheima.mapper")
public class GitRepoApplication {
    public static void main(String[] args) {
        SpringApplication.run(GitRepoApplication.class, args);
    }

}
