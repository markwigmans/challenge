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
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;

/**
 * Created by mawi on 22/07/2016.
 */
class BackendActor extends AbstractLoggingActor {

    /**
     * Create Props for an actor of this type.
     */
    public static Props props() {
        return Props.create(BackendActor.class, BackendActor::new);
    }

    private BackendActor() {
        receive(ReceiveBuilder
                .matchAny(o -> log().warning("received unknown message: {}", o)).build());
    }
}
