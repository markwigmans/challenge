package com.ximedes.sva.frontend.actor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.pattern.PatternsCS;
import akka.util.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import scala.concurrent.Await;
import scala.concurrent.ExecutionContext;

import static akka.pattern.Patterns.ask;

/**
 * Created by mawi on 13/11/2015.
 */
@Component
public class ActorManager {

    private final ExecutionContext ec;
    private final ActorRef supervisor;
    private final ActorRef backendActor;

    /**
     * Auto wired constructor
     */
    @Autowired
    ActorManager(final ActorSystem system, final Timeout timeout) throws Exception {
        super();
        this.ec = system.dispatcher();
        this.supervisor = system.actorOf(Supervisor.props());
        this.backendActor = (ActorRef) PatternsCS.ask(supervisor, new Supervisor.NamedProps(LocalBackendActor.props(), "backendActor"), timeout)
                .toCompletableFuture().get();
    }

    public ActorRef getBackendActor() {
        return backendActor;
    }
}
