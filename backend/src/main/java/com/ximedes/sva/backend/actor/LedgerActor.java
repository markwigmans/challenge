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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
                .match(QueryAccountRangeRequest.class, this::queryAccountRangeRequest)
                .match(QueryAccountsRequest.class, this::queryAccountsRequest)
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

    void createAccount(final CreateAccountMessage message) {
        log().debug("message received: [{}]", TextFormat.shortDebugString(message));

        // process message
        if (validAccountId(message.getAccountId())) {
            balance[message.getAccountId()] = 0;
            overdraft[message.getAccountId()] = message.getOverdraft();
        } else {
            log().warning("illegal account ID: '{}'", message.getAccountId());
        }
    }

    boolean validAccountId(final int id) {
        return id >= 0 && id < balance.length;
    }

    void queryAccount(final QueryAccountRequest request) {
        log().debug("message received: [{}]", TextFormat.shortDebugString(request));
        sender().tell(transform(request.getAccountId()), self());
    }

    QueryAccountResponse transform(final int id) {
        final QueryAccountResponse response;
        if (accountFound(id)) {
            response = QueryAccountResponse.newBuilder()
                    .setAccountId(id)
                    .setBalance(balance[id])
                    .setOverdraft(overdraft[id])
                    .setStatus(QueryAccountResponse.EnumStatus.CONFIRMED)
                    .build();
        } else {
            response = QueryAccountResponse.newBuilder()
                    .setAccountId(id)
                    .setStatus(QueryAccountResponse.EnumStatus.ACCOUNT_NOT_FOUND).build();
        }
        return response;
    }

    void queryAccountRangeRequest(final QueryAccountRangeRequest request) {
        final QueryAccountsResponse.Builder builder = QueryAccountsResponse.newBuilder();
        builder.addAllAccounts(IntStream.range(request.getStartAccountId(), request.getEndAccountId()).boxed()
                .map(this::transform)
                .collect(Collectors.toList()));

        sender().tell(builder.build(), self());
    }

    void queryAccountsRequest(final QueryAccountsRequest request) {
        final QueryAccountsResponse.Builder builder = QueryAccountsResponse.newBuilder();
        builder.addAllAccounts(request.getAccountIdsList().stream().map(this::transform).collect(Collectors.toList()));
        sender().tell(builder.build(), self());
    }

    boolean accountFound(final int id) {
        return validAccountId(id) && (overdraft[id] != EMPTY_ACCOUNT);
    }

    void processTransfer(final CreateTransferMessage request) throws IOException {
        log().debug("message received: [{}]", request.toString());

        // process message
        final QueryTransferResponse.EnumStatus status = transfer(request.getFrom(), request.getTo(), request.getAmount());
        storeTransfer(request, status);
    }

    QueryTransferResponse.EnumStatus transfer(final int from, final int to, final int amount) {
        if (accountFound(from) && accountFound(to)) {
            // check balance
            if (balance[from] + overdraft[from] - amount >= 0) {
                //log().debug("sufficient funds: [{},{},{}]", from, to, amount);
                balance[from] -= amount;
                balance[to] += amount;
                return QueryTransferResponse.EnumStatus.CONFIRMED;
            } else {
                //log().debug("insufficient funds: [{},{},{}]", from, to, amount);
                return QueryTransferResponse.EnumStatus.INSUFFICIENT_FUNDS;
            }
        } else {
            return QueryTransferResponse.EnumStatus.ACCOUNT_NOT_FOUND;
        }
    }

    private void storeTransfer(CreateTransferMessage request, final QueryTransferResponse.EnumStatus status) {
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

