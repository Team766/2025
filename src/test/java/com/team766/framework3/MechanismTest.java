package com.team766.framework3;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.team766.TestCase3;
import com.team766.framework3.FakeMechanism.FakeStatus;
import edu.wpi.first.wpilibj2.command.Command;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

public class MechanismTest extends TestCase3 {
    /// Test that checkContextReservation succeeds when called from a Procedure which reserves
    /// the Mechanism.
    @Test
    public void testSuccessCheckContextReservationInProcedure() {
        var mech = new FakeMechanism();

        var succeeded = new AtomicBoolean(false);
        var cmd =
                new ContextImpl(
                        new FunctionalProcedure(
                                Set.of(mech),
                                context -> {
                                    mech.mutateMechanism(0);

                                    succeeded.set(true);
                                }));
        cmd.schedule();

        step();
        assertTrue(cmd.isFinished());
        assertTrue(succeeded.get());
    }

    /// Test a Mechanism publishing a Status via its run() method return value.
    @Test
    public void testStatuses() {
        // FakeMechanism publishes a FakeStatus with the state value which was most recently set
        // via its mutateMechanism() method.
        @SuppressWarnings("unused")
        var mech = new FakeMechanism() {};
        step();
        // Status set from initial state.
        assertEquals(
                new FakeStatus(-1), StatusBus.getInstance().getStatusOrThrow(FakeStatus.class));
        step();
        // Status set from onMechanismIdle
        assertEquals(
                new FakeStatus(10), StatusBus.getInstance().getStatusOrThrow(FakeStatus.class));
    }

    /// Test that checkContextReservation throws an exception when called from a Procedure which has
    /// not reserved the Mechanism.
    @Test
    public void testFailedCheckContextReservationInProcedure() {
        var mech = new FakeMechanism();

        var thrownException = new AtomicReference<String>(null);
        var cmd =
                new ContextImpl(
                        new FunctionalProcedure(
                                Set.of(),
                                context -> {
                                    try {
                                        mech.mutateMechanism(0);
                                    } catch (Throwable ex) {
                                        thrownException.set(ex.getMessage());
                                    }
                                }));
        cmd.schedule();
        step();
        assertThat(thrownException.get())
                .matches("FakeMechanism tried to be used without reserving it");

        var cmd2 = new ContextImpl(new FakeProcedure(1, Set.of(mech)));
        cmd2.schedule();
        thrownException.set(null);
        cmd.schedule();
        step();
        assertThat(thrownException.get())
                .matches("FakeMechanism tried to be used without reserving it");
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
                    protected void run() {
                        try {
                            checkContextReservation();
                        } catch (Throwable ex) {
                            thrownException.set(ex);
                        }
                    }
                };
        step();
        assertNull(thrownException.get());
    }

    /// Test that the Idle callback runs if no other Command reserves this Mechanism.
    @Test
    public void testIdleCallback() {
        var mech =
                new FakeMechanism() {
                    Boolean isIdle = null;

                    public void useMechanism() {
                        isIdle = false;
                    }

                    @Override
                    protected void onMechanismIdle() {
                        isIdle = true;
                    }
                };

        assertNull(mech.isIdle);

        // Mechanism starts unreserved, so the Idle callback should run the Idle callback.
        step();
        assertEquals(Boolean.valueOf(true), mech.isIdle);
        // Subsequent steps should not call the Idle callback, even though the mechanism is
        // still idle.
        mech.isIdle = null;
        step();
        assertNull(mech.isIdle);

        // Test a Command is scheduled which reserves this Procedure.
        Command command =
                new FunctionalProcedure(
                                Set.of(mech),
                                context -> {
                                    mech.useMechanism();
                                    context.waitFor(() -> false);
                                })
                        .createCommandToRunProcedure();
        command.schedule();
        step();
        step();
        assertEquals(false, mech.isIdle);
        // Subsequent steps should allow the scheduled Command to continue. The Idle callback
        // should not be run, even if the scheduled Command does not call any methods on the
        // Mechanism.
        step();
        assertEquals(false, mech.isIdle);

        command.cancel();
        // After the reserving command is stopped, the Idle callback should be called again.
        step();
        assertEquals(true, mech.isIdle);
    }
}
