package com.ximedes.sva.backend;

import akka.actor.ActorSystem;
import akka.util.Timeout;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
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
        final Config config = ConfigFactory.parseString("akka.cluster.roles = [backend]").withFallback(ConfigFactory.load());
        return ActorSystem.create("sva-cluster", config);
    }

    @Bean
    Timeout timeout() {
        return Timeout.apply(timeout, TimeUnit.MILLISECONDS);
    }
}
