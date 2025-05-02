package com.team766.errorprone;

import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

public class DontDiscardProceduresTest {
    @Test
    public void procedureDiscardedFromExpressionStatement() {
        CompilationTestHelper.newInstance(DontDiscardProcedures.class, getClass())
                .addSourceLines(
                        "Test.java",
                        """
                        import java.util.Set;
                        import com.team766.framework.FunctionalProcedure;

                        class Test {
                            void foo() {
                                // BUG: Diagnostic contains: Ignored return value
                                new FunctionalProcedure(Set.of(), context -> {});
                            }
                        }
                        """)
                .doTest();
    }

    @Test
    public void procedureDiscardedFromLambda() {
        CompilationTestHelper.newInstance(DontDiscardProcedures.class, getClass())
                .addSourceLines(
                        "Test.java",
                        """
                        import java.util.Set;
                        import com.team766.framework.FunctionalProcedure;

                        class Test {
                            // BUG: Diagnostic contains: Ignored return value
                            Runnable r = () -> new FunctionalProcedure(Set.of(), context -> {});
                        }
                        """)
                .doTest();

        // No diagnostic expected if the lambda returns the Procedure object
        CompilationTestHelper.newInstance(DontDiscardProcedures.class, getClass())
                .addSourceLines(
                        "Test.java",
                        """
                        import java.util.function.Supplier;
                        import java.util.Set;
                        import com.team766.framework.Procedure;
                        import com.team766.framework.FunctionalProcedure;

                        class Test {
                            Supplier<Procedure> r = () -> new FunctionalProcedure(Set.of(), context -> {});
                        }
                        """)
                .doTest();
    }
}
