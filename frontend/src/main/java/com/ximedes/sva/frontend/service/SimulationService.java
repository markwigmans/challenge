/**
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
package com.ximedes.sva.frontend.service;

import akka.actor.ActorRef;
import akka.pattern.PatternsCS;
import akka.util.Timeout;
import com.ximedes.sva.frontend.actor.ActorManager;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;

/**
 * Created by mawi on 24/07/2016.
 */
@Service
@Slf4j
public class SimulationService {
    private final ActorRef backendActor;
    private final Timeout timeout;

    /**
     * Auto wired constructor
     */
    @Autowired
    public SimulationService(final ActorManager actorManager, final Timeout timeout) {
        this.backendActor = actorManager.getBackendActor();
        this.timeout = timeout;
    }

    public void reset() throws Exception {
        log.info("Reset simulation");
        PatternsCS.ask(backendActor, new Reset(), timeout).toCompletableFuture().get();
    }

    @Value
    @EqualsAndHashCode
    public static final class Reset implements Serializable {
    }

    @Value
    @EqualsAndHashCode
    public static final class Resetted implements Serializable {
    }
}
