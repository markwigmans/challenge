package com.ximedes.sva.backend.actor;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;

/**
 * Created by mawi on 22/07/2016.
 */
public class BackendManager extends AbstractLoggingActor {

    /**
     * Create Props for an actor of this type.
     */
    public static Props props() {
        return Props.create(BackendManager.class, BackendManager::new);
    }

    private BackendManager() {
        receive(ReceiveBuilder
                .matchAny(o -> log().warning("received unknown message: {}", o)).build());
    }
}
