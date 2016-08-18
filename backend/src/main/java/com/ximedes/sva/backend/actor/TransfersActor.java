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
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.TextFormat;

import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.ximedes.sva.protocol.BackendProtocol.*;
import static com.ximedes.sva.protocol.SimulationProtocol.Reset;
import static com.ximedes.sva.protocol.SimulationProtocol.Resetted;

/**
 * Created by mawi on 12/08/2016.
 */
public class TransfersActor extends AbstractLoggingActor {

    private final ByteString[] transfers;

    /**
     * Create Props for an actor of this type.
     */
    public static Props props(final int transferSize) {
        return Props.create(TransfersActor.class, transferSize);
    }

    private TransfersActor(final int transferSize) {
        log().info("constructor({})", transferSize);
        transfers = new ByteString[transferSize];
        init();

        receive(ReceiveBuilder
                .match(QueryTransferResponse.class, this::storeTransfer)
                .match(QueryTransferRequest.class, this::queryTransfer)
                .match(QueryTransferRangeRequest.class, this::queryTransferRangeRequest)
                .match(QueryTransfersRequest.class, this::queryTransfersRequest)
                .match(Reset.class, this::reset)
                .matchAny(this::unhandled)
                .build());
    }

    private void init() {
        QueryTransferResponse template = QueryTransferResponse.newBuilder().setStatus(QueryTransferResponse.EnumStatus.TRANSFER_NOT_FOUND).buildPartial();
        IntStream.range(0, transfers.length)
                .forEach(i -> transfers[i] = transform(QueryTransferResponse.newBuilder(template).setTransferId(i).build()));
    }

    void reset(final Reset message) {
        log().info("reset()");
        init();
        sender().tell(Resetted.getDefaultInstance(), self());
    }

    void queryTransfer(final QueryTransferRequest request) {
        log().debug("message received: [{}]", TextFormat.shortDebugString(request));
        sender().tell(transform(request.getTransferId()), self());
    }

    void queryTransferRangeRequest(final QueryTransferRangeRequest request) {
        final QueryTransfersResponse.Builder builder = QueryTransfersResponse.newBuilder();
        builder.addAllTransfers(IntStream.range(request.getStartTransferId(), request.getEndTransferId()).boxed()
                .map(this::transform)
                .collect(Collectors.toList()));

        sender().tell(builder.build(), self());
    }

    void queryTransfersRequest(final QueryTransfersRequest request) {
        final QueryTransfersResponse.Builder builder = QueryTransfersResponse.newBuilder();
        builder.addAllTransfers(request.getTransferIdsList().stream()
                .map(this::transform)
                .collect(Collectors.toList()));

        sender().tell(builder.build(), self());
    }

    boolean validTransferId(final int id) {
        return id >= 0 && id < transfers.length;
    }

    void storeTransfer(final QueryTransferResponse message) throws IOException {
        log().debug("message received: [{}]", TextFormat.shortDebugString(message));
        if (validTransferId(message.getTransferId())) {
            transfers[message.getTransferId()] = transform(message);
        } else {
            log().warning("illegal transfer ID: '{}'", message.getTransferId());
        }
    }

    ByteString transform(final QueryTransferResponse msg) {
        return msg.toByteString();
    }

    QueryTransferResponse transform(final ByteString bytes) {
        try {
            return QueryTransferResponse.parseFrom(bytes);
        } catch (InvalidProtocolBufferException e) {
            log().error("Exception: {}", e.toString());
            throw new Error(e);
        }
    }

    QueryTransferResponse transform(final int id) {
        final QueryTransferResponse response;
        if (validTransferId(id)) {
            response = transform(transfers[id]);
        } else {
            response = QueryTransferResponse.newBuilder()
                    .setStatus(QueryTransferResponse.EnumStatus.TRANSFER_NOT_FOUND)
                    .setTransferId(id).build();
        }
        return response;
    }
}
