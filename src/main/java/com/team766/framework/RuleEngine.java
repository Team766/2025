package com.team766.framework;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.team766.framework.Rule.ResetReason;
import com.team766.logging.LoggerExceptionUtils;
import com.team766.logging.Severity;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.Subsystem;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * {@link RuleEngine}s manage and process a set of {@link Rule}s.  Subclasses should add rules via
 * {@link #addRule(com.team766.framework.Rule.Builder)}.  {@link Rule}s have an implicit priority based on insertion order - the first {@link Rule} to be added is highest priority, etc.
 *
 * Callers should then call {@link #run} once per iteration.  Each call to {@link #run} evaluates each of the contained {@link Rule}s, firing the associated {@link Procedure}
 * {@link Supplier}s when {@link Rule}s are NEWLY triggering, are CONTINUING to trigger, or are FINISHED triggering.
 *
 * The {@link RuleEngine} also pays attention to the {@link Mechanism}s that these {@link Procedure}s reserve.
 * For a {@link Rule} to trigger, its predicate must be satisfied -- and, the {@link Mechanism}s the corresponding {@link Procedure} would reserve
 * must not be in use or about to be in use from a higher priority {@link Rule}.
 */
public class RuleEngine extends RuleGroupBase {

    private static record RuleAction(Rule rule, Rule.TriggerType triggerType) {}

    private final LinkedHashMap<String, Rule> rules = new LinkedHashMap<>();
    private final Map<Rule, Integer> rulePriorities = new HashMap<>();
    private BiMap<Command, RuleAction> ruleMap = HashBiMap.create();
    private boolean sealed = false;

    protected RuleEngine() {}

    @Override
    protected void addRule(Rule rule) {
        if (sealed) {
            throw new IllegalStateException(
                    "Cannot add rules after the RuleEngine has started running");
        }
        rules.put(rule.getName(), rule);
        int priority = rulePriorities.size();
        rulePriorities.put(rule, priority);
    }

    @VisibleForTesting
    /* package */ int size() {
        return rules.size();
    }

    @VisibleForTesting
    /* package */ Rule getRuleByName(String name) {
        return rules.get(name);
    }

    @VisibleForTesting
    /* package */ int getPriorityForRule(Rule rule) {
        if (rulePriorities.containsKey(rule)) {
            return rulePriorities.get(rule);
        }
        log(
                Severity.WARNING,
                "Could not find priority for rule " + rule.getName() + ".  Should not happen.");
        return Integer.MAX_VALUE;
    }

    protected Rule getRuleForTriggeredProcedure(Command command) {
        RuleAction ruleAction = ruleMap.get(command);
        return (ruleAction == null) ? null : ruleAction.rule;
    }

    private void sealRules() {
        for (Rule rule : rules.values()) {
            rule.seal();
        }
    }

    public final void run() {
        if (!sealed) {
            sealRules();
            sealed = true;
        }

        Set<Subsystem> subsystemsToUse = new HashSet<>();

        // TODO(MF3): when creating a Procedure, check that the reservations are the same as
        // what the Rule pre-computed.

        // evaluate each rule
        ruleLoop:
        for (Rule rule : rules.values()) {
            try {
                rule.evaluate();

                // see if the rule is triggering
                final Rule.TriggerType triggerType = rule.getCurrentTriggerType();
                if (triggerType != Rule.TriggerType.NONE) {
                    int priority = getPriorityForRule(rule);

                    // see if there are mechanisms a potential procedure would want to reserve
                    Set<Subsystem> reservations = rule.getSubsystemsToReserve();
                    for (Subsystem subsystem : reservations) {
                        // see if any of the mechanisms higher priority rules will use would also be
                        // used by this lower priority rule's procedure.
                        if (subsystemsToUse.contains(subsystem)) {
                            rule.reset(ResetReason.IGNORED);
                            continue ruleLoop;
                        }
                        // see if a previously triggered rule is still using the mechanism
                        Command existingCommand =
                                CommandScheduler.getInstance().requiring(subsystem);
                        if (existingCommand != null) {
                            // look up the rule
                            Rule existingRule = getRuleForTriggeredProcedure(existingCommand);
                            if (existingRule != null) {
                                // look up the priority
                                int existingPriority = getPriorityForRule(existingRule);
                                if (existingPriority < priority /* less is more */) {
                                    // existing rule takes priority.
                                    // don't proceed with this new rule.
                                    rule.reset(ResetReason.IGNORED);
                                    continue ruleLoop;
                                } else if (rule != existingRule) {
                                    // new rule takes priority
                                    // reset existing rule
                                    existingRule.reset(ResetReason.PREEMPTED);
                                }
                            }
                        }
                    }

                    // we're good to proceed
                    if (triggerType == Rule.TriggerType.FINISHED
                            && rule.getCancellationOnFinish()
                                    == Rule.Cancellation.CANCEL_NEWLY_ACTION) {
                        var newlyCommand =
                                ruleMap.inverse().get(new RuleAction(rule, Rule.TriggerType.NEWLY));
                        if (newlyCommand != null) {
                            newlyCommand.cancel();
                        }
                    }

                    Procedure procedure = rule.getProcedureToRun();
                    if (procedure == null) {
                        continue;
                    }
                    log(
                            Severity.INFO,
                            "Rule "
                                    + rule.getName()
                                    + " triggered ("
                                    + rule.getCurrentTriggerType()
                                    + ").  Running Procedure "
                                    + procedure.getName()
                                    + " with reservations "
                                    + reservations);

                    // TODO(MF3): check that the reservations have not changed
                    Command command = procedure.createCommandToRunProcedure();
                    subsystemsToUse.addAll(reservations);
                    ruleMap.forcePut(command, new RuleAction(rule, triggerType));
                    command.schedule();
                }
            } catch (Exception ex) {
                log(
                        Severity.ERROR,
                        "Exception caught while trying to run(): "
                                + LoggerExceptionUtils.exceptionToString(ex));
            }
        }

        for (Rule rule : rules.values()) {
            rule.flushLog();
        }
    }
}
