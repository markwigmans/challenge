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
import akka.actor.ActorSelection;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

import static com.ximedes.sva.protocol.SimulationProtocol.Reset;
import static com.ximedes.sva.protocol.SimulationProtocol.Resetted;

/**
 * Created by mawi on 08/08/2016.
 */
public class ResetActor extends AbstractLoggingActor {
    /**
     * Create Props for an actor of this type.
     */
    public static Props props() {
        return Props.create(ResetActor.class);
    }

    private ResetActor() {
        receive(ReceiveBuilder
                .match(Reset.class, this::reset)
                .matchAny(m -> unhandled(m)).build());
    }

    private void reset(final Reset message) {
        // check if reset was send by my self
        if (!sender().equals(self())) {
            final ActorSelection selection = context().actorSelection("../*");
            selection.tell(message, self());
            getContext().system().scheduler().scheduleOnce(
                    Duration.create(500, TimeUnit.MILLISECONDS),
                    sender(), Resetted.getDefaultInstance(), getContext().dispatcher(), self());
        }
    }
}
