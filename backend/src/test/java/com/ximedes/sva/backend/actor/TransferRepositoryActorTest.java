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

import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;
import akka.testkit.TestActorRef;
import static com.ximedes.sva.protocol.BackendProtocol.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by mawi on 13/08/2016.
 */
public class TransferRepositoryActorTest {

    static ActorSystem system;

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
    public void testToFromBytes() throws Exception {
        final TestActorRef<TransferRepositoryActor> ref = TestActorRef.create(system, TransferRepositoryActor.props(5), "testA");
        final TransferRepositoryActor actor = ref.underlyingActor();

        QueryTransferResponse msg1 = QueryTransferResponse.newBuilder().setTransferId(10)
        .setStatus(QueryTransferResponse.EnumStatus.TRANSFER_NOT_FOUND).build();

        final byte[] bytes = actor.toBytes(msg1);
        assertEquals(msg1.getSerializedSize(), bytes.length);

        QueryTransferResponse msg2 = actor.fromBytes(bytes);
        assertEquals(msg1,msg2);
    }

}