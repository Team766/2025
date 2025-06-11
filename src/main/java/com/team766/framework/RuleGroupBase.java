package com.team766.framework;

import static com.team766.framework.RulePersistence.ONCE_AND_HOLD;

import com.team766.logging.Category;
import java.util.Set;
import java.util.function.BooleanSupplier;

/* package */ abstract class RuleGroupBase implements StatusesMixin, LoggingBase {
    protected static final BooleanSupplier UNCONDITIONAL = () -> true;

    @Override
    public Category getLoggerCategory() {
        return Category.RULES;
    }

    /* package */ abstract void addRule(Rule rule);

    protected Rule addRule(String name, BooleanSupplier condition) {
        final Rule rule = new Rule(this, name, condition);
        addRule(rule);
        return rule;
    }

    protected Rule addRule(
            String name,
            BooleanSupplier condition,
            RulePersistence rulePersistence,
            SerializableLambda.Supplier<Procedure> action) {
        return addRule(name, condition).withOnTriggeringProcedure(rulePersistence, action);
    }

    protected Rule addRule(
            String name, BooleanSupplier condition, SerializableLambda.Supplier<Procedure> action) {
        return addRule(name, condition, ONCE_AND_HOLD, action);
    }

    protected Rule addRule(
            String name,
            BooleanSupplier condition,
            RulePersistence rulePersistence,
            Set<Reservable> mechanisms,
            SerializableLambda.Consumer<Context> action) {
        return addRule(
                name,
                condition,
                rulePersistence,
                () -> new FunctionalProcedure(mechanisms, action));
    }

    protected Rule addRule(
            String name,
            BooleanSupplier condition,
            Set<Reservable> mechanisms,
            SerializableLambda.Consumer<Context> action) {
        return addRule(name, condition, ONCE_AND_HOLD, mechanisms, action);
    }

    protected Rule addRule(
            String name,
            BooleanSupplier condition,
            RulePersistence rulePersistence,
            Mechanism mechanism,
            SerializableLambda.Consumer<Context> action) {
        return addRule(name, condition, rulePersistence, Set.of(mechanism), action);
    }

    protected Rule addRule(
            String name,
            BooleanSupplier condition,
            Mechanism mechanism,
            SerializableLambda.Consumer<Context> action) {
        return addRule(name, condition, ONCE_AND_HOLD, mechanism, action);
    }

    protected Rule addRule(
            String name,
            BooleanSupplier condition,
            RulePersistence rulePersistence,
            Set<Reservable> mechanisms,
            SerializableLambda.Runnable action) {
        return addRule(
                name,
                condition,
                rulePersistence,
                () -> new FunctionalInstantProcedure(mechanisms, action));
    }

    protected Rule addRule(
            String name,
            BooleanSupplier condition,
            Set<Reservable> mechanisms,
            SerializableLambda.Runnable action) {
        return addRule(name, condition, ONCE_AND_HOLD, mechanisms, action);
    }

    protected Rule addRule(
            String name,
            BooleanSupplier condition,
            RulePersistence rulePersistence,
            Reservable mechanism,
            SerializableLambda.Runnable action) {
        return addRule(name, condition, rulePersistence, Set.of(mechanism), action);
    }

    protected Rule addRule(
            String name,
            BooleanSupplier condition,
            Reservable mechanism,
            SerializableLambda.Runnable action) {
        return addRule(name, condition, ONCE_AND_HOLD, mechanism, action);
    }

    protected final void addRules(RuleGroup group) {
        group.mergeInto(this, null, true);
    }
}
