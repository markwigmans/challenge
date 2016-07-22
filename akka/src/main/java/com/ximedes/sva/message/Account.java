package com.ximedes.sva.message;

import lombok.Value;

/**
 * Created by mawi on 19/07/2016.
 */
@Value
public class Account {
    String accountId;
    Integer balance;
    Integer overdraft;
}
