package com.team766.framework;

import java.util.ArrayList;
import java.util.List;

public class RuleGroup extends RuleGroupBase {
    private final List<Rule> rules = new ArrayList<>();

    @Override
    /* package */ void addRule(Rule rule) {
        rules.add(rule);
    }

    /* package */ void mergeInto(RuleGroupBase container, Rule parent, boolean triggerValue) {
        for (var rule : rules) {
            rule.attachTo(container, parent, triggerValue);
            container.addRule(rule);
        }
        rules.clear();
    }
}
