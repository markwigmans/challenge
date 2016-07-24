package com.ximedes.sva.frontend.actor;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;

/**
 * Created by mawi on 22/07/2016.
 */
public class BackendActor extends AbstractLoggingActor {

    /**
     * Create Props for an actor of this type.
     */
    public static Props props() {
        return Props.create(BackendActor.class, BackendActor::new);
    }

    private BackendActor() {
        receive(ReceiveBuilder
                .matchAny(o -> log().warning("received unknown message: {}", o)).build());
    }
}
