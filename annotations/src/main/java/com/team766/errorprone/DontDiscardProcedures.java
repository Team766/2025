package com.team766.errorprone;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.matchers.method.MethodMatchers.constructor;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.BugPattern.LinkType;
import com.google.errorprone.bugpatterns.AbstractReturnValueIgnored;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.predicates.TypePredicates;
import com.sun.source.tree.ExpressionTree;

@AutoService(BugChecker.class)
@BugPattern(summary = "Procedure created but not run", severity = ERROR, linkType = LinkType.NONE)
public class DontDiscardProcedures extends AbstractReturnValueIgnored {
    @Override
    protected Matcher<? super ExpressionTree> specializedMatcher() {
        return constructor()
                .forClass(TypePredicates.isDescendantOf("com.team766.framework3.Procedure"));
    }

    @Override
    protected boolean allowInExceptionThrowers() {
        return false;
    }
}
