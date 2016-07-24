package com.ximedes.sva.frontend.message;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Value;
import lombok.experimental.Builder;

/**
 * Created by mawi on 19/07/2016.
 */
@JsonDeserialize(builder = Transfer.TransferBuilder.class)
@Builder
@Value
public class Transfer {
    Integer transferId;
    Integer from;
    Integer to;
    Integer amount;

    @JsonPOJOBuilder(withPrefix = "")
    public static final class TransferBuilder {
    }
}
