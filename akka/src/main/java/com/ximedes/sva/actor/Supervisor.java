package com.ximedes.sva.actor;

import akka.actor.AbstractActor;
import akka.actor.OneForOneStrategy;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.japi.pf.DeciderBuilder;
import akka.japi.pf.ReceiveBuilder;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

import static akka.actor.SupervisorStrategy.*;

/**
 *
 */
public class Supervisor extends AbstractActor {

    /**
     * Create Props for an actor of this type.
     */
    public static Props props() {
        return Props.create(Supervisor.class, Supervisor::new);
    }

    private static final SupervisorStrategy strategy =
            new OneForOneStrategy(10, Duration.create(1, TimeUnit.MINUTES), DeciderBuilder.
                    match(ArithmeticException.class, e -> resume()).
                    match(NullPointerException.class, e -> restart()).
                    match(IllegalArgumentException.class, e -> stop()).
                    matchAny(o -> escalate()).build());

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return strategy;
    }

    private Supervisor() {
        receive(ReceiveBuilder.
                match(Props.class, props -> {
                    sender().tell(context().actorOf(props), self());
                }).build()
        );
    }
}
