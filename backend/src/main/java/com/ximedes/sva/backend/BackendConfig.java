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
class BackendConfig {

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
