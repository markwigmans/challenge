package com.ximedes.sva.actor;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.time.LocalDateTime;

/**
 * Created by mawi on 20/07/2016.
 */
public abstract class Event {
    private final LocalDateTime created;

    public Event() {
        created = LocalDateTime.now();
    }

    public Event(final LocalDateTime created) {
        this.created = created;
    }

    public LocalDateTime getCreated() {
        return created;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Event event = (Event) o;

        return new EqualsBuilder()
                .append(created, event.created)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(created).toHashCode();
    }
}

