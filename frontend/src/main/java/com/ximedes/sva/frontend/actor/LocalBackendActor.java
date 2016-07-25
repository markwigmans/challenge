package com.ximedes.sva.frontend.actor;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import com.google.protobuf.TextFormat;
import com.ximedes.sva.frontend.service.SimulationService;
import com.ximedes.sva.protocol.BackendProtocol;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mawi on 22/07/2016.
 */
public class LocalBackendActor extends AbstractLoggingActor {

    private static final int ACCOUNTS = 300100;

    private int accountId = 0;
    private final int[] balance = new int[ACCOUNTS];
    private final int[] overdraft = new int[ACCOUNTS];

    private int transferId = 0;
    private Map<Integer,byte[]> transfers = new HashMap(1000000,0.9f);

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
                .match(BackendProtocol.QueryAccountRequest.class, this::queryAccount)
                .match(BackendProtocol.QueryTransferRequest.class, this::queryTransfer)
                .match(SimulationService.Reset.class, this::reset)
                .matchAny(o -> log().warning("received unknown message: {}", o)).build());
    }

    // reset the simulation
    void reset(SimulationService.Reset message) {
        accountId = 0;
        transferId = 0;
        sender().tell(new SimulationService.Resetted(), self());
    }

    void createAccount(final BackendProtocol.CreateAccountRequest request) {
        log().debug("message received: [{}]", TextFormat.shortDebugString(request));

        // send response
        final int id = accountId++;
        sender().tell(BackendProtocol.CreateAccountResponse.newBuilder().setAccountId(id).build(), self());

        // process message
        overdraft[id] = request.getOverdraft();
    }

    private void queryAccount(BackendProtocol.QueryAccountRequest request) {
        log().debug("message received: [{}]", TextFormat.shortDebugString(request));
        final int id = request.getAccountId();
        final BackendProtocol.QueryAccountResponse response = BackendProtocol.QueryAccountResponse.newBuilder()
                .setAccountId(id)
                .setBalance(balance[id])
                .setOverdraft(overdraft[id])
                .setStatus(BackendProtocol.QueryAccountResponse.EnumStatus.CONFIRMED)
                .build();
        sender().tell(response,self());
    }

    void processTransfer(final BackendProtocol.CreateTransferRequest request) {
        log().debug("message received: [{}]", TextFormat.shortDebugString(request));

        // send response
        final int id = transferId++;
        sender().tell(BackendProtocol.CreateTransferResponse.newBuilder().setTransferId(id).build(), self());

        // process message
        final boolean transferred = transfer(request.getFrom(), request.getTo(), request.getAmount());
        storeTransfer(id, request,transferred);
    }

    boolean transfer(int from, int to, int amount) {
        // check balance
        if (balance[from] + overdraft[from] - amount >= 0) {
            log().info("sufficient funds: [{},{},{}]", from,to,amount);
            balance[from] -= amount;
            balance[to]   += amount;
            return true;
        } else {
            log().warning("insufficient funds: [{},{},{}]", from,to,amount);
            return false;
        }
    }

    private void storeTransfer(int id, BackendProtocol.CreateTransferRequest request, boolean transferred) {
        BackendProtocol.QueryTransferResponse.EnumStatus status = transferred
                ? BackendProtocol.QueryTransferResponse.EnumStatus.CONFIRMED
                : BackendProtocol.QueryTransferResponse.EnumStatus.INSUFFICIENT_FUNDS;
        BackendProtocol.QueryTransferResponse message =  BackendProtocol.QueryTransferResponse.newBuilder()
                .setTransferId(id)
                .setFrom(request.getFrom())
                .setTo(request.getTo())
                .setAmount(request.getAmount())
                .setStatus(status)
                .build();

        transfers.put(id,message.toByteArray());
    }

    private void queryTransfer(BackendProtocol.QueryTransferRequest request) {
        log().debug("message received: [{}]", TextFormat.shortDebugString(request));
    }
}
