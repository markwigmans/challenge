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

import akka.actor.ActorRef;
import akka.testkit.JavaTestKit;
import com.ximedes.sva.protocol.BackendProtocol;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import static org.junit.Assert.assertThat;

/**
 * Created by mawi on 18/08/2016.
 */
public class LedgerActorTest extends AbstractActorTest {

    @Test
    public void queryAccountRangeRequest() {
        new JavaTestKit(system) {{
            final ActorRef transfers = system.actorOf(TransfersActor.props(100));
            final ActorRef ledger = system.actorOf(LedgerActor.props(transfers, 50));
            final JavaTestKit probe = new JavaTestKit(system);
            ledger.tell(probe.getRef(), getRef());
            //expectMsgClass(String.class);

            // create an account
            ledger.tell(BackendProtocol.CreateAccountMessage.newBuilder().setAccountId(2).setOverdraft(100).build(), getRef());

            // send tested request
            final int start = -2;
            final int end = 7;
            BackendProtocol.QueryAccountRangeRequest request = BackendProtocol.QueryAccountRangeRequest.newBuilder()
                    .setStartAccountId(start)
                    .setEndAccountId(end).build();
            ledger.tell(request, getRef());
            final BackendProtocol.QueryAccountsResponse response = expectMsgClass(BackendProtocol.QueryAccountsResponse.class);

            // test response
            assertThat(response.getAccountsList().size(), CoreMatchers.is(end - start));
            // test create account
            assertThat(response.getAccountsList().get(2 - start).getOverdraft(), CoreMatchers.is(100));
            assertThat(response.getAccountsList().get(2 - start).getStatus(), CoreMatchers.is(BackendProtocol.QueryAccountResponse.EnumStatus.CONFIRMED));
            // rest should be empty
            assertThat(response.getAccountsList().get(0).getStatus(), CoreMatchers.is(BackendProtocol.QueryAccountResponse.EnumStatus.ACCOUNT_NOT_FOUND));
        }};
    }
}