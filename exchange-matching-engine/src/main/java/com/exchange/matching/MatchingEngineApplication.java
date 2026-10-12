package com.exchange.matching;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.exchange.matching.mapper")
public class MatchingEngineApplication {
    public static void main(String[] args) {
        SpringApplication.run(MatchingEngineApplication.class, args);
    }
}
