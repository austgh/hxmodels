package com.ahzx.mdfc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DispatcherApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(DispatcherApplication.class, args);
    }
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder){
        return builder.sources(DispatcherApplication.class);
    }
}
