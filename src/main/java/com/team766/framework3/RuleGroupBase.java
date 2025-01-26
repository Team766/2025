package com.team766.framework3;

import static com.team766.framework3.RulePersistence.ONCE_AND_HOLD;

import com.team766.logging.Category;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
            Supplier<Procedure> action) {
        return addRule(name, condition).withOnTriggeringProcedure(rulePersistence, action);
    }

    protected Rule addRule(String name, BooleanSupplier condition, Supplier<Procedure> action) {
        return addRule(name, condition, ONCE_AND_HOLD, action);
    }

    protected Rule addRule(
            String name,
            BooleanSupplier condition,
            RulePersistence rulePersistence,
            Set<Mechanism> mechanisms,
            Consumer<Context> action) {
        return addRule(
                name,
                condition,
                rulePersistence,
                () -> new FunctionalProcedure(mechanisms, action));
    }

    protected Rule addRule(
            String name,
            BooleanSupplier condition,
            Set<Mechanism> mechanisms,
            Consumer<Context> action) {
        return addRule(name, condition, ONCE_AND_HOLD, mechanisms, action);
    }

    protected Rule addRule(
            String name,
            BooleanSupplier condition,
            RulePersistence rulePersistence,
            Mechanism mechanism,
            Consumer<Context> action) {
        return addRule(name, condition, rulePersistence, Set.of(mechanism), action);
    }

    protected Rule addRule(
            String name, BooleanSupplier condition, Mechanism mechanism, Consumer<Context> action) {
        return addRule(name, condition, ONCE_AND_HOLD, mechanism, action);
    }

    protected Rule addRule(
            String name,
            BooleanSupplier condition,
            RulePersistence rulePersistence,
            Set<Mechanism> mechanisms,
            Runnable action) {
        return addRule(
                name,
                condition,
                rulePersistence,
                () -> new FunctionalInstantProcedure(mechanisms, action));
    }

    protected Rule addRule(
            String name, BooleanSupplier condition, Set<Mechanism> mechanisms, Runnable action) {
        return addRule(name, condition, ONCE_AND_HOLD, mechanisms, action);
    }

    protected Rule addRule(
            String name,
            BooleanSupplier condition,
            RulePersistence rulePersistence,
            Mechanism mechanism,
            Runnable action) {
        return addRule(name, condition, rulePersistence, Set.of(mechanism), action);
    }

    protected Rule addRule(
            String name, BooleanSupplier condition, Mechanism mechanism, Runnable action) {
        return addRule(name, condition, ONCE_AND_HOLD, mechanism, action);
    }

    protected final void addRules(RuleGroup group) {
        group.mergeInto(this, null, true);
    }
}
