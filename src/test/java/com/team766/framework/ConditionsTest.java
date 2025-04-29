package com.team766.framework;

import static org.junit.jupiter.api.Assertions.*;

import com.team766.TestCase;
import com.team766.framework.test.FakeMechanism.FakeStatus;
import edu.wpi.first.wpilibj2.command.Command;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;

public class ConditionsTest extends TestCase implements StatusesMixin {
    private static class ValueProxy implements BooleanSupplier {
        boolean value = false;

        public boolean getAsBoolean() {
            return value;
        }
    }

    public record OtherStatus(int currentState) implements Status {}

    private static Command startContext(Consumer<Context> runnable) {
        var context = new ContextImpl(new FunctionalProcedure(Set.of(), runnable));
        context.initialize();
        return context;
    }

    private static boolean step(int numSteps, Command command) {
        for (int i = 0; i < 5; ++i) {
            command.execute();
        }
        return command.isFinished();
    }

    private static void finish(Command command) {
        while (!command.isFinished()) {
            command.execute();
            Thread.yield();
        }
    }

    public StatusBus statusBus = StatusBus.getInstance();

    @Test
    public void testWaitForValue() {
        AtomicReference<Optional<String>> container = new AtomicReference<>(Optional.empty());
        var c =
                startContext(
                        context -> {
                            assertEquals("the value", context.waitForValue(container::get));
                        });
        assertFalse(step(5, c));
        container.set(Optional.of("the value"));
        assertTrue(step(1, c));
    }

    @Test
    public void testWaitForValueOrTimeout() {
        AtomicReference<Optional<String>> container = new AtomicReference<>(Optional.empty());
        finish(
                startContext(
                        context -> {
                            assertEquals(
                                    Optional.empty(),
                                    context.waitForValueOrTimeout(container::get, 0.1));
                        }));
        var c =
                startContext(
                        context -> {
                            assertEquals(
                                    Optional.of("the value"),
                                    context.waitForValueOrTimeout(container::get, 1000.0));
                        });
        assertFalse(step(5, c));
        container.set(Optional.of("the value"));
        assertTrue(step(1, c));
    }

    @Test
    public void testCheckForStatus() {
        assertFalse(checkForStatus(FakeStatus.class));
        statusBus.publishStatus(new FakeStatus(0));
        assertTrue(checkForStatus(FakeStatus.class));
        assertFalse(checkForStatus(OtherStatus.class));
    }

    @Test
    public void testCheckForStatusMatching() {
        assertFalse(checkForStatusMatching(FakeStatus.class, s -> s.currentState() == 1));
        statusBus.publishStatus(new OtherStatus(1));
        assertFalse(checkForStatusMatching(FakeStatus.class, s -> s.currentState() == 1));
        statusBus.publishStatus(new FakeStatus(0));
        assertFalse(checkForStatusMatching(FakeStatus.class, s -> s.currentState() == 1));
        statusBus.publishStatus(new FakeStatus(1));
        assertTrue(checkForStatusMatching(FakeStatus.class, s -> s.currentState() == 1));
    }

    @Test
    public void testCheckForStatusEntryMatching() {
        assertFalse(
                checkForStatusEntryMatching(FakeStatus.class, s -> s.status().currentState() == 1));
        statusBus.publishStatus(new OtherStatus(1));
        assertFalse(
                checkForStatusEntryMatching(FakeStatus.class, s -> s.status().currentState() == 1));
        statusBus.publishStatus(new FakeStatus(0));
        assertFalse(
                checkForStatusEntryMatching(FakeStatus.class, s -> s.status().currentState() == 1));
        statusBus.publishStatus(new FakeStatus(1));
        assertTrue(
                checkForStatusEntryMatching(FakeStatus.class, s -> s.status().currentState() == 1));
    }

    @Test
    public void testWhenStatusMatching() {
        assertFalse(
                whenStatusMatching(FakeStatus.class, s -> s.currentState() == 1).getAsBoolean());
        statusBus.publishStatus(new OtherStatus(1));
        assertFalse(
                whenStatusMatching(FakeStatus.class, s -> s.currentState() == 1).getAsBoolean());
        statusBus.publishStatus(new FakeStatus(0));
        assertFalse(
                whenStatusMatching(FakeStatus.class, s -> s.currentState() == 1).getAsBoolean());
        statusBus.publishStatus(new FakeStatus(1));
        assertTrue(whenStatusMatching(FakeStatus.class, s -> s.currentState() == 1).getAsBoolean());
    }

    @Test
    public void testWaitForStatus() {
        var c =
                startContext(
                        context -> {
                            assertEquals(
                                    new FakeStatus(42), waitForStatus(context, FakeStatus.class));
                        });
        assertFalse(step(5, c));
        statusBus.publishStatus(new OtherStatus(42));
        assertFalse(step(5, c));
        statusBus.publishStatus(new FakeStatus(42));
        assertTrue(step(1, c));
    }

    @Test
    public void testWaitForStatusOrTimeout() {
        finish(
                startContext(
                        context -> {
                            assertEquals(
                                    Optional.empty(),
                                    waitForStatusOrTimeout(context, FakeStatus.class, 0.1));
                        }));
        var c =
                startContext(
                        context -> {
                            assertEquals(
                                    Optional.of(new FakeStatus(42)),
                                    waitForStatusOrTimeout(context, FakeStatus.class, 1000.0));
                        });
        assertFalse(step(5, c));
        statusBus.publishStatus(new OtherStatus(42));
        assertFalse(step(5, c));
        statusBus.publishStatus(new FakeStatus(42));
        assertTrue(step(1, c));
    }

    @Test
    public void testWaitForStatusMatching() {
        var c =
                startContext(
                        context -> {
                            assertEquals(
                                    new FakeStatus(42),
                                    waitForStatusMatching(
                                            context,
                                            FakeStatus.class,
                                            s -> s.currentState() == 42));
                        });
        assertFalse(step(5, c));
        statusBus.publishStatus(new OtherStatus(42));
        assertFalse(step(5, c));
        statusBus.publishStatus(new FakeStatus(0));
        assertFalse(step(5, c));
        statusBus.publishStatus(new FakeStatus(42));
        assertTrue(step(1, c));
    }

    @Test
    public void testWaitForStatusMatchingOrTimeout() {
        finish(
                startContext(
                        context -> {
                            assertEquals(
                                    Optional.empty(),
                                    waitForStatusMatchingOrTimeout(
                                            context,
                                            FakeStatus.class,
                                            s -> s.currentState() == 42,
                                            0.1));
                        }));
        var c =
                startContext(
                        context -> {
                            assertEquals(
                                    Optional.of(new FakeStatus(42)),
                                    waitForStatusMatchingOrTimeout(
                                            context,
                                            FakeStatus.class,
                                            s -> s.currentState() == 42,
                                            1000.0));
                        });
        assertFalse(step(5, c));
        statusBus.publishStatus(new OtherStatus(42));
        assertFalse(step(5, c));
        statusBus.publishStatus(new FakeStatus(0));
        assertFalse(step(5, c));
        statusBus.publishStatus(new FakeStatus(42));
        assertTrue(step(1, c));
    }

    @Test
    public void testToggle() {
        var v = new ValueProxy();

        var t = new Conditions.Toggle(v);

        assertFalse(t.getAsBoolean());
        assertFalse(t.getAsBoolean());

        v.value = true;

        assertTrue(t.getAsBoolean());
        assertTrue(t.getAsBoolean());

        v.value = false;

        assertTrue(t.getAsBoolean());
        assertTrue(t.getAsBoolean());

        v.value = true;

        assertFalse(t.getAsBoolean());
        assertFalse(t.getAsBoolean());

        v.value = false;

        assertFalse(t.getAsBoolean());
        assertFalse(t.getAsBoolean());
    }

    @Test
    public void testTimedLatch() {
        testClock.setTime(1710411240);
        var predicate =
                new BooleanSupplier() {
                    private int counter = 0;

                    public boolean getAsBoolean() {
                        return (counter++) % 4 == 1;
                    }
                };

        var timedLatch = new Conditions.TimedLatch(predicate, 2.0);

        assertFalse(timedLatch.getAsBoolean()); // 0->false
        assertTrue(timedLatch.getAsBoolean()); // 1->true
        testClock.tick(1.0);
        assertTrue(timedLatch.getAsBoolean()); // 2->false, but still within 2.0s
        testClock.tick(0.5);
        assertTrue(timedLatch.getAsBoolean()); // 3->false, but still within 2.0s
        testClock.tick(0.6);
        assertFalse(timedLatch.getAsBoolean()); // 4->false, 2.1s elapsed
        testClock.tick(1.0);
        assertTrue(timedLatch.getAsBoolean()); // 1->true
        // TODO: add test to ensure clock resets whenever underlying predicate is true
    }

    @Test
    public void testLogicalAnd() {
        BooleanSupplier predicateFalse = () -> false;
        BooleanSupplier predicateTrue = () -> true;

        assertTrue(new Conditions.LogicalAnd());
        assertTrue(new Conditions.LogicalAnd(predicateTrue));
        assertFalse(new Conditions.LogicalAnd(predicateFalse));
        assertFalse(new Conditions.LogicalAnd(predicateFalse, predicateFalse));
        assertTrue(new Conditions.LogicalAnd(predicateTrue, predicateTrue));
        assertFalse(new Conditions.LogicalAnd(predicateTrue, predicateTrue, predicateFalse));
        assertTrue(new Conditions.LogicalAnd(predicateTrue, predicateTrue, predicateTrue));
    }
}
