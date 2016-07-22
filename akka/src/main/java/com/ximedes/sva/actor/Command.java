package com.ximedes.sva.actor;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.time.LocalDateTime;

/**
 * Created by mawi on 20/07/2016.
 */
public class Command {
    private final LocalDateTime received;

    public Command() {
        received = LocalDateTime.now();
    }

    public Command(final LocalDateTime received) {
        this.received = received;
    }

    public LocalDateTime getReceived() {
        return received;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Command command = (Command) o;

        return new EqualsBuilder()
                .append(received, command.received)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(received).toHashCode();
    }
}

