package com.team766.framework3;

import com.team766.logging.Category;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/* package */ abstract class RuleGroupConstructors implements StatusesMixin, LoggingBase {

    protected static final BooleanSupplier UNCONDITIONAL = () -> true;

    @Override
    public Category getLoggerCategory() {
        return Category.RULES;
    }

    protected abstract void addRule(Rule.Builder builder);

    protected final void addRule(
            String name,
            BooleanSupplier predicate,
            RulePersistence rulePersistence,
            Supplier<Procedure> action) {
        addRule(Rule.create(name, predicate).onTriggering(rulePersistence, action));
    }

    protected final void addRule(
            String name,
            BooleanSupplier predicate,
            RulePersistence rulePersistence,
            Set<Reservable> reservations,
            Runnable action) {
        addRule(
                Rule.create(name, predicate)
                        .onTriggering(rulePersistence, reservations, () -> action.run()));
    }

    protected final <M extends Reservable> void addRule(
            String name,
            BooleanSupplier predicate,
            RulePersistence rulePersistence,
            M reservation,
            Supplier<Request<M>> requestSupplier) {
        addRule(
                Rule.create(name, predicate)
                        .onTriggering(rulePersistence, Set.of(reservation), requestSupplier::get));
    }

    protected final <M extends Reservable> void addRule(
            String name,
            BooleanSupplier predicate,
            M reservation,
            Supplier<Request<M>> requestSupplier) {
        addRule(name, predicate, RulePersistence.ONCE_AND_HOLD, reservation, requestSupplier);
    }

    protected final void addRules(RuleGroup group) {
        for (Rule.Builder rule : group.getRuleBuildersOrderedByPriority()) {
            addRule(rule);
        }
    }
}
