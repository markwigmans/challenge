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
package com.ximedes.sva.backend.actor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.util.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import scala.concurrent.Await;

import static akka.pattern.Patterns.ask;

/**
 * Created by mawi on 13/11/2015.
 */
@Component("backendActorManager")
class ActorManager {

    private final ActorRef backendManager;

    /**
     * Auto wired constructor
     */
    @Autowired
    ActorManager(final ActorSystem system, final Timeout timeout) throws Exception {
        super();
        final ActorRef supervisor = system.actorOf(Supervisor.props());
        this.backendManager = (ActorRef) Await.result(ask(supervisor, new Supervisor.NamedProps(BackendManager.props(), "backendManager"),
                timeout.duration().toMillis()), timeout.duration());
    }
}
