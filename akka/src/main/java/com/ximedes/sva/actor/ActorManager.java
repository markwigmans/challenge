package com.ximedes.sva.actor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.util.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import scala.concurrent.ExecutionContext;

/**
 * Created by mawi on 13/11/2015.
 */
@Component
public class ActorManager {

    private final ExecutionContext ec;
    private final ActorRef supervisor;


    /**
     * Auto wired constructor
     */
    @Autowired
    ActorManager(final ActorSystem system, final Timeout timeout) throws Exception {
        super();
        this.ec = system.dispatcher();
        this.supervisor = system.actorOf(Supervisor.props());
    }

    public ExecutionContext getEc() {
        return ec;
    }
}
