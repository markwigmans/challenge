package com.ximedes.sva.frontend.service;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.pattern.PatternsCS;
import akka.util.Timeout;
import com.ximedes.sva.frontend.actor.ActorManager;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Created by mawi on 24/07/2016.
 */
@Service
@Slf4j
public class SimulationService {
    private final ActorRef backendActor;
    private final Timeout timeout;

    /**
     * Auto wired constructor
     */
    @Autowired
    public SimulationService(final ActorManager actorManager, final Timeout timeout) {
        this.backendActor = actorManager.getBackendActor();
        this.timeout = timeout;
    }

    public void reset() throws Exception {
        log.info("Reset simulation");
        PatternsCS.ask(backendActor, new Reset(), timeout).toCompletableFuture().get();
    }

    @Value
    @EqualsAndHashCode
    public static final class Reset implements Serializable {
    }

    @Value
    @EqualsAndHashCode
    public static final class Resetted implements Serializable {
    }
}
