package com.ximedes.sva.frontend.message;

import lombok.Value;

/**
 * Created by mawi on 19/07/2016.
 */
@Value
public class Transfer {
    String transferId;
    String to;
    String from;
    Integer amount;
}
