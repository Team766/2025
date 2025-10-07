package com.team766.hal;

import static org.junit.jupiter.api.Assertions.*;

import com.team766.TestCase;
import com.team766.framework.AutonomousMode;
import com.team766.framework.RuleEngine;
import java.io.IOException;
import org.junit.jupiter.api.Test;

public class GenericRobotMainTest extends TestCase {
    @Test
    public void testNoExceptionOnDefaultRobotConfigurator() throws IOException {
        loadConfig("{}");

        try (var robot = new GenericRobotMain()) {
            assertNotNull(robot);
        }
    }

    @Test
    public void testNoExceptionOnMissingRobotConfigurator() throws IOException {
        loadConfig(
                String.format(
                        "{ \"%s\" : \"thisRobotDoesntExist\" }",
                        RobotSelector.ROBOT_CONFIGURATOR_KEY));

        try (var robot = new GenericRobotMain()) {
            assertNotNull(robot);
        }
    }

    @Test
    public void testNoExceptionOnNefariousRobotConfigurator() throws IOException {
        loadConfig(
                String.format(
                        "{ \"%s\" : \"%s\" }",
                        RobotSelector.ROBOT_CONFIGURATOR_KEY,
                        NefariousConfigurator.class.getName()));

        try (var robot = new GenericRobotMain()) {
            assertNotNull(robot);
        }
    }
}

class NefariousConfigurator implements RobotConfigurator {
    @Override
    public void initializeMechanisms() {
        throw new UnsupportedOperationException("Unimplemented method 'initializeMechanisms'");
    }

    @Override
    public RuleEngine createOI() {
        throw new UnsupportedOperationException("Unimplemented method 'createOI'");
    }

    @Override
    public RuleEngine createLights() {
        throw new UnsupportedOperationException("Unimplemented method 'createLights'");
    }

    @Override
    public AutonomousMode[] getAutonomousModes() {
        throw new UnsupportedOperationException("Unimplemented method 'getAutonomousModes'");
    }
}
