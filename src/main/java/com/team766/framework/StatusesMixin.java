package com.team766.framework;

import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;

public interface StatusesMixin {
    /**
     * Alias for {@link StatusBus#publishStatus(Record)}
     */
    default <S extends Record & Status> StatusBus.Entry<S> publishStatus(S status) {
        return StatusBus.getInstance().publishStatus(status);
    }

    /**
     * Alias for {@link StatusBus#getStatusEntry(Class)}
     */
    default <S extends Status> Optional<StatusBus.Entry<S>> getStatusEntry(Class<S> statusClass) {
        return StatusBus.getInstance().getStatusEntry(statusClass);
    }

    /**
     * Alias for {@link StatusBus#getStatus(Class)}
     */
    default <S extends Status> Optional<S> getStatus(Class<S> statusClass) {
        return StatusBus.getInstance().getStatus(statusClass);
    }

    /**
     * Alias for {@link StatusBus#getStatusOrThrow(Class)}
     */
    default <S extends Status> S getStatusOrThrow(Class<S> statusClass) {
        return StatusBus.getInstance().getStatusOrThrow(statusClass);
    }

    /**
     * Alias for {@link StatusBus#getStatusValue(Class, Function)}
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

    default <S extends Status> boolean checkForRecentStatus(
            Class<S> statusClass, double maxAgeSeconds) {
        return getStatusEntry(statusClass).map(e -> e.age() < maxAgeSeconds).orElse(false);
    }

    default <S extends Status> boolean checkForRecentStatusMatching(
            Class<S> statusClass, double maxAgeSeconds, Predicate<S> predicate) {
        return getStatusEntry(statusClass)
                .map(e -> e.age() < maxAgeSeconds && predicate.test(e.status()))
                .orElse(false);
    }

    default <S extends Status> BooleanSupplier whenStatus(Class<S> statusClass) {
        return () -> checkForStatus(statusClass);
    }

    default <S extends Status> BooleanSupplier whenStatusMatching(
            Class<S> statusClass, Predicate<S> predicate) {
        return () -> checkForStatusMatching(statusClass, predicate);
    }

    default <S extends Status> BooleanSupplier whenRecentStatusMatching(
            Class<S> statusClass, double maxAgeSeconds, Predicate<S> predicate) {
        return () -> checkForRecentStatusMatching(statusClass, maxAgeSeconds, predicate);
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
