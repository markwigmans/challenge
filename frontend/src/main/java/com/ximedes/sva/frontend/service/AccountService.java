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
package com.ximedes.sva.frontend.service;

import akka.actor.ActorRef;
import akka.pattern.PatternsCS;
import akka.util.Timeout;
import com.ximedes.sva.frontend.actor.ActorManager;
import com.ximedes.sva.frontend.message.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

import static com.ximedes.sva.protocol.BackendProtocol.*;

@Service
public class AccountService {

    private final ActorRef ledger;
    private final ActorRef idActor;
    private final Timeout timeout;

    /**
     * Auto wired constructor
     */
    @Autowired
    public AccountService(final ActorManager actorManager, final Timeout timeout) {
        this.ledger = actorManager.getLedger();
        this.idActor = actorManager.getLocalIdActor();
        this.timeout = timeout;
    }

    public CompletableFuture<Account> createAccount(final Account request) {
        final int overDraft = request.getOverdraft() == null ? 0 : request.getOverdraft();
        final IdRequest idRequest = IdRequest.newBuilder().setType(IdType.ACCOUNTS).build();
        final CompletableFuture<Object> ask = PatternsCS.ask(idActor, idRequest, timeout).toCompletableFuture();

        return ask.thenApply(r -> {
            final IdResponse response = (IdResponse) r;
            ledger.tell(CreateAccountMessage.newBuilder().setAccountId(response.getId()).setOverdraft(overDraft).build(), ActorRef.noSender());
            return Account.builder().accountId(Integer.toString(response.getId())).build();
        });
    }

    public CompletableFuture<Account> queryAccount(final String accountId) {
        final int id = Integer.parseInt(accountId);
        final QueryAccountRequest message = QueryAccountRequest.newBuilder().setAccountId(id).build();
        final CompletableFuture<Object> ask = PatternsCS.ask(ledger, message, timeout).toCompletableFuture();

        return ask.thenApply(r -> {
            final QueryAccountResponse response = (QueryAccountResponse) r;
            switch (response.getStatus()) {
                case CONFIRMED:
                    return Account.builder()
                            .accountId(Integer.toString(response.getAccountId()))
                            .balance(response.getBalance())
                            .overdraft(response.getOverdraft())
                            .build();
                default:
                    return null;
            }
        });
    }
}
