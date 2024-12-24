package com.team766.framework3;

import static com.team766.framework3.Conditions.waitForValue;
import static com.team766.framework3.Conditions.waitForValueOrTimeout;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public interface StatusesMixin {
    /**
     * @see StatusBus#getStatusEntry(Class)
     */
    default <H extends StatusHandle<S>, S extends Status>
            Optional<StatusBus.Entry<S>> getStatusEntry(Class<H> handle) {
        return StatusBus.getInstance().getStatusEntry(handle);
    }

    /**
     * @see StatusBus#getStatus(Class)
     */
    default <H extends StatusHandle<S>, S extends Status> Optional<S> getStatus(Class<H> handle) {
        return StatusBus.getInstance().getStatus(handle);
    }

    /**
     * @see StatusBus#getStatusOrThrow(Class)
     */
    default <H extends StatusHandle<S>, S extends Status> S getStatusOrThrow(Class<H> handle) {
        return StatusBus.getInstance().getStatusOrThrow(handle);
    }

    /**
     * @see StatusBus#getStatusValue(Class, Function)
     */
    default <H extends StatusHandle<S>, S extends Status, V> Optional<V> getStatusValue(
            Class<H> handle, Function<S, V> getter) {
        return StatusBus.getInstance().getStatusValue(handle, getter);
    }

    /**
     * Predicate that checks whether or not a {@link Status} with the given class has been published
     */
    default <H extends StatusHandle<S>, S extends Status> boolean checkForStatus(Class<H> handle) {
        return getStatusEntry(handle).isPresent();
    }

    /**
     * Predicate that checks whether or not the latest {@link Status} with the given class passes
     * the check provided by {@code predicate}.
     */
    default <H extends StatusHandle<S>, S extends Status> boolean checkForStatusWith(
            Class<H> handle, Predicate<S> predicate) {
        return getStatusValue(handle, predicate::test).orElse(false);
    }

    /**
     * Predicate that checks whether or not the latest {@link Status} with the given class passes
     * the check provided by {@code predicate}, including additional metadata about how the Status
     * was published.
     */
    default <H extends StatusHandle<S>, S extends Status> boolean checkForStatusEntryWith(
            Class<H> handle, Predicate<StatusBus.Entry<S>> predicate) {
        return getStatusEntry(handle).map(predicate::test).orElse(false);
    }

    /**
     * Suspend the Procedure until a {@link Status} with the given class has been published, then
     * return that Status.
     */
    default <H extends StatusHandle<S>, S extends Status> S waitForStatus(
            Context context, Class<H> handle) {
        return waitForValue(context, () -> getStatus(handle));
    }

    /**
     * Suspend the Procedure until a {@link Status} with the given class has been published, or
     * we've waited for at least {@code timeoutSeconds}. Returns an Optional containing the Status
     * if one was published, or return an empty Optional if the timeout was reached.
     */
    default <H extends StatusHandle<S>, S extends Status> Optional<S> waitForStatusOrTimeout(
            Context context, Class<H> handle, double timeoutSeconds) {
        return waitForValueOrTimeout(context, () -> getStatus(handle), timeoutSeconds);
    }

    /**
     * Suspend the Procedure until a {@link Status} with the given class has been published that
     * passes the check provided by {@code predicate}, then return that Status.
     */
    default <H extends StatusHandle<S>, S extends Status> S waitForStatusWith(
            Context context, Class<H> handle, Predicate<S> predicate) {
        return waitForValue(context, () -> getStatus(handle).filter(predicate));
    }

    /**
     * Suspend the Procedure until a {@link Status} with the given class has been published that
     * passes the check provided by {@code predicate}, or we've waited for at least
     * {@code timeoutSeconds}. Returns an Optional containing the Status if one was published, or
     * return an empty Optional if the timeout was reached.
     */
    default <H extends StatusHandle<S>, S extends Status> Optional<S> waitForStatusWithOrTimeout(
            Context context, Class<H> handle, Predicate<S> predicate, double timeoutSeconds) {
        return waitForValueOrTimeout(
                context, () -> getStatus(handle).filter(predicate), timeoutSeconds);
    }
}
