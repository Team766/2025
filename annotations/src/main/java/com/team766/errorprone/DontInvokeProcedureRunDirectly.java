package com.team766.errorprone;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.BugPattern.LinkType;
import com.google.errorprone.BugPattern.SeverityLevel;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MemberReferenceTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.matchers.method.MethodMatchers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.MethodInvocationTree;

@AutoService(BugChecker.class)
@BugPattern(
        summary = "Use context.runSync(procedure) instead of procedure.run(context)",
        severity = SeverityLevel.ERROR,
        linkType = LinkType.NONE)
public class DontInvokeProcedureRunDirectly extends BugChecker
        implements MethodInvocationTreeMatcher, MemberReferenceTreeMatcher {
    Matcher<? super ExpressionTree> procedureRunMethodMatcher =
            MethodMatchers.instanceMethod()
                    .onDescendantOf("com.team766.framework.Procedure")
                    .named("run");

    @Override
    public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
        if (procedureRunMethodMatcher.matches(tree, state)) {
            return describeMatch(tree);
        }

        return Description.NO_MATCH;
    }

    @Override
    public Description matchMemberReference(MemberReferenceTree tree, VisitorState state) {
        if (procedureRunMethodMatcher.matches(tree, state)) {
            return describeMatch(tree);
        }

        return Description.NO_MATCH;
    }
}
