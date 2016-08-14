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
import com.google.protobuf.TextFormat;

import static com.ximedes.sva.protocol.BackendProtocol.*;
import static com.ximedes.sva.protocol.SimulationProtocol.Reset;
import static com.ximedes.sva.protocol.SimulationProtocol.Resetted;

/**
 * handles the requests for ID ranges
 */
public class IdActor extends AbstractLoggingActor {

    private int accountWatermark;
    private int transferWatermark;

    private final int maxAccounts;
    private final int maxTransfers;

    /**
     * Create Props for an actor of this type.
     */
    public static Props props(final int maxAccounts, final int maxTransfers) {
        return Props.create(IdActor.class, maxAccounts, maxTransfers);
    }

    private IdActor(final int maxAccounts, final int maxTransfers) {
        log().info("constructor({},{})", maxAccounts, maxTransfers);
        this.maxAccounts = maxAccounts;
        this.maxTransfers = maxTransfers;

        init();

        receive(ReceiveBuilder
                .match(IdRangeRequest.class, this::idRangeRequest)
                .match(IdRequest.class, this::idRequest)
                .match(Reset.class, this::reset)
                .matchAny(this::unhandled)
                .build());
    }

    private void init() {
        accountWatermark = 0;
        transferWatermark = 0;
    }

    private void idRequest(IdRequest request) {
        if (IdType.ACCOUNTS == request.getType()) {
            final IdResponse message = IdResponse.newBuilder().setType(request.getType()).setId(accountWatermark).build();
            sender().tell(message, self());
            accountWatermark += 1;
        }
        if (IdType.TRANSFERS == request.getType()) {
            final IdResponse message = IdResponse.newBuilder().setType(request.getType()).setId(transferWatermark).build();
            sender().tell(message, self());
            transferWatermark += 1;
        }
    }

    private void idRangeRequest(final IdRangeRequest request) {
        log().debug("idRangeRequest: '{}'", TextFormat.shortDebugString(request));
        if (IdType.ACCOUNTS == request.getType()) {
            // return account ID's
            IdRangeResponse message = createResponse(request.getType(), accountWatermark, request.getIds(), maxAccounts);
            sender().tell(message, self());
            accountWatermark += request.getIds();
        }
        if (IdType.TRANSFERS == request.getType()) {
            IdRangeResponse message = createResponse(request.getType(), transferWatermark, request.getIds(), maxTransfers);
            sender().tell(message, self());
            transferWatermark += request.getIds();
        }
    }

    IdRangeResponse createResponse(final IdType type, final int start, final int count, final int max) {
        log().debug("createResponse({},{},{},{})'", type, start, count, max);
        final IdRangeResponse.Builder builder = IdRangeResponse.newBuilder().setType(type);
        for (int i = 0; (i < count) && (start + i < max); i++) {
            builder.addId(start + i);
        }
        return builder.build();
    }

    // reset the simulation
    void reset(final Reset message) {
        log().info("reset()");
        init();
        sender().tell(Resetted.getDefaultInstance(), self());
    }
}
