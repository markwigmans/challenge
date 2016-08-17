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
import akka.pattern.PatternsCS;
import akka.util.Timeout;
import com.ximedes.sva.backend.BackendConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by mawi on 13/11/2015.
 */
@Component("backendActorManager")
class ActorManager {

    /**
     * Auto wired constructor
     */
    @Autowired
    ActorManager(final ActorSystem system, final Timeout timeout, final BackendConfig config) throws Exception {

        final int accountPoolSize = config.getAccountPoolSize();
        final int transferPoolSize = config.getTransferPoolSize();

        final ActorRef supervisor = system.actorOf(Supervisor.props(), "supervisor");

        final ActorRef idActor = (ActorRef) PatternsCS.ask(supervisor, new Supervisor.NamedProps(IdActor.props(accountPoolSize, transferPoolSize),
                "idActor"), timeout).toCompletableFuture().get();
        final ActorRef transfers = (ActorRef) PatternsCS.ask(supervisor, new Supervisor.NamedProps(TransfersActor.props(transferPoolSize),
                "transfers"), timeout).toCompletableFuture().get();
        final ActorRef ledger = (ActorRef) PatternsCS.ask(supervisor, new Supervisor.NamedProps(LedgerActor.props(transfers, accountPoolSize),
                "ledger"), timeout).toCompletableFuture().get();

        // create
        system.actorOf(ClusterManager.props(idActor, ledger, transfers), "backend");
    }
}
