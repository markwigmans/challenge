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

import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;
import akka.testkit.TestActorRef;
import org.hamcrest.CoreMatchers;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.ximedes.sva.protocol.BackendProtocol.IdRangeResponse;
import static com.ximedes.sva.protocol.BackendProtocol.IdType;
import static org.junit.Assert.assertThat;

/**
 * Created by mawi on 15/08/2016.
 */
public class IdActorTest {

    private static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();
    }

    @AfterClass
    public static void teardown() {
        JavaTestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void createResponse() throws Exception {
        final int maxAccounts = 20;
        final TestActorRef<IdActor> ref = TestActorRef.create(system, IdActor.props(maxAccounts,0));
        final IdActor actor = ref.underlyingActor();

        final int count = 5;
        final IdRangeResponse response = actor.createResponse(IdType.ACCOUNTS, 3, count, maxAccounts);
        assertThat(response.getIdList().size(), CoreMatchers.is(count));
        assertThat(response.getIdList(), CoreMatchers.hasItems(3,4,5,6,7));
    }

    @Test
    public void createResponseMaxSizeReached() throws Exception {
        final int maxAccounts = 20;
        final TestActorRef<IdActor> ref = TestActorRef.create(system, IdActor.props(maxAccounts,0));
        final IdActor actor = ref.underlyingActor();

        final int count = 5;
        final IdRangeResponse response = actor.createResponse(IdType.ACCOUNTS, 18, count, maxAccounts);
        assertThat(response.getIdList().size(), CoreMatchers.is(2));
        assertThat(response.getIdList(), CoreMatchers.hasItems(18,19));
    }
}