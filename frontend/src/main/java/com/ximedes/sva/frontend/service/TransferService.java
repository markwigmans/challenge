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
import com.ximedes.sva.frontend.message.Transfer;
import com.ximedes.sva.protocol.BackendProtocol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class TransferService {

    private final ActorRef backendActor;
    private final Timeout timeout;

    /**
     * Auto wired constructor
     */
    @Autowired
    public TransferService(final ActorManager actorManager, final Timeout timeout) {
        this.backendActor = actorManager.getBackendActor();
        this.timeout = timeout;
    }

    public CompletableFuture<Transfer> createTransfer(final Transfer request) {
        final BackendProtocol.CreateTransferRequest message = BackendProtocol.CreateTransferRequest.newBuilder()
                .setTo(Integer.parseInt(request.getTo()))
                .setFrom(Integer.parseInt(request.getFrom()))
                .setAmount(request.getAmount()).build();
        final CompletableFuture<Object> ask = PatternsCS.ask(backendActor, message, timeout).toCompletableFuture();

        return ask.thenApply(r -> {
            BackendProtocol.CreateTransferResponse response = (BackendProtocol.CreateTransferResponse) r;
            return Transfer.builder().transferId(Integer.toString(response.getTransferId())).build();
        });
    }

    public Transfer queryTransfer(final String transferId) {
        // TODO
        return null;
    }
}
