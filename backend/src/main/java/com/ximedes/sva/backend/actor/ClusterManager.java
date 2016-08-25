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
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.Member;
import akka.cluster.MemberStatus;
import akka.japi.pf.ReceiveBuilder;
import com.ximedes.sva.shared.ClusterConstants;

import static com.ximedes.sva.protocol.ClusterProtocol.*;
import static com.ximedes.sva.protocol.ClusterProtocol.BackendRegistration;
import static com.ximedes.sva.shared.ClusterActors.*;

/**
 * Created by mawi on 16/08/2016.
 */
public class ClusterManager extends AbstractLoggingActor {

    private final Cluster cluster;

    private final ActorRef idActor;
    private final ActorRef ledger;
    private final ActorRef transferRepository;

    /**
     * Create Props for an actor of this type.
     */
    public static Props props(final ActorRef idActor, final ActorRef ledger, final ActorRef transferRepository) {
        return Props.create(ClusterManager.class, idActor, ledger, transferRepository);
    }

    private ClusterManager(final ActorRef idActor, final ActorRef ledger, final ActorRef transferRepository) {
        this.idActor = idActor;
        this.ledger = ledger;
        this.transferRepository = transferRepository;
        this.cluster = Cluster.get(getContext().system());

        receive(ReceiveBuilder
                .match(ClusterEvent.CurrentClusterState.class, this::currentClusterState)
                .match(ClusterEvent.MemberUp.class, this::memberUp)
                .matchAny(this::unhandled)
                .build());
    }

    private void memberUp(ClusterEvent.MemberUp message) {
        register(message.member());
    }

    void currentClusterState(final ClusterEvent.CurrentClusterState state) {
        for (Member member : state.getMembers()) {
            if (member.status().equals(MemberStatus.up())) {
                register(member);
            }
        }
    }

    void register(final Member member) {
        log().debug("register: roles [{}]", String.join(",", member.getRoles()));

        if (member.hasRole(ClusterConstants.FRONTEND)) {
            final String idActorPath = idActor.path().toStringWithAddress(getContext().provider().getDefaultAddress());
            final String ledgerPath = ledger.path().toStringWithAddress(getContext().provider().getDefaultAddress());
            final String transfersPath = transferRepository.path().toStringWithAddress(getContext().provider().getDefaultAddress());

            BackendRegistration message = BackendRegistration.newBuilder()
                    .addActors(Actor.newBuilder().setType(ID_GENERATOR.toString()).setActorPath(idActorPath).build())
                    .addActors(Actor.newBuilder().setType(LEDGER.toString()).setActorPath(ledgerPath).build())
                    .addActors(Actor.newBuilder().setType(TRANSFERS.toString()).setActorPath(transfersPath).build())
                    .build();

            final String actorPath = String.format("%s/user/%s", member.address(), ClusterConstants.FRONTEND);
            log().info("register: '{}'", actorPath);
            getContext().actorSelection(actorPath).tell(message, self());
        }
    }

    //subscribe to cluster changes, MemberUp
    @Override
    public void preStart() {
        cluster.subscribe(self(), ClusterEvent.MemberUp.class);
    }

    //re-subscribe when restart
    @Override
    public void postStop() {
        log().info("postStop()");
        cluster.unsubscribe(self());
    }
}
