package com.ximedes.sva.frontend.actor;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import com.ximedes.sva.frontend.service.SimulationService;
import com.ximedes.sva.protocol.BackendProtocol;

/**
 * Created by mawi on 22/07/2016.
 */
public class LocalBackendActor extends AbstractLoggingActor {

    private static final int ACCOUNTS = 300100;

    private int accountId = 0;
    private final int[] balance = new int[ACCOUNTS];
    private final int[] overdraft = new int[ACCOUNTS];

    private int transferId = 0;

    /**
     * Create Props for an actor of this type.
     */
    public static Props props() {
        return Props.create(LocalBackendActor.class, LocalBackendActor::new);
    }

    private LocalBackendActor() {
        receive(ReceiveBuilder
                .match(BackendProtocol.CreateAccountRequest.class, this::createAccount)
                .match(BackendProtocol.CreateTransferRequest.class, this::processTransfer)
                .match(SimulationService.Reset.class, this::reset)
                .matchAny(o -> log().warning("received unknown message: {}", o)).build());
    }

    // reset the simulation
    private void reset(SimulationService.Reset message) {
        accountId = 0;
        transferId = 0;
        sender().tell(new SimulationService.Resetted(), self());
    }

    void createAccount(final BackendProtocol.CreateAccountRequest request) {
        log().debug("message received: {}", request);

        // send response
        final int id = accountId++;
        sender().tell(BackendProtocol.CreateAccountResponse.newBuilder().setAccountId(id).build(), self());

        // process message
        overdraft[id] = request.getOverdraft();
    }

    void processTransfer(final BackendProtocol.CreateTransferRequest request) {
        log().debug("message received: {}", request);

        // send response
        final int id = transferId++;
        sender().tell(BackendProtocol.CreateTransferResponse.newBuilder().setTransferId(id).build(), self());

        // process message
        transfer(request.getFrom(), request.getTo(), request.getAmount());
    }

    void transfer(int from, int to, int amount) {
        // check balance
        if (balance[from] + overdraft[from] - amount >= 0) {
            // sufficient funds
            balance[from] -= amount;
            balance[to] -= amount;
        }
    }
}
