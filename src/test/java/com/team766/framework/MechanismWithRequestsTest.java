package com.team766.framework;

import static org.junit.jupiter.api.Assertions.*;

import com.team766.TestCase;
import com.team766.framework.test.FakeMechanismWithRequests;
import com.team766.framework.test.FakeMechanismWithRequests.FakeRequest;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class MechanismWithRequestsTest extends TestCase {
    /// Test sending requests to a Mechanism. Also test that checkContextReservation succeeds when
    /// called from a Procedure which reserves the Mechanism.
    @Test
    public void testRequests() {
        var mech = new FakeMechanismWithRequests();

        var cmd =
                new ContextImpl(
                        new FunctionalProcedure(
                                Set.of(mech),
                                context -> {
                                    // Step 1
                                    context.yield();

                                    // Step 2
                                    mech.setRequest(new FakeRequest(0));
                                    context.yield();

                                    // Step 3
                                    context.yield();

                                    // Step 4
                                    mech.setRequest(new FakeRequest(1));
                                    context.yield();
                                }));
        cmd.schedule();

        // Step 0. The CommandScheduler runs Subsystems (Mechanisms) before Commands (Procedures),
        // but this test is written as if the Procedure steps first. Thus we add a "Step 0" here
        // but not in the Procedure to readjust the relationship between these sequences of events.
        step();

        // Step 1. Test running the Mechanism in its uninitialized state.
        step();
        assertEquals(FakeMechanismWithRequests.INITIAL_REQUEST, mech.currentRequest);
        assertFalse(mech.wasRequestNew);

        // Step 2. The Mechanism receives the first request.
        step();
        assertEquals(new FakeRequest(0), mech.currentRequest);
        assertTrue(mech.wasRequestNew);

        // Step 3. The Mechanism continues with its first request.
        step();
        assertEquals(new FakeRequest(0), mech.currentRequest);
        assertFalse(mech.wasRequestNew);

        // Step 4. The Mechanism receives the second request.
        step();
        assertEquals(new FakeRequest(1), mech.currentRequest);
        assertTrue(mech.wasRequestNew);

        // Poke the Procedure to ensure it has finished.
        step();
        assertTrue(cmd.isFinished());
    }

    /// Test that the Initial request runs after Mechanism creation.
    @Test
    public void testInitialRequest() {
        var mech = new FakeMechanismWithRequests();
        step();
        assertEquals(FakeMechanismWithRequests.INITIAL_REQUEST, mech.currentRequest);
        assertTrue(mech.wasRequestNew);
        // Subsequent steps should continue to pass the Initial request to run(),
        // but it should not be indicated as a new request.
        mech.currentRequest = FakeMechanismWithRequests.NULL_REQUEST;
        step();
        assertEquals(FakeMechanismWithRequests.INITIAL_REQUEST, mech.currentRequest);
        assertFalse(mech.wasRequestNew);
    }

    /// Test that the Idle request runs if no other Command reserves this Mechanism.
    @Test
    public void testIdleRequest() {
        var mech =
                new FakeMechanismWithRequests() {
                    @Override
                    protected FakeRequest getIdleRequest() {
                        return new FakeRequest(0);
                    }
                };

        // The first step should run the Initial request.
        step();
        assertEquals(FakeMechanismWithRequests.INITIAL_REQUEST, mech.currentRequest);
        assertTrue(mech.wasRequestNew);
        // On subsequent steps, the Idle request should take over (as long as a Command hasn't
        // reserved this Mechanism).
        step();
        assertEquals(new FakeRequest(0), mech.currentRequest);
        assertTrue(mech.wasRequestNew);
        // Subsequent steps should continue to pass the Idle request to run(),
        // but it should not be indicated as a new request.
        mech.currentRequest = FakeMechanismWithRequests.NULL_REQUEST;
        step();
        assertEquals(new FakeRequest(0), mech.currentRequest);
        assertFalse(mech.wasRequestNew);

        // When a Command is scheduled which reserves this Procedure, it should preempt
        // the Idle request.
        new FunctionalProcedure(
                        Set.of(mech),
                        context -> {
                            mech.setRequest(new FakeRequest(1));
                            context.waitFor(() -> false);
                        })
                .createCommandToRunProcedure()
                .schedule();
        step();
        step(); // NOTE: Second step() is needed because Scheduler runs Procedures after Subsystems
        assertEquals(new FakeRequest(1), mech.currentRequest);
        assertTrue(mech.wasRequestNew);
        // Subsequent steps should allow the scheduled Command to continue. It should not be
        // interrupted by the Idle request, even if the scheduled Command does not set a
        // new request.
        mech.currentRequest = FakeMechanismWithRequests.NULL_REQUEST;
        step();
        assertEquals(new FakeRequest(1), mech.currentRequest);
        assertFalse(mech.wasRequestNew);
    }
}
