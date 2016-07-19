package com.ximedes.sva.akka.api.message;

import lombok.Value;

/**
 * Created by mawi on 19/07/2016.
 */
@Value
public class Transfer {
    String to;
    String from;
    Integer amount;
}
