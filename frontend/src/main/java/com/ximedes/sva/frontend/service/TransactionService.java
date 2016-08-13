/**
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
import com.ximedes.sva.frontend.message.Transaction;
import com.ximedes.sva.frontend.message.Transfer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

import static com.ximedes.sva.protocol.BackendProtocol.*;

@Service
public class TransactionService {

    private final ActorRef transferRepository;
    private final Timeout timeout;

    /**
     * Auto wired constructor
     */
    @Autowired
    public TransactionService(final ActorManager actorManager, final Timeout timeout) {
        this.transferRepository = actorManager.getTransferRepository();
        this.timeout = timeout;
    }

    public CompletableFuture<Transaction> queryTransaction(final String transactionId) {
        final int id = Integer.parseInt(transactionId);
        final QueryTransferRequest message = QueryTransferRequest.newBuilder().setTransferId(id).build();
        final CompletableFuture<Object> ask = PatternsCS.ask(transferRepository, message, timeout).toCompletableFuture();

        return ask.thenApply(r -> {
            final QueryTransferResponse response = (QueryTransferResponse) r;
            switch (response.getStatus()) {
                case CONFIRMED:
                    return Transaction.builder()
                            .transactionId(Integer.toString(response.getTransferId()))
                            .to(Integer.toString(response.getTo()))
                            .from(Integer.toString(response.getFrom()))
                            .amount(response.getAmount())
                            .build();
                default:
                    return null;
            }
        });
    }
}
