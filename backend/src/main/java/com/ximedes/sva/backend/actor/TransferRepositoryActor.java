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
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import com.google.protobuf.ByteString;
import com.google.protobuf.TextFormat;

import java.io.IOException;

import static com.ximedes.sva.protocol.BackendProtocol.QueryTransferRequest;
import static com.ximedes.sva.protocol.BackendProtocol.QueryTransferResponse;
import static com.ximedes.sva.protocol.SimulationProtocol.Reset;
import static com.ximedes.sva.protocol.SimulationProtocol.Resetted;

/**
 * Created by mawi on 12/08/2016.
 */
public class TransferRepositoryActor extends AbstractLoggingActor {

    private final ByteString[] transfers;

    /**
     * Create Props for an actor of this type.
     */
    public static Props props(final int transferSize) {
        return Props.create(TransferRepositoryActor.class, transferSize);
    }

    private TransferRepositoryActor(final int transferSize) throws IOException {
        log().info("constructor({})", transferSize);
        transfers = new ByteString[transferSize];
        init();

        receive(ReceiveBuilder
                .match(QueryTransferResponse.class, this::storeTransfer)
                .match(QueryTransferRequest.class, this::queryTransfer)
                .match(Reset.class, this::reset)
                .matchAny(this::unhandled)
                .build());
    }

    private void init() throws IOException {
        QueryTransferResponse template = QueryTransferResponse.newBuilder().setStatus(QueryTransferResponse.EnumStatus.TRANSFER_NOT_FOUND).buildPartial();
        for (int i = 0; i < transfers.length; i++) {
            QueryTransferResponse message = QueryTransferResponse.newBuilder(template).setTransferId(i).build();
            transfers[i] = transform(message);
        }
    }

    void reset(final Reset message) throws IOException {
        log().info("reset()");
        init();
        sender().tell(Resetted.getDefaultInstance(), self());
    }

    void queryTransfer(final QueryTransferRequest request) throws IOException {
        log().debug("message received: [{}]", TextFormat.shortDebugString(request));

        final QueryTransferResponse response = transform(transfers[request.getTransferId()]);
        sender().tell(response, self());
    }

    void storeTransfer(final QueryTransferResponse message) throws IOException {
        log().debug("message received: [{}]", TextFormat.shortDebugString(message));
        transfers[message.getTransferId()] = transform(message);
    }

    ByteString transform(final QueryTransferResponse msg) {
        return msg.toByteString();
    }

    QueryTransferResponse transform(final ByteString bytes) throws IOException {
        return QueryTransferResponse.parseFrom(bytes);
    }
}
