package com.ximedes.sva.backend.actor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.util.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import scala.concurrent.Await;
import scala.concurrent.ExecutionContext;

import static akka.pattern.Patterns.ask;

/**
 * Created by mawi on 13/11/2015.
 */
@Component("backendActorManager")
public class ActorManager {

    private final ExecutionContext ec;
    private final ActorRef supervisor;
    private final ActorRef backendManager;

    /**
     * Auto wired constructor
     */
    @Autowired
    ActorManager(final ActorSystem system, final Timeout timeout) throws Exception {
        super();
        this.ec = system.dispatcher();
        this.supervisor = system.actorOf(Supervisor.props());
        this.backendManager = (ActorRef) Await.result(ask(supervisor, new Supervisor.NamedProps(BackendManager.props(), "backendManager"),
                timeout.duration().toMillis()), timeout.duration());
    }

    public ExecutionContext getEc() {
        return ec;
    }

    public ActorRef getBackendManager() {
        return backendManager;
    }
}
