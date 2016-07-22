package com.ximedes.sva.actor;

import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Creator;
import akka.japi.pf.ReceiveBuilder;
import akka.persistence.AbstractPersistentActor;
import lombok.EqualsAndHashCode;
import lombok.Value;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;

/**
 * Created by mawi on 19/07/2016.
 */
public class AccountActor extends AbstractPersistentActor {

    final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private final String accountId;
    private Integer balance;
    private final Integer overdraft;

    public static Props props(final String accountId, final Integer balance, final Integer overdraft) {
        return Props.create((Creator<AccountActor>) () -> new AccountActor(accountId, balance, overdraft));
    }

    AccountActor(final String accountId, Integer balance, Integer overdraft) {
        this.accountId = accountId;
        this.balance = balance;
        this.overdraft = overdraft;
    }

    void debitRequest(final DebitRequest command) {
        sender().tell(new Confirmed(command.amount), self());
    }

    void creditRequest(final CreditRequest command) {
        if (balance - command.amount + overdraft >= 0) {
            sender().tell(new Confirmed(command.amount), self());
        } else {
            sender().tell(new InsufficientFunds(command.amount), self());
        }
    }

    void debitConfirmed(final DebitConfirmed command) {
        balance += command.amount;
        sender().tell(new Confirmed(command.amount), self());
    }

    void creditConfirmed(final CreditConfirmed command) {
        balance -= command.amount;
        sender().tell(new Confirmed(command.amount), self());
    }

    void query(final Query command) {
        sender().tell(new QueryResult(accountId, balance, overdraft), self());
    }

    @Override
    public PartialFunction<Object, BoxedUnit> receiveRecover() {
        return null;
    }

    @Override
    public PartialFunction<Object, BoxedUnit> receiveCommand() {
        return ReceiveBuilder
                .match(DebitRequest.class, this::debitRequest)
                .match(CreditRequest.class, this::creditRequest)
                .match(DebitConfirmed.class, this::debitConfirmed)
                .match(CreditConfirmed.class, this::creditConfirmed)
                .match(Query.class, this::query)
                .matchAny(o -> log.warning("received unknown message: {}", o))
                .build();
    }

    @Override
    public String persistenceId() {
        return accountId;
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static final class Query extends Command {
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static final class QueryResult extends Event {
        String accountId;
        Integer balance;
        Integer overdraft;
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static final class DebitRequest extends Command {
        Integer amount;
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static final class CreditRequest extends Command {
        Integer amount;
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static final class DebitConfirmed extends Command {
        Integer amount;
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static final class CreditConfirmed extends Command {
        Integer amount;
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static final class Confirmed extends Event {
        Integer amount;
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static final class InsufficientFunds extends Event {
        Integer amount;
    }
}
