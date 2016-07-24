/******************************************************************************
 * Copyright 2014,2015 Mark Wigmans
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.ximedes.sva.frontend.service;

import akka.actor.ActorRef;
import akka.pattern.PatternsCS;
import akka.util.Timeout;
import com.ximedes.sva.frontend.actor.ActorManager;
import com.ximedes.sva.frontend.message.Account;
import com.ximedes.sva.protocol.BackendProtocol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class AccountService {

    private final ActorRef backendActor;
    private final Timeout timeout;

    /**
     * Auto wired constructor
     */
    @Autowired
    public AccountService(final ActorManager actorManager, final Timeout timeout) {
        this.backendActor = actorManager.getBackendActor();
        this.timeout = timeout;
    }

    public CompletableFuture<Account> createAccount(final Account request) throws Exception {
        final int overDraft = request.getOverdraft() == null ? 0 : request.getOverdraft();
        final BackendProtocol.CreateAccountRequest message = BackendProtocol.CreateAccountRequest.newBuilder().setOverdraft(overDraft).build();
        final CompletableFuture<Object> ask = PatternsCS.ask(backendActor, message, timeout).toCompletableFuture();

        return ask.thenApply(r -> {
            BackendProtocol.CreateAccountResponse response = (BackendProtocol.CreateAccountResponse) r;
            return Account.builder().accountId(response.getAccountId()).build();
        });
    }


    public Account queryAccount(final String accountId) {

        // TODO
        return null;
    }
}
