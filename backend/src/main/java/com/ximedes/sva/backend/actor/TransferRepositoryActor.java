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
package com.ximedes.sva.backend.actor;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.TextFormat;

import static com.ximedes.sva.protocol.BackendProtocol.*;
import static com.ximedes.sva.protocol.SimulationProtocol.*;

/**
 * Created by mawi on 12/08/2016.
 */
public class TransferRepositoryActor extends AbstractLoggingActor {
    private static final int MESSAGE_SIZE = 20;

    private final byte[][] transfers;

    /**
     * Create Props for an actor of this type.
     */
    public static Props props(final int transferSize) {
        return Props.create(TransferRepositoryActor.class, transferSize);
    }

    private TransferRepositoryActor(final int transferSize) {
        log().info("constructor({})", transferSize);
        transfers = new byte[transferSize][MESSAGE_SIZE];
        init();

        receive(ReceiveBuilder
                .match(QueryTransferResponse.class, this::storeTransfer)
                .match(QueryTransferRequest.class, this::queryTransfer)
                .match(Reset.class, this::reset)
                .matchAny(this::unhandled)
                .build());
    }

    private void init() {
        for (int i = 0; i < transfers.length; i++) {
            QueryTransferResponse message = QueryTransferResponse.newBuilder()
                    .setTransferId(i)
                    .setStatus(QueryTransferResponse.EnumStatus.TRANSFER_NOT_FOUND)
                    .build();
            System.arraycopy( toBytes(message), 0, transfers[i], 0, message.getSerializedSize());
        }
    }

    void reset(final Reset message) {
        log().info("reset()");
        init();
        sender().tell(Resetted.getDefaultInstance(), self());
    }

    void queryTransfer(final QueryTransferRequest request) throws InvalidProtocolBufferException {
        log().debug("message received: [{}]", TextFormat.shortDebugString(request));

        final QueryTransferResponse response = fromBytes(transfers[request.getTransferId()]);
        sender().tell(response, self());
    }

    void storeTransfer(final QueryTransferResponse message) {
        log().debug("message received: [{}]", TextFormat.shortDebugString(message));
        System.arraycopy( toBytes(message), 0, transfers[message.getTransferId()], 0, message.getSerializedSize());
    }

    byte[] toBytes (QueryTransferResponse msg) {
        return msg.toByteArray();
    }

    QueryTransferResponse fromBytes(final byte[] bytes) throws InvalidProtocolBufferException {
        return QueryTransferResponse.parseFrom(bytes);
    }
}
