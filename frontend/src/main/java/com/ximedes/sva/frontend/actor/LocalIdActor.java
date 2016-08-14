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
package com.ximedes.sva.frontend.actor;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import com.google.protobuf.TextFormat;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.buffer.CircularFifoBuffer;
import scala.Option;

import java.util.List;

import static com.ximedes.sva.protocol.BackendProtocol.*;
import static com.ximedes.sva.protocol.SimulationProtocol.Reset;
import static com.ximedes.sva.protocol.SimulationProtocol.Resetted;


/**
 * Created by mawi on 05/08/2016.
 */
class LocalIdActor extends AbstractLoggingActor {

    private final IdQueue accountIds;
    private final IdQueue transferIds;

    /**
     * Create Props for an actor of this type.
     */
    public static Props props(final ActorRef idActor, final int accountSize, final int transferSize, final int factor) {
        return Props.create(LocalIdActor.class, idActor, accountSize, transferSize, factor);
    }

    private LocalIdActor(final ActorRef idActor, int accountSize, int transferSize, int factor) {
        this.accountIds = new IdQueue(IdType.ACCOUNTS, idActor, self(), accountSize, factor);
        this.transferIds = new IdQueue(IdType.TRANSFERS, idActor, self(), transferSize, factor);

        receive(ReceiveBuilder
                .match(IdRequest.class, this::idRequest)
                .match(IdRangeResponse.class, this::IdRangeResponse)
                .match(Reset.class, this::reset)
                .matchAny(this::unhandled)
                .build());
    }

    @Override
    public void preStart() throws Exception {
        log().info("preStart()");
        initQueues();
    }

    @Override
    public void preRestart(Throwable reason, Option<Object> message) throws Exception {
        log().info("preRestart()", reason);
        // TODO return existing ID's
    }

    private void initQueues() {
        accountIds.init();
        transferIds.init();
    }

    private void idRequest(final IdRequest request) {
        boolean successful = false;
        if (IdType.ACCOUNTS == request.getType()) {
            successful = accountIds.requestId(sender());
        }
        if (IdType.TRANSFERS == request.getType()) {
            successful = transferIds.requestId(sender());
        }
        // resend while the ID queue is being filed
        if (!successful) {
            self().forward(request, context());
        }
    }

    private void IdRangeResponse(final IdRangeResponse response) {
        log().debug("IdRangeResponse: '{}'", TextFormat.shortDebugString(response));
        if (IdType.ACCOUNTS == response.getType()) {
            accountIds.addIds(response.getIdList());
        }
        if (IdType.TRANSFERS == response.getType()) {
            transferIds.addIds(response.getIdList());
        }
    }

    // reset the simulation
    void reset(final Reset message) {
        log().info("reset()");
        initQueues();
        sender().tell(Resetted.getDefaultInstance(), self());
    }

    @Slf4j
    static class IdQueue {
        private final IdType type;
        private final ActorRef actor;
        private final ActorRef self;
        private CircularFifoBuffer ids;
        private int preferredSize;
        private final int requestFactor;
        private boolean blockRequestSend;
        private boolean queueResized;

        public IdQueue(final IdType type, final ActorRef actor, final ActorRef self, final int preferredSize, final int requestFactor) {
            this.type = type;
            this.actor = actor;
            this.self = self;
            this.ids = new CircularFifoBuffer(preferredSize);
            this.preferredSize = preferredSize;
            this.requestFactor = requestFactor;
            blockRequestSend = false;
            queueResized = false;
        }

        public void init() {
            log.info("init()");
            ids.clear();
            requestIds(preferredSize);
        }

        /**
         * @param sender
         * @return {code true} if successful
         */
        public boolean requestId(final ActorRef sender) {
            if (ids.isEmpty()) {
                if (!queueResized) {
                    // resize queue
                    preferredSize *= 1.5;
                    log.warn("queue ({}) is empty, resize to: {}", type, preferredSize);
                    this.ids = new CircularFifoBuffer(preferredSize);
                    queueResized = true;
                } else {
                    log.debug("queue ({}) still empty", type);
                }
                return false;
            } else {
                final Integer id = (Integer) ids.remove();
                final IdResponse message = IdResponse.newBuilder().setType(type).setId(id).build();
                sender.tell(message, self);
                if (!blockRequestSend && (ids.size() * requestFactor <= preferredSize)) {
                    // we need more ID's
                    requestIds(preferredSize - ids.size());
                }
            }
            return true;
        }

        void requestIds(final int size) {
            log.debug("{} : requestIds({})", type, size);
            actor.tell(IdRangeRequest.newBuilder().setType(type).setIds(size).build(), self);
            blockRequestSend = true;
        }

        public void addIds(final List<Integer> list) {
            log.debug("{} : addIds({}), old size ids:{}", type, list.size(), ids.size());
            list.forEach(ids::add);
            blockRequestSend = false;
            queueResized = false;
        }
    }
}
