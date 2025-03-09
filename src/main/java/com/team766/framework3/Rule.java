package com.team766.framework3;

import com.google.common.collect.Maps;
import edu.wpi.first.wpilibj2.command.Subsystem;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import org.littletonrobotics.junction.Logger;

/**
 * Rule to be evaluated in the {@link RuleEngine}.  Rules contain a
 * "predicate" that will be evaluated in each call to {@link RuleEngine#run}, typically
 * in an OperatorInterface loop or Display (LED lights, etc) loop.  The Rule keeps track of
 * when the predicate starts triggering and has finished triggering, via
 * a {@link TriggerType}, eg when a driver or boxop starts pressing a button and then releases the button.
 * Each Rule has optional {@link Procedure} actions for each of these trigger types, which the
 * {@link RuleEngine} will consider running, after checking if higher priority rules have reserved the
 * same {@link Mechanism}s that the candidate rule would use.
 *
 * {@link Rule}s are always created and used with a {@link RuleEngine}.  Typically creation would be:
 *
 * <pre>
 * {@code
 *   public class MyRules extends RuleEngine {
 *     public MyRules() {
 *       // add rule to spin up the shooter when the boxop presses the right trigger on the gamepad
 *       addRule("spin up shooter", gamepad.getButton(InputConstants.XBOX_RT),
 *               () -> new ShooterSpin(shooter)));
 *       ...
 *     }
 * }
 * </pre>
 */
public class Rule {

    /**
     * Rules will be in one of four "trigger" (based on rule predicate) states:
     *
     * NONE - rule is not triggering and was not triggering in the last evaluation.
     * NEWLY - rule just started triggering this evaluation.
     * CONTINUING - rule was triggering in the last evaluation and is still triggering.  Only used internally.
     * FINISHED - rule was triggering in the last evaluation and is no longer triggering.
     *
     */
    enum TriggerType {
        NONE,
        NEWLY,
        CONTINUING,
        FINISHED
    }

    /** Policy for canceling actions when the rule is in a given state. */
    enum Cancellation {
        /** Do not cancel any previous actions. */
        DO_NOT_CANCEL,
        /** Cancel the action previously scheduled when the rule was in the NEWLY state. */
        CANCEL_NEWLY_ACTION,
    }

    private RuleGroupBase container;
    private String name;
    private BooleanSupplier predicate;
    private final Map<TriggerType, Supplier<Procedure>> triggerProcedures =
            Maps.newEnumMap(TriggerType.class);
    private final Map<TriggerType, Set<Subsystem>> triggerReservations =
            Maps.newEnumMap(TriggerType.class);
    private Cancellation cancellationOnFinish;

    private TriggerType currentTriggerType = TriggerType.NONE;
    private boolean sealed = false;

    /* package */ Rule(RuleGroupBase container, String name, BooleanSupplier predicate) {
        if (name == null) {
            throw new IllegalArgumentException("Rule name has not been set.");
        }

        if (name.contains("/")) {
            throw new IllegalArgumentException("Rule name cannot contain a '/' character.");
        }

        if (predicate == null) {
            throw new IllegalArgumentException("Rule predicate has not been set.");
        }

        this.container = container;
        this.name = name;
        this.predicate = predicate;
    }

    public Rule withOnTriggeringProcedure(
            RulePersistence rulePersistence, Supplier<Procedure> onTriggeringProcedure) {
        if (sealed) {
            throw new IllegalStateException(
                    "Cannot modify rules once they've been evaluated in the RuleEngine");
        }
        if (triggerProcedures.containsKey(TriggerType.NEWLY)) {
            throw new IllegalStateException("This trigger already has an OnTriggering action");
        }

        final Supplier<Procedure> newlyTriggeringProcedure =
                switch (rulePersistence) {
                    case ONCE -> {
                        this.cancellationOnFinish = Cancellation.DO_NOT_CANCEL;
                        yield onTriggeringProcedure;
                    }
                    case ONCE_AND_HOLD -> {
                        this.cancellationOnFinish = Cancellation.CANCEL_NEWLY_ACTION;
                        yield () -> {
                            final Procedure procedure = onTriggeringProcedure.get();
                            if (procedure == null) {
                                return null;
                            }
                            return new FunctionalProcedure(
                                    procedure.getName(),
                                    procedure.reservations(),
                                    context -> {
                                        procedure.run(context);
                                        context.waitFor(() -> false);
                                    });
                        };
                    }
                    case REPEATEDLY -> {
                        this.cancellationOnFinish = Cancellation.CANCEL_NEWLY_ACTION;
                        yield () -> {
                            final Procedure procedure = onTriggeringProcedure.get();
                            if (procedure == null) {
                                return null;
                            }
                            return new FunctionalProcedure(
                                    procedure.getName(),
                                    procedure.reservations(),
                                    context -> {
                                        Procedure currentProcedure = procedure;
                                        while (currentProcedure != null) {
                                            context.runSync(currentProcedure);
                                            context.yield();
                                            currentProcedure = onTriggeringProcedure.get();
                                        }
                                    });
                        };
                    }
                };

        triggerProcedures.put(TriggerType.NEWLY, newlyTriggeringProcedure);
        triggerReservations.put(
                TriggerType.NEWLY, getReservationsForProcedure(newlyTriggeringProcedure));

        return this;
    }

    public Rule withOnTriggeringProcedure(
            RulePersistence rulePersistence, Set<Reservable> reservations, Runnable action) {
        return withOnTriggeringProcedure(
                rulePersistence, () -> new FunctionalInstantProcedure(reservations, action));
    }

    public Rule withOnTriggeringProcedure(
            RulePersistence rulePersistence, Reservable reservation, Runnable action) {
        return withOnTriggeringProcedure(rulePersistence, Set.of(reservation), action);
    }

    /** Specify a creator for the Procedure that should be run when this rule was triggering before and is no longer triggering. */
    public Rule withFinishedTriggeringProcedure(Supplier<Procedure> action) {
        if (sealed) {
            throw new IllegalStateException(
                    "Cannot modify rules once they've been evaluated in the RuleEngine");
        }
        if (triggerProcedures.containsKey(TriggerType.FINISHED)) {
            throw new IllegalStateException(
                    "This trigger already has an FinishedTriggering action");
        }

        triggerProcedures.put(TriggerType.FINISHED, action);
        triggerReservations.put(TriggerType.FINISHED, getReservationsForProcedure(action));
        return this;
    }

    public Rule withFinishedTriggeringProcedure(Set<Reservable> reservations, Runnable action) {
        return withFinishedTriggeringProcedure(
                () -> new FunctionalInstantProcedure(reservations, action));
    }

    public Rule withFinishedTriggeringProcedure(Reservable reservation, Runnable action) {
        return withFinishedTriggeringProcedure(Set.of(reservation), action);
    }

    /** Specify Rules which should only trigger when this Rule is also triggering. */
    public Rule whenTriggering(RuleGroup rules) {
        if (sealed) {
            throw new IllegalStateException(
                    "Cannot modify rules once they've been evaluated in the RuleEngine");
        }
        rules.mergeInto(container, this, true);
        return this;
    }

    /** Specify Rules which should only trigger when this Rule is not triggering. */
    public Rule whenNotTriggering(RuleGroup rules) {
        if (sealed) {
            throw new IllegalStateException(
                    "Cannot modify rules once they've been evaluated in the RuleEngine");
        }
        rules.mergeInto(container, this, false);
        return this;
    }

    /* package */ void attachTo(RuleGroupBase container, Rule parent, boolean triggerValue) {
        if (sealed) {
            throw new IllegalStateException(
                    "Cannot modify rules once they've been evaluated in the RuleEngine");
        }
        this.container = container;
        if (parent != null) {
            this.name = parent.getName() + '/' + this.name;
            final var previousPredicate = this.predicate;
            this.predicate =
                    triggerValue
                            // Important! These composed predicates shouldn't invoke the parent's
                            // `predicate`. Each Rule's `predicate` should be invoked only once
                            // per call to RuleEngine.run(), so having all rules in the hierarchy
                            // call it would not work as expected. Instead, we have the child rules
                            // query the triggering state of the parent rule.
                            // Also Important! The order of these conditions matters: we want the
                            // user's predicate to be invoked only when this rule is active
                            // (i.e. when its parent condition is satisfied), so we put the user's
                            // predicate second, so it gets short-circuited when the rule is not
                            // active.
                            ? () -> parent.isTriggering() && previousPredicate.getAsBoolean()
                            : () -> !parent.isTriggering() && previousPredicate.getAsBoolean();
        }
    }

    private static Set<Subsystem> getReservationsForProcedure(Supplier<Procedure> supplier) {
        if (supplier != null) {
            Procedure procedure = supplier.get();
            if (procedure != null) {
                HashSet<Subsystem> subsystems = new HashSet<>();
                for (var r : procedure.reservations()) {
                    subsystems.addAll(r.getReservableSubsystems());
                }
                return subsystems;
            }
        }
        return Collections.emptySet();
    }

    public String getName() {
        return name;
    }

    /* package */ TriggerType getCurrentTriggerType() {
        return currentTriggerType;
    }

    /* package */ boolean isTriggering() {
        return switch (currentTriggerType) {
            case NEWLY -> true;
            case CONTINUING -> true;
            case FINISHED -> false;
            case NONE -> false;
        };
    }

    /* package */ void seal() {
        sealed = true;
    }

    /* package */ void reset() {
        currentTriggerType = TriggerType.NONE;
        log("reset");
    }

    /* package */ void evaluate() {
        if (predicate.getAsBoolean()) {
            currentTriggerType =
                    switch (currentTriggerType) {
                        case NONE -> TriggerType.NEWLY;
                        case NEWLY -> TriggerType.CONTINUING;
                        case CONTINUING -> TriggerType.CONTINUING;
                        case FINISHED -> TriggerType.NEWLY;
                    };
        } else {
            currentTriggerType =
                    switch (currentTriggerType) {
                        case NONE -> TriggerType.NONE;
                        case NEWLY -> TriggerType.FINISHED;
                        case CONTINUING -> TriggerType.FINISHED;
                        case FINISHED -> TriggerType.NONE;
                    };
        }
        log();
    }

    /* package */ Set<Subsystem> getSubsystemsToReserve() {
        return triggerReservations.getOrDefault(currentTriggerType, Collections.emptySet());
    }

    /* package */ Cancellation getCancellationOnFinish() {
        return cancellationOnFinish;
    }

    /* package */ Procedure getProcedureToRun() {
        if (currentTriggerType != TriggerType.NONE) {
            if (triggerProcedures.containsKey(currentTriggerType)) {
                Supplier<Procedure> supplier = triggerProcedures.get(currentTriggerType);
                if (supplier != null) {
                    return supplier.get();
                }
            }
        }
        return null;
    }

    /* package */ void log(String value) {
        String containerName = container == null ? "" : container.getName();
        Logger.recordOutput("Rules/" + containerName + "/" + name, value);
    }

    /* package */ void log() {
        log(currentTriggerType.toString());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[ name: ");
        builder.append(name);
        builder.append(", cancelledOnFinish: ");
        builder.append(cancellationOnFinish);
        builder.append(", predicate: ");
        builder.append(predicate);
        builder.append(", currentTriggerType: ");
        builder.append(currentTriggerType);
        builder.append("]");
        return builder.toString();
    }
}
