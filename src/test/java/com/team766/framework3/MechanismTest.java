package com.team766.framework3;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.team766.TestCase3;
import com.team766.framework3.FakeMechanism.FakeStatus;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

public class MechanismTest extends TestCase3 {
    /// Test sending requests to a Mechanism. Also test that checkContextReservation succeeds when
    /// called from a Procedure which reserves the Mechanism.
    @Test
    public void testRequests() {
        var mech = new FakeMechanism();

        var cmd =
                new ContextImpl(
                        new FunctionalProcedure(
                                Set.of(mech),
                                context -> {
                                    // Step 1
                                    context.yield();

                                    // Step 2
                                    mech.requestFakeState(0);
                                    context.yield();

                                    // Step 3
                                    context.yield();

                                    // Step 4
                                    mech.requestFakeState(1);
                                    context.yield();
                                }));
        cmd.schedule();

        // Step 0. The CommandScheduler runs Subsystems (Mechanisms) before Commands (Procedures),
        // but this test is written as if the Procedure steps first. Thus we add a "Step 0" here
        // but not in the Procedure to readjust the relationship between these sequences of events.
        step();

        // Step 1. Test running the Mechanism in its uninitialized state.
        step();
        assertEquals(-1, mech.currentState);
        assertFalse(mech.wasRequestNew);

        // Step 2. The Mechanism receives the first request.
        step();
        assertEquals(0, mech.currentState);
        assertTrue(mech.wasRequestNew);

        // Step 3. The Mechanism continues with its first request.
        step();
        assertEquals(0, mech.currentState);
        assertFalse(mech.wasRequestNew);

        // Step 4. The Mechanism receives the second request.
        step();
        assertEquals(1, mech.currentState);
        assertTrue(mech.wasRequestNew);

        // Poke the Procedure to ensure it has finished.
        step();
        assertTrue(cmd.isFinished());
    }

    /// Test a Mechanism publishing a Status via its run() method return value.
    @Test
    public void testStatuses() {
        // FakeMechanism publishes a FakeStatus with the state value which was most recently set in
        // its Request.
        var mech =
                new FakeMechanism() {
                    @Override
                    protected Request<FakeMechanism> startIdleRequest() {
                        return requestFakeState(10);
                    }
                };
        step();
        // Status set from Initial request
        assertEquals(
                new FakeStatus(-1), StatusBus.getInstance().getStatusOrThrow(FakeStatus.class));
        assertEquals(new FakeStatus(-1), mech.getStatus());
        step();
        // Status set from Idle request
        assertEquals(
                new FakeStatus(10), StatusBus.getInstance().getStatusOrThrow(FakeStatus.class));
        assertEquals(new FakeStatus(10), mech.getStatus());
    }

    /// Test that checkContextReservation throws an exception when called from a Procedure which has
    /// not reserved the Mechanism.
    @Test
    public void testFailedCheckContextReservationInProcedure() {
        class DummyMechanism extends MechanismWithStatus<FakeStatus> {
            private int currentState;

            public DummyMechanism() {
                // Initial request
                requestState(-1);
            }

            public Request<DummyMechanism> requestState(int state) {
                currentState = state;
                return startRequest(() -> true);
            }

            @Override
            protected FakeStatus reportStatus() {
                return new FakeStatus(currentState);
            }
        }
        var mech = new DummyMechanism();

        var thrownException = new AtomicReference<String>(null);
        var cmd =
                new ContextImpl(
                        new FunctionalProcedure(
                                Set.of(),
                                context -> {
                                    try {
                                        mech.requestState(0);
                                    } catch (Throwable ex) {
                                        thrownException.set(ex.getMessage());
                                    }
                                }));
        cmd.schedule();
        step();
        assertThat(thrownException.get())
                .matches("DummyMechanism tried to be used without reserving it");

        var cmd2 = new ContextImpl(new FakeProcedure(1, Set.of(mech)));
        cmd2.schedule();
        cmd.schedule();
        step();
        assertThat(thrownException.get())
                .matches("DummyMechanism tried to be used without reserving it");
    }

    /// Test that checkContextReservation succeeds when called from within the Mechanism's own run()
    /// method.
    @Test
    public void testCheckContextReservationInRun() {
        var thrownException = new AtomicReference<Throwable>();
        @SuppressWarnings("unused")
        var mech =
                new FakeMechanism() {
                    @Override
                    public Request<FakeMechanism> requestFakeState(int targetState) {
                        try {
                            return super.requestFakeState(targetState);
                        } catch (Throwable ex) {
                            thrownException.set(ex);
                            throw ex;
                        }
                    }
                };
        step();
        assertNull(thrownException.get());
    }

    /// Test that the Initial request runs after Mechanism creation.
    @Test
    public void testInitialRequest() {
        var mech = new FakeMechanism();
        step();
        assertEquals(-1, mech.currentState);
        assertTrue(mech.wasRequestNew);
        // Subsequent steps should continue to pass the Initial request to run(),
        // but it should not be indicated as a new request.
        mech.currentState = null;
        step();
        assertEquals(-1, mech.currentState);
        assertFalse(mech.wasRequestNew);
    }

    /// Test that the Idle request runs if no other Command reserves this Mechanism.
    @Test
    public void testIdleRequest() {
        var mech =
                new FakeMechanism() {
                    @Override
                    protected Request<FakeMechanism> startIdleRequest() {
                        return requestFakeState(0);
                    }
                };

        // The first step should run the Initial request.
        step();
        assertEquals(-1, mech.currentState);
        assertTrue(mech.wasRequestNew);
        // On subsequent steps, the Idle request should take over (as long as a Command hasn't
        // reserved this Mechanism).
        step();
        assertEquals(0, mech.currentState);
        assertTrue(mech.wasRequestNew);
        // Subsequent steps should continue to pass the Idle request to run(),
        // but it should not be indicated as a new request.
        mech.currentState = null;
        step();
        assertEquals(0, mech.currentState);
        assertFalse(mech.wasRequestNew);

        // When a Command is scheduled which reserves this Procedure, it should preempt
        // the Idle request.
        new FunctionalProcedure(
                        Set.of(mech),
                        context -> {
                            mech.requestFakeState(1);
                            context.waitFor(() -> false);
                        })
                .createCommandToRunProcedure()
                .schedule();
        step();
        step(); // NOTE: Second step() is needed because Scheduler runs Procedures after Subsystems
        assertEquals(1, mech.currentState);
        assertTrue(mech.wasRequestNew);
        // Subsequent steps should allow the scheduled Command to continue. It should not be
        // interrupted by the Idle request, even if the scheduled Command does not set a
        // new request.
        mech.currentState = null;
        step();
        assertEquals(1, mech.currentState);
        assertFalse(mech.wasRequestNew);
    }

    /// Test making a Mechanism part of a superstructure.
    @Test
    public void testSuperstructure() {
        class TestSuperstructure extends Superstructure<FakeStatus> {
            // NOTE: Real superstructures should have their members be private. This is public
            // to test handling of bad code patterns, and to allow us to inspect the state of the
            // inner mechanism for purposes of testing the framework.
            public final FakeMechanism submechanism;

            public TestSuperstructure() {
                submechanism = addMechanism(new FakeMechanism());

                // Initial request
                requestFakeState(0);
            }

            public Request<TestSuperstructure> requestFakeState(int targetState) {
                currentSuperState = targetState;
                if (targetState == 0) {
                    submechanism.requestFakeState(2);
                } else {
                    submechanism.requestFakeState(4);
                }
                return startRequest(() -> true);
            }

            private int currentSuperState;

            @Override
            protected FakeStatus reportStatus() {
                return new FakeStatus(currentSuperState);
            }
        }
        var superstructure = new TestSuperstructure();

        step();
        // Sub-mechanisms should run their periodic() method before the superstructure's periodic(),
        // so we will see the sub-mechanism's initial request after the first step.
        assertEquals(-1, superstructure.submechanism.currentState);

        step();
        // After the second step, the request set by the superstructure on the first step will have
        // propagated to the sub-mechanism.
        assertEquals(2, superstructure.submechanism.currentState);

        // Test error conditions

        assertThrows(
                IllegalStateException.class,
                () -> superstructure.submechanism.requestFakeState(0),
                "is part of a superstructure");

        assertThrows(NullPointerException.class, () -> superstructure.addMechanism(null));

        assertThrows(
                IllegalStateException.class,
                () -> superstructure.addMechanism(superstructure.submechanism),
                "Mechanism is already part of a superstructure");

        assertThrows(
                UnsupportedOperationException.class,
                () ->
                        superstructure.addMechanism(
                                new FakeMechanism() {
                                    protected Request<FakeMechanism> startIdleRequest() {
                                        return requestFakeState(0);
                                    }
                                }),
                "A Mechanism contained in a superstructure cannot define an idle request");
    }
}
