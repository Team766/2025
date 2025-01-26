package com.team766.framework3;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.team766.framework3.test.FakeMechanism;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class InstantCommandTest {
    @Test
    public void testRequirements() {
        var mech1 = new FakeMechanism();
        var mech2 = new FakeMechanism();

        var command =
                new InstantCommand(new FunctionalInstantProcedure(Set.of(mech1, mech2), () -> {}));

        assertEquals(Set.of(mech1, mech2), command.getRequirements());
    }
}
