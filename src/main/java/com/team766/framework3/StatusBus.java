package com.team766.framework3;

import com.team766.hal.RobotProvider;
import com.team766.logging.Category;
import com.team766.logging.Logger;
import com.team766.logging.Severity;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;

/**
 * Bus for broadcasting and querying {@link Status} of different parts of the robot (eg state of a Mechanism).
 * This is a Singleton.  Producers can call {@link #publish} to publish their latest {@link Status}.
 * Consumers can call {@link #getStatus(Class)} with the class Object (eg, {@code MyStatus.class})
 * for the {@link Status} they are interested in querying, to get the latest published {@link Status}.
 */
public class StatusBus {

    private static StatusBus s_instance = new StatusBus();
    private final Map<Class<? extends StatusHandle<?>>, Entry<?>> statuses = new LinkedHashMap<>();

    /**
     * Get the Singleton instance of the {@link StatusBus}.
     */
    public static StatusBus getInstance() {
        return s_instance;
    }

    /**
     * @param status The {@link Status} that was published
     * @param timestamp The time at which the Status was published
     */
    public record Entry<T extends Status>(T status, double timestamp) {
        public double age() {
            return RobotProvider.instance.getClock().getTime() - timestamp;
        }
    }

    /**
     * Remove all published @{link Status}es from the StatusBus.
     */
    public void clear() {
        statuses.clear();
    }

    /**
     * Publish a new {@link Status} for the given specific class of {@link Status}. Each producer will
     * create their own implementation of the {@link Status} interface to contain its state information.
     *
     * This method also logs the Status to diagnostic logs.
     */
    public <H extends StatusHandle<S>, S extends Record & Status> Entry<S> publishStatus(
            Class<H> handle, S status) {
        var entry = new Entry<>(status, RobotProvider.instance.getClock().getTime());
        statuses.put(handle, entry);
        // TODO(MF3): also publish to data logs
        Logger.get(Category.FRAMEWORK)
                .logRaw(
                        Severity.INFO,
                        "StatusBus received Status ("
                                + status.getClass().getName()
                                + "): "
                                + status);
        return entry;
    }

    /**
     * Gets the latest published {@link Status} for the given class of {@link Status}, along with
     * additional metadata about how the Status was published. Each producer will create their own
     * implementation of the {@link Status} interface to contain its state information. Each
     * consumer will need to know the {@link Status} class a priori, in order to query it.
     *
     * @param <S> The specific {@link Status} class of interest.
     * @param statusClass The Class object for the Status, eg {@code MyStatus.class}.
     * @return
     *     A {@link StatusBus.Entry} containing the latest published {@link Status}, or an empty
     *     Optional if the {@link Status} hasn't been published.
     */
    @SuppressWarnings("unchecked")
    public <H extends StatusHandle<S>, S extends Status> Optional<Entry<S>> getStatusEntry(
            Class<H> handle) {
        return Optional.ofNullable((Entry<S>) statuses.get(handle));
    }

    /**
     * Gets the latest published {@link Status} for the given class of {@link Status}. Each producer
     * will create their own implementation of the {@link Status} interface to contain its state
     * information. Each consumer will need to know the {@link Status} class a priori, in order to
     * query it.
     *
     * @param <S> The specific {@link Status} class of interest.
     * @param statusClass The Class object for the Status, eg {@code MyStatus.class}.
     * @return
     *     The latest published {@link Status} or an empty Optional if the {@link Status} hasn't
     *     been published.
     */
    public <H extends StatusHandle<S>, S extends Status> Optional<S> getStatus(Class<H> handle) {
        return getStatusEntry(handle).map(Entry<S>::status);
    }

    /**
     * Gets the latest published {@link Status} for the given class of {@link Status}. Each producer
     * will create their own implementation of the {@link Status} interface to contain its state
     * information. Each consumer will need to know the {@link Status} class a priori, in order to
     * query it.
     *
     * @param <S> The specific {@link Status} class of interest.
     * @param statusClass The Class object for the Status, eg {@code MyStatus.class}.
     * @return The latest published {@link Status}
     * @throws NoSuchElementException if the {@link Status} hasn't been published
     */
    public <H extends StatusHandle<S>, S extends Status> S getStatusOrThrow(Class<H> handle) {
        return getStatus(handle).orElseThrow();
    }

    /**
     * Gets the latest published {@link Status} for the given class of {@link Status}, then applies
     * the given function to the Status and returns the result. Each producer will create their own
     * implementation of the {@link Status} interface to contain its state information. Each
     * consumer will need to know the {@link Status} class a priori, in order to query it.
     *
     * @param <S> The specific {@link Status} class of interest.
     * @param <V> The return type of the Function
     * @param statusClass The Class object for the Status, eg {@code MyStatus.class}.
     * @return
     *     The result of the Function applied to the latest published {@link Status} or an empty
     *     Optional if the {@link Status} hasn't been published.
     */
    public <H extends StatusHandle<S>, S extends Status, V> Optional<V> getStatusValue(
            Class<H> handle, Function<S, V> getter) {
        return getStatusEntry(handle).map(s -> getter.apply(s.status()));
    }
}
