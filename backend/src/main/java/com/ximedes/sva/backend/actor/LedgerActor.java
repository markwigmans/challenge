package com.ximedes.sva.backend.actor;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorSelection;
import akka.actor.Identify;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import com.google.protobuf.TextFormat;
import static com.ximedes.sva.protocol.BackendProtocol.*;
import static com.ximedes.sva.protocol.SimulationProtocol.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mawi on 06/08/2016.
 */
public class LedgerActor extends AbstractLoggingActor {
    private static final int ACCOUNTS  = 310000;
    private static final int TRANSFERS = 1000000;

    private final int[] balance = new int[ACCOUNTS];
    private final int[] overdraft = new int[ACCOUNTS];

    // TODO mus it be a map?
    private final Map<Integer,byte[]> transfers = new HashMap(TRANSFERS,0.9f);

    /**
     * Create Props for an actor of this type.
     */
    public static Props props() {
        return Props.create(LedgerActor.class, LedgerActor::new);
    }

    private LedgerActor() {
        init();
        receive(ReceiveBuilder
                .match(CreateAccountMessage.class, this::createAccount)
                .match(CreateTransferMessage.class, this::processTransfer)
                .match(QueryAccountRequest.class, this::queryAccount)
                .match(QueryTransferRequest.class, this::queryTransfer)
                .match(Reset.class, this::reset)
                .matchAny(o -> log().warning("received unknown message: {}", o)).build());
    }

    // reset the simulation
    void reset(final Reset message) {
        log().info("reset()");
        init();
        sender().tell(Resetted.getDefaultInstance(), self());
    }

    private void init() {
        for (int i = 0; i < ACCOUNTS; i++) {
            balance[i] = Integer.MAX_VALUE;
            overdraft[i] = 0;
        }
        transfers.clear();
    }

    void createAccount(final CreateAccountMessage request) {
        log().debug("message received: [{}]", TextFormat.shortDebugString(request));

        // process message
        balance[request.getAccountId()] = 0;
        overdraft[request.getAccountId()] = request.getOverdraft();
    }

    private void queryAccount(QueryAccountRequest request) {
        log().debug("message received: [{}]", TextFormat.shortDebugString(request));
        final int id = request.getAccountId();
        final QueryAccountResponse response = QueryAccountResponse.newBuilder()
                .setAccountId(id)
                .setBalance(balance[id])
                .setOverdraft(overdraft[id])
                .setStatus(QueryAccountResponse.EnumStatus.CONFIRMED)
                .build();
        sender().tell(response,self());
    }

    void processTransfer(final CreateTransferMessage request) {
        log().debug("message received: [{}]", TextFormat.shortDebugString(request));

        // process message
        final boolean transferred = transfer(request.getFrom(), request.getTo(), request.getAmount());
        storeTransfer(request,transferred);
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

    private void storeTransfer(CreateTransferMessage request, boolean transferred) {
        QueryTransferResponse.EnumStatus status = transferred
                ? QueryTransferResponse.EnumStatus.CONFIRMED
                : QueryTransferResponse.EnumStatus.INSUFFICIENT_FUNDS;
        QueryTransferResponse message =  QueryTransferResponse.newBuilder()
                .setTransferId(request.getTransferId())
                .setFrom(request.getFrom())
                .setTo(request.getTo())
                .setAmount(request.getAmount())
                .setStatus(status)
                .build();

        transfers.put(request.getTransferId(),message.toByteArray());
    }

    private void queryTransfer(QueryTransferRequest request) {
        log().debug("message received: [{}]", TextFormat.shortDebugString(request));
        // TODO make it work
    }
}

