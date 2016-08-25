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
import com.ximedes.sva.shared.ClusterConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
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

    @Value("${actor.creation.timeout.s:60}")
    private int creationTimeout;

    @Value("${actor.idActor.pool:8}")
    private int localIdActorPool;

    @Value("${id.pool:256}")
    private int idPoolSize;
    @Value("${id.pool.request.factor:2}")
    private int requestFactor;
    @Value("${id.pool.resize.factor:1.5}")
    private float resizeFactor;

    @Value("${seed.hostname:127.0.0.1}")
    private String seedHostName;
    @Value("${seed.port:2550}")
    private int seedPort;

    @Value("${clustering.hostname:127.0.0.1}")
    private String hostName;
    @Value("${clustering.port:2551}")
    private int port;

    @Value("${monitoring.hostname:127.0.0.1}")
    private String monitoringdHostName;
    @Value("${monitoring.port:8125}")
    private int monitoringPort;

    @Bean
    ActorSystem actorSystem() throws UnknownHostException {
        final Map<String, Object> options = new HashMap<>();
        options.put("akka.cluster.roles", Arrays.asList(ClusterConstants.FRONTEND));
        options.put(String.format("akka.cluster.role.%s.min-nr-of-members", ClusterConstants.BACKEND), Integer.toString(1));
        options.put(String.format("akka.cluster.role.%s.min-nr-of-members", ClusterConstants.FRONTEND), Integer.toString(1));

        options.put("akka.cluster.seed-nodes", Arrays.asList(String.format("akka.tcp://%s@%s:%d", ClusterConstants.CLUSTER, seedHostName, seedPort)));
        options.put("akka.remote.netty.tcp.hostname", hostName);
        options.put("akka.remote.netty.tcp.port", Integer.toString(port));
        options.put("akka.remote.netty.tcp.bind-hostname", "0.0.0.0");
        options.put("akka.remote.netty.tcp.bind-port", 2551);

        options.put("kamon.statsd.hostname", monitoringdHostName);
        options.put("kamon.statsd.port", monitoringPort);

        final Config config = ConfigFactory.parseMap(options).withFallback(ConfigFactory.load());
        return ActorSystem.create(ClusterConstants.CLUSTER, config);
    }

    @Bean
    Timeout timeout() {
        return Timeout.apply(timeout, TimeUnit.MILLISECONDS);
    }

    @PostConstruct
    void postConstruct() {
        log.info("actor.ask.timeout.ms:{} ms", timeout);
        log.info("actor.creation.timeout.s:{} s", creationTimeout);

        log.info("actor.idActor.pool:{}", localIdActorPool);

        log.info("id.pool:{}", idPoolSize);
        log.info("id.pool.request.factor:{}", requestFactor);
        log.info("id.pool.resize.factor:{}", resizeFactor);

        log.info("seed.hostname:{}", seedHostName);
        log.info("seed.port:{}", seedPort);

        log.info("clustering.hostname:{}", hostName);
        log.info("clustering.por:{}", port);

        log.info("monitoring.hostname:{}", monitoringdHostName);
        log.info("monitoring.port:{}", monitoringPort);
    }

    public int getCreationTimeout() {
        return creationTimeout;
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
