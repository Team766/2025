package com.team766.framework3;

import java.util.LinkedList;
import java.util.List;

public class RuleGroup extends RuleGroupConstructors {
    private final List<Rule.Builder> rules = new LinkedList<>();

    @Override
    protected final void addRule(Rule.Builder builder) {
        rules.add(builder);
    }

    /* package */ List<Rule.Builder> getRuleBuildersOrderedByPriority() {
        return rules;
    }
}
