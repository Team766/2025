package com.team766.framework3;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public interface StatusesMixin {
    /**
     * @see StatusBus#publishStatus(Record)
     */
    default <S extends Record & Status> StatusBus.Entry<S> publishStatus(S status) {
        return StatusBus.getInstance().publishStatus(status);
    }

    /**
     * @see StatusBus#getStatusEntry(Class)
     */
    default <S extends Status> Optional<StatusBus.Entry<S>> getStatusEntry(Class<S> statusClass) {
        return StatusBus.getInstance().getStatusEntry(statusClass);
    }

    /**
     * @see StatusBus#getStatus(Class)
     */
    default <S extends Status> Optional<S> getStatus(Class<S> statusClass) {
        return StatusBus.getInstance().getStatus(statusClass);
    }

    /**
     * @see StatusBus#getStatusOrThrow(Class)
     */
    default <S extends Status> S getStatusOrThrow(Class<S> statusClass) {
        return StatusBus.getInstance().getStatusOrThrow(statusClass);
    }

    /**
     * @see StatusBus#getStatusValue(Class, Function)
     */
    default <S extends Status, V> Optional<V> getStatusValue(
            Class<S> statusClass, Function<S, V> getter) {
        return StatusBus.getInstance().getStatusValue(statusClass, getter);
    }

    /**
     * Predicate that checks whether or not a {@link Status} with the given class has been published
     */
    default <S extends Status> boolean checkForStatus(Class<S> statusClass) {
        return getStatusEntry(statusClass).isPresent();
    }

    /**
     * Predicate that checks whether or not the latest {@link Status} with the given class passes
     * the check provided by {@code predicate}.
     */
    default <S extends Status> boolean checkForStatusMatching(
            Class<S> statusClass, Predicate<S> predicate) {
        return getStatusValue(statusClass, predicate::test).orElse(false);
    }

    /**
     * Predicate that checks whether or not the latest {@link Status} with the given class passes
     * the check provided by {@code predicate}, including additional metadata about how the Status
     * was published.
     */
    default <S extends Status> boolean checkForStatusEntryMatching(
            Class<S> statusClass, Predicate<StatusBus.Entry<S>> predicate) {
        return getStatusEntry(statusClass).map(predicate::test).orElse(false);
    }

    /**
     * Suspend the Procedure until a {@link Status} with the given class has been published, then
     * return that Status.
     */
    default <S extends Status> S waitForStatus(Context context, Class<S> statusClass) {
        return context.waitForValue(() -> getStatus(statusClass));
    }

    /**
     * Suspend the Procedure until a {@link Status} with the given class has been published, or
     * we've waited for at least {@code timeoutSeconds}. Returns an Optional containing the Status
     * if one was published, or return an empty Optional if the timeout was reached.
     */
    default <S extends Status> Optional<S> waitForStatusOrTimeout(
            Context context, Class<S> statusClass, double timeoutSeconds) {
        return context.waitForValueOrTimeout(() -> getStatus(statusClass), timeoutSeconds);
    }

    /**
     * Suspend the Procedure until a {@link Status} with the given class has been published that
     * passes the check provided by {@code predicate}, then return that Status.
     */
    default <S extends Status> S waitForStatusMatching(
            Context context, Class<S> statusClass, Predicate<S> predicate) {
        return context.waitForValue(() -> getStatus(statusClass).filter(predicate));
    }

    /**
     * Suspend the Procedure until a {@link Status} with the given class has been published that
     * passes the check provided by {@code predicate}, or we've waited for at least
     * {@code timeoutSeconds}. Returns an Optional containing the Status if one was published, or
     * return an empty Optional if the timeout was reached.
     */
    default <S extends Status> Optional<S> waitForStatusMatchingOrTimeout(
            Context context, Class<S> statusClass, Predicate<S> predicate, double timeoutSeconds) {
        return context.waitForValueOrTimeout(
                () -> getStatus(statusClass).filter(predicate), timeoutSeconds);
    }
}
