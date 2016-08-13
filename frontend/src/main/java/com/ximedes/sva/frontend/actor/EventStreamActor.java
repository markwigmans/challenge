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
package com.ximedes.sva.frontend.actor;

import akka.actor.AbstractLoggingActor;
import akka.actor.DeadLetter;
import akka.actor.Props;
import akka.actor.UnhandledMessage;
import akka.japi.pf.ReceiveBuilder;
import akka.protobuf.TextFormat;
import static com.ximedes.sva.protocol.BackendProtocol.*;

/**
 * Created by mawi on 12/08/2016.
 */
public class EventStreamActor extends AbstractLoggingActor {
    /**
     * Create Props for an actor of this type.
     */
    public static Props props() {
        return Props.create(EventStreamActor.class);
    }

    private EventStreamActor() {
        receive(ReceiveBuilder.matchAny(m -> logEvent(m)).build());
    }

    @Override
    public void preStart() throws Exception {
        log().info("preStart()");
        getContext().system().eventStream().subscribe(self(), DeadLetter.class);
        getContext().system().eventStream().subscribe(self(), UnhandledMessage.class);
    }

    private void logEvent(final Object msg) {
        if (msg instanceof UnhandledMessage) {
            log().debug("Unhandled message [{}]'", msg);
        } else if (msg instanceof DeadLetter) {
            log().debug("dead letter [{}]'", msg);
        } else {
            log().debug("event stream: '{}'", msg);
        }
    }
}
