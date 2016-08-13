/**
 * Copyright (C) 2016 Mark Wigmans (mark.wigmans@gmail.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
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
import com.ximedes.sva.backend.actor.IdActor;
import com.ximedes.sva.backend.actor.LedgerActor;
import com.ximedes.sva.backend.actor.TransferRepositoryActor;
import com.ximedes.sva.frontend.FrontendConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by mawi on 13/11/2015.
 */
@Component("frontendActorManager")
public class ActorManager {

    private final int localIdActorPool;
    private final int accountPoolSize;
    private final int transferPoolSize;

    private final ActorRef transferRepository;
    private final ActorRef ledger;
    private final ActorRef idActor;
    private final ActorRef localIdActorRouter;
    private final ActorRef resetActor;

    /**
     * Auto wired constructor
     */
    @Autowired
    ActorManager(final ActorSystem system, final Timeout timeout, final FrontendConfig config) throws Exception {
        super();
        this.localIdActorPool = config.getLocalIdActorPool();
        this.accountPoolSize = config.getAccountPoolSize();
        this.transferPoolSize = config.getTransferPoolSize();

        system.actorOf(EventStreamActor.props(), "eventStreamActor");

        final ActorRef supervisor = system.actorOf(Supervisor.props(), "supervisor");

        this.transferRepository = (ActorRef) PatternsCS.ask(supervisor, new Supervisor.NamedProps(TransferRepositoryActor.props(transferPoolSize), "transferRepository"), timeout).toCompletableFuture().get();
        this.ledger = (ActorRef) PatternsCS.ask(supervisor, new Supervisor.NamedProps(LedgerActor.props(transferRepository, accountPoolSize), "ledger"), timeout).toCompletableFuture().get();
        this.idActor = (ActorRef) PatternsCS.ask(supervisor, new Supervisor.NamedProps(IdActor.props(accountPoolSize, transferPoolSize), "idActor"), timeout).toCompletableFuture().get();

        this.localIdActorRouter = (ActorRef) PatternsCS.ask(supervisor, new Supervisor.NamedProps(new RoundRobinPool(localIdActorPool)
                .props(LocalIdActor.props(idActor, config.getAccountSize(), config.getTransferSize(), config.getFactor())), "localIdActorRouter"), timeout).toCompletableFuture().get();

        this.resetActor = (ActorRef) PatternsCS.ask(supervisor, new Supervisor.NamedProps(ResetActor.props(), "resetActor"), timeout).toCompletableFuture().get();
    }

    public ActorRef getLedger() {
        return ledger;
    }

    public ActorRef getTransferRepository() {
        return transferRepository;
    }

    public ActorRef getLocalIdActor() {
        return localIdActorRouter;
    }

    public ActorRef getResetActor() {
        return resetActor;
    }
}
