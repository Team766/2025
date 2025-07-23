package com.team766.framework;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.team766.framework.test.FakeMechanism;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class ProcedureTest {
    @Test
    public void testNoReserveNullMechanisms() {
        var mech1 = new FakeMechanism();
        var mech2 = new FakeMechanism();

        @SuppressWarnings("unused")
        var proc =
                new Procedure() {
                    {
                        assertDoesNotThrow(() -> reserve(mech1));
                        assertThrows(
                                NullPointerException.class, () -> reserve((FakeMechanism) null));
                        assertDoesNotThrow(() -> reserve(mech1, mech2));
                        assertThrows(NullPointerException.class, () -> reserve(null, mech1));
                        assertThrows(NullPointerException.class, () -> reserve(mech1, null));
                        // NOTE(rcahoon, 2025-07-22): Using Arrays.asList instead of List.of because
                        // List.of doesn't allow null values
                        assertDoesNotThrow(() -> reserve(Arrays.asList(mech1, mech2)));
                        assertThrows(
                                NullPointerException.class,
                                () -> reserve(Arrays.asList(null, mech1)));
                        assertThrows(
                                NullPointerException.class,
                                () -> reserve(Arrays.asList(mech1, null)));
                    }

                    @Override
                    public void run(Context context) {}
                };
    }
}
