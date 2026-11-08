package com.btr.platform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@MapperScan("com.btr.platform.mapper")
public class BtrApplication {

    public static void main(String[] args) {
        SpringApplication.run(BtrApplication.class, args);
    }
}
