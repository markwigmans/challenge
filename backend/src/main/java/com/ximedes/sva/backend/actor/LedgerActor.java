/*
 * Copyright (C) 2016 Mark Wigmans (mark.wigmans@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ximedes.sva.backend.actor;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import com.google.protobuf.TextFormat;

import java.io.IOException;

import static com.ximedes.sva.protocol.BackendProtocol.*;
import static com.ximedes.sva.protocol.SimulationProtocol.Reset;
import static com.ximedes.sva.protocol.SimulationProtocol.Resetted;

/**
 * Created by mawi on 06/08/2016.
 */
public class LedgerActor extends AbstractLoggingActor {
    // represent an empty / not used account
    private static final int EMPTY_ACCOUNT = -1;

    private final ActorRef transferActor;
    private final int[] balance;
    private final int[] overdraft;

    /**
     * Create Props for an actor of this type.
     */
    public static Props props(final ActorRef transferActor, final int accountSize) {
        return Props.create(LedgerActor.class, transferActor, accountSize);
    }

    private LedgerActor(final ActorRef transferActor, final int accountSize) {
        log().info("constructor({})", accountSize);
        this.transferActor = transferActor;
        this.balance = new int[accountSize];
        this.overdraft = new int[accountSize];

        init();
        receive(ReceiveBuilder
                .match(CreateAccountMessage.class, this::createAccount)
                .match(CreateTransferMessage.class, this::processTransfer)
                .match(QueryAccountRequest.class, this::queryAccount)
                .match(Reset.class, this::reset)
                .matchAny(this::unhandled)
                .build());
    }

    // reset the simulation
    void reset(final Reset message) {
        log().info("reset()");
        init();
        sender().tell(Resetted.getDefaultInstance(), self());
    }

    private void init() {
        for (int i = 0; i < balance.length; i++) {
            balance[i] = 0;
            overdraft[i] = EMPTY_ACCOUNT;
        }
    }

    void createAccount(final CreateAccountMessage request) {
        log().debug("message received: [{}]", TextFormat.shortDebugString(request));

        // process message
        if (validAccountId(request.getAccountId())) {
            balance[request.getAccountId()] = 0;
            overdraft[request.getAccountId()] = request.getOverdraft();
        } else {
            log().error("illegal account ID: '{}'", request.getAccountId());
        }
    }

    boolean validAccountId(final int id) {
        return id >= 0 && id < balance.length;
    }

    void queryAccount(final QueryAccountRequest request) {
        log().debug("message received: [{}]", TextFormat.shortDebugString(request));
        final QueryAccountResponse response;
        if (accountFound(request.getAccountId())) {
            response = QueryAccountResponse.newBuilder()
                    .setAccountId(request.getAccountId())
                    .setBalance(balance[request.getAccountId()])
                    .setOverdraft(overdraft[request.getAccountId()])
                    .setStatus(QueryAccountResponse.EnumStatus.CONFIRMED)
                    .build();
        } else {
            response = QueryAccountResponse.newBuilder()
                    .setAccountId(request.getAccountId())
                    .setStatus(QueryAccountResponse.EnumStatus.ACCOUNT_NOT_FOUND).build();
        }
        sender().tell(response, self());
    }

    boolean accountFound(final int id) {
        return validAccountId(id) && overdraft[id] != EMPTY_ACCOUNT;
    }

    void processTransfer(final CreateTransferMessage request) throws IOException {
        log().debug("message received: [{}]", request.toString());

        // process message
        final boolean transferred = transfer(request.getFrom(), request.getTo(), request.getAmount());
        storeTransfer(request, transferred);
    }

    boolean transfer(int from, int to, int amount) {
        // check balance
        if (balance[from] + overdraft[from] - amount >= 0) {
            //log().debug("sufficient funds: [{},{},{}]", from, to, amount);
            balance[from] -= amount;
            balance[to] += amount;
            return true;
        } else {
            //log().debug("insufficient funds: [{},{},{}]", from, to, amount);
            return false;
        }
    }

    private void storeTransfer(CreateTransferMessage request, boolean transferred) {
        QueryTransferResponse.EnumStatus status = transferred
                ? QueryTransferResponse.EnumStatus.CONFIRMED
                : QueryTransferResponse.EnumStatus.INSUFFICIENT_FUNDS;
        QueryTransferResponse message = QueryTransferResponse.newBuilder()
                .setTransferId(request.getTransferId())
                .setFrom(request.getFrom())
                .setTo(request.getTo())
                .setAmount(request.getAmount())
                .setStatus(status)
                .build();
        transferActor.tell(message, self());
    }
}

