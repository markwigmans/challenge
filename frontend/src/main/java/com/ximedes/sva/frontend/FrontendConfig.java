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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Created by mawi on 22/07/2016.
 */
@Configuration
public class FrontendConfig {

    /**
     * Default timeout for processing calls.
     */
    @Value("${frontend.actor.ask.timeout.ms:5000}")
    private int timeout;

    @Value("${frontend.actor.idActor.pool:8}")
    private int localIdActorPool;

    // TODO use for ledger as well
    @Value("${frontend.account.pool:360000}")
    private int accountPoolSize;

    @Value("${frontend.transfer.pool:1200000}")
    private int transferPoolSize;

    @Value("${frontend.account.id.pool:32}")
    private int accountSize;
    @Value("${frontend.transfer.id.pool:32}")
    private int transferSize;
    @Value("${frontend.id.pool.factor:2}")
    private int factor;

    @Bean
    ActorSystem actorSystem() {
        final Config config = ConfigFactory.parseString("akka.cluster.roles = [frontend]").withFallback(ConfigFactory.load());
        return ActorSystem.create("sva-cluster", config);
    }

    @Bean
    Timeout timeout() {
        return Timeout.apply(timeout, TimeUnit.MILLISECONDS);
    }

    public int getLocalIdActorPool() {
        return localIdActorPool;
    }

    public int getAccountPoolSize() {
        return accountPoolSize;
    }

    public int getTransferPoolSize() {
        return transferPoolSize;
    }

    public int getAccountSize() {
        return accountSize;
    }

    public int getTransferSize() {
        return transferSize;
    }

    public int getFactor() {
        return factor;
    }
}
