/*
 * Copyright (C) 2016 Mark Wigmans (mark.wigmans@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ximedes.sva.frontend;

import akka.actor.ActorSystem;
import akka.util.Timeout;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.ximedes.sva.shared.ClusterRoles;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * Created by mawi on 22/07/2016.
 */
@Configuration
@Slf4j
public class FrontendConfig {

    /**
     * Default timeout for processing calls.
     */
    @Value("${actor.ask.timeout.ms:5000}")
    private int timeout;

    @Value("${actor.idActor.pool:8}")
    private int localIdActorPool;

    @Value("${id.pool:256}")
    private int idPoolSize;
    @Value("${id.pool.request.factor:2}")
    private int requestFactor;
    @Value("${id.pool.resize.factor:1.5}")
    private float resizeFactor;

    @Bean
    ActorSystem actorSystem() {
        final String roles = String.format("akka.cluster.roles = [%s]", ClusterRoles.FRONTEND.getName());
        final Config config = ConfigFactory.parseString(roles).withFallback(ConfigFactory.load());
        return ActorSystem.create("sva-cluster", config);
    }

    @Bean
    Timeout timeout() {
        return Timeout.apply(timeout, TimeUnit.MILLISECONDS);
    }

    @PostConstruct
    void postConstruct() {
        log.info("timeout:{}", timeout);
        log.info("localIdActorPool:{}", localIdActorPool);
        log.info("idPoolSize:{}", idPoolSize);
        log.info("requestFactor:{}", requestFactor);
        log.info("resizeFactor:{}", resizeFactor);
    }

    public int getLocalIdActorPool() {
        return localIdActorPool;
    }

    public int getIdPoolSize() {
        return idPoolSize;
    }

    public int getRequestFactor() {
        return requestFactor;
    }

    public float getResizeFactor() {
        return resizeFactor;
    }
}
