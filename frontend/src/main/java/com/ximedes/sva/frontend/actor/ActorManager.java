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
package com.ximedes.sva.frontend.actor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.pattern.PatternsCS;
import akka.routing.RoundRobinPool;
import akka.util.Timeout;
import com.ximedes.sva.frontend.FrontendConfig;
import com.ximedes.sva.shared.ClusterActors;
import com.ximedes.sva.shared.ClusterRoles;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by mawi on 13/11/2015.
 */
@Component("frontendActorManager")
@Slf4j
public class ActorManager {

    private final ActorSystem system;
    private final ActorRef transfers;
    private final ActorRef ledger;
    private final ActorRef localIdActorRouter;
    private final ActorRef resetActor;


    /**
     * Auto wired constructor
     */
    @Autowired
    ActorManager(final ActorSystem system, final Timeout timeout, final FrontendConfig config) throws Exception {
        this.system = system;

        final int localIdActorPool = config.getLocalIdActorPool();

        system.actorOf(EventStreamActor.props(), "eventStreamActor");

        final ActorRef supervisor = system.actorOf(Supervisor.props(), "supervisor");

        this.transfers = (ActorRef) PatternsCS.ask(supervisor, new Supervisor.NamedProps(BackendProxy.props(ClusterActors.TRANSFERS),
                "proxy-transfers"), timeout).toCompletableFuture().get();
        this.ledger = (ActorRef) PatternsCS.ask(supervisor, new Supervisor.NamedProps(BackendProxy.props(ClusterActors.LEDGER),
                "proxy-ledger"), timeout).toCompletableFuture().get();
        final ActorRef idActor = (ActorRef) PatternsCS.ask(supervisor, new Supervisor.NamedProps(BackendProxy.props(ClusterActors.ID_GENERATOR),
                "proxy-idActor"), timeout).toCompletableFuture().get();

        this.localIdActorRouter = (ActorRef) PatternsCS.ask(supervisor, new Supervisor.NamedProps(new RoundRobinPool(localIdActorPool)
                .props(LocalIdActor.props(idActor, config.getIdPoolSize(), config.getRequestFactor(), config.getResizeFactor())),
                "local-IdActorRouter"), timeout).toCompletableFuture().get();

        this.resetActor = (ActorRef) PatternsCS.ask(supervisor, new Supervisor.NamedProps(ResetActor.props(), "resetActor"), timeout).toCompletableFuture().get();

        // register frontend to the cluster
        system.actorOf(ClusterManager.props(idActor, ledger, transfers), ClusterRoles.FRONTEND.getName());
    }

    public ActorRef getLedger() {
        return ledger;
    }

    public ActorRef getTransfers() {
        return transfers;
    }

    public ActorRef getLocalIdActor() {
        return localIdActorRouter;
    }

    public ActorRef getResetActor() {
        return resetActor;
    }
}
