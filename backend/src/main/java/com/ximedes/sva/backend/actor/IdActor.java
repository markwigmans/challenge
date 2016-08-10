package com.ximedes.sva.backend.actor;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import com.google.protobuf.TextFormat;
import com.ximedes.sva.protocol.SimulationProtocol;

import static com.ximedes.sva.protocol.BackendProtocol.*;

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
                .match(SimulationProtocol.Reset.class, this::reset)
                .matchAny(o -> log().warning("received unknown message: {}", o)).build());
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
        // log().debug("idRangeRequest: '{}'", TextFormat.shortDebugString(request));
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
        // log().debug("createResponse({},{},{},{})'", type,start,count,max);
        final IdRangeResponse.Builder builder = IdRangeResponse.newBuilder().setType(type);
        for (int i = 0; (i < count) && (start + i < max); i++) {
            builder.addId(start + i);
        }
        return builder.build();
    }

    // reset the simulation
    void reset(final SimulationProtocol.Reset message) {
        log().info("reset()");
        init();
        sender().tell(SimulationProtocol.Resetted.getDefaultInstance(), self());
    }
}
