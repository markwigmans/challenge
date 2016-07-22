package com.ximedes.sva.backend;

import akka.actor.ActorSystem;
import akka.util.Timeout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Created by mawi on 22/07/2016.
 */
@Configuration
public class BackendConfig {

    /**
     * Default timeout for processing calls.
     */
    @Value("${backend.actor.ask.timeout.ms:500}")
    private int timeout;

    @Bean
    ActorSystem actorSystem() {
        return ActorSystem.create("backend");
    }

    @Bean
    Timeout timeout() {
        return Timeout.apply(timeout, TimeUnit.MILLISECONDS);
    }
}
