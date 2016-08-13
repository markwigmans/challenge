/**
 * Copyright (C) 2016 Mark Wigmans (mark.wigmans@gmail.com)
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
 */
package com.ximedes.sva.frontend.service;

import akka.actor.ActorRef;
import akka.pattern.PatternsCS;
import akka.util.Timeout;
import com.ximedes.sva.frontend.actor.ActorManager;
import com.ximedes.sva.frontend.message.Transfer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

import static com.ximedes.sva.protocol.BackendProtocol.*;

@Service
public class TransferService {

    private final ActorRef ledger;
    private final ActorRef transferRepository;
    private final ActorRef idActor;
    private final Timeout timeout;

    /**
     * Auto wired constructor
     */
    @Autowired
    public TransferService(final ActorManager actorManager, final Timeout timeout) {
        this.ledger = actorManager.getLedger();
        this.transferRepository = actorManager.getTransferRepository();
        this.idActor = actorManager.getLocalIdActor();
        this.timeout = timeout;
    }

    public CompletableFuture<Transfer> createTransfer(final Transfer request) {
        final IdRequest idRequest = IdRequest.newBuilder().setType(IdType.TRANSFERS).build();
        final CompletableFuture<Object> ask = PatternsCS.ask(idActor, idRequest, timeout).toCompletableFuture();

        return ask.thenApply(r -> {
            final IdResponse response = (IdResponse) r;
            final CreateTransferMessage message = CreateTransferMessage.newBuilder()
                    .setTransferId(response.getId())
                    .setTo(Integer.parseInt(request.getTo()))
                    .setFrom(Integer.parseInt(request.getFrom()))
                    .setAmount(request.getAmount()).build();
            ledger.tell(message, ActorRef.noSender());
            return Transfer.builder().transferId(Integer.toString(response.getId())).build();
        });
    }

    public CompletableFuture<Transfer> queryTransfer(final String transferId) {
        final int id = Integer.parseInt(transferId);
        final QueryTransferRequest message = QueryTransferRequest.newBuilder().setTransferId(id).build();
        final CompletableFuture<Object> ask = PatternsCS.ask(transferRepository, message, timeout).toCompletableFuture();

        return ask.thenApply(r -> {
            final QueryTransferResponse response = (QueryTransferResponse) r;
            switch (response.getStatus()) {
                case PENDING:
                case CONFIRMED:
                case INSUFFICIENT_FUNDS:
                case ACCOUNT_NOT_FOUND:
                    return Transfer.builder()
                            .transferId(Integer.toString(response.getTransferId()))
                            .to(Integer.toString(response.getTo()))
                            .from(Integer.toString(response.getFrom()))
                            .amount(response.getAmount())
                            .status(response.getStatus().toString())
                            .build();
                default:
                    return null;
            }
        });
    }
}
