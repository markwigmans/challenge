package com.ximedes.sva.frontend.message;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Value;
import lombok.experimental.Builder;

/**
 * Created by mawi on 19/07/2016.
 */
@JsonDeserialize(builder = Account.AccountBuilder.class)
@Builder
@Value
public class Account {
    Integer accountId;
    Integer balance;
    Integer overdraft;

    @JsonPOJOBuilder(withPrefix = "")
    public static final class AccountBuilder {
    }
}
