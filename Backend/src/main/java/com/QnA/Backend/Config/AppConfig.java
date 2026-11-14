package com.QnA.Backend.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestClient;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AppConfig {
    ////Without Config (100 parallel requests):
    // GitHub views your server as a malicious Distributed Denial of Service (DDoS) attack or an aggressive scraper.
    // It will instantly slap your application with an HTTP 403 Forbidden or HTTP 429 Too Many Requests error
    // . Your downloads won't be fast—they will completely fail.
    ///• With Config (4 parallel requests): GitHub sees a well-behaved, polite application.
    // It allows the 4 downloads to run at normal, maximum speed.
    // As soon as one finishes, the next one in line starts.

    @Bean
    RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    @Bean(name = "indexingExecutor")
    Executor indexingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("index-");
        executor.initialize();
        return executor;

    }
}
