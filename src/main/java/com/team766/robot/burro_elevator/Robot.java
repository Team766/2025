package com.team766.robot.burro_elevator;

import com.team766.framework.AutonomousMode;
import com.team766.framework.RuleEngine;
import com.team766.hal.RobotConfigurator;
import com.team766.robot.burro_arm.mechanisms.Gripper;
import com.team766.robot.burro_elevator.mechanisms.*;
import com.team766.robot.burro_elevator.procedures.*;
import com.team766.robot.common.mechanisms.BurroDrive;

public class Robot implements RobotConfigurator {
    // Declare mechanisms here
    private BurroDrive drive;
    private Elevator elevator;
    private Gripper gripper;

    @Override
    public void initializeMechanisms() {
        // Initialize mechanisms here
        drive = new BurroDrive();
        elevator = new Elevator();
        gripper = new Gripper();
    }

    @Override
    public RuleEngine createOI() {
        return new OI(drive, elevator, gripper);
    }

    @Override
    public RuleEngine createLights() {
        return null;
    }

    @Override
    public AutonomousMode[] getAutonomousModes() {
        return new AutonomousMode[] {
            // Add autonomous modes here like this:
            //    new AutonomousMode("NameOfAutonomousMode", () -> new MyAutonomousProcedure()),
            //
            // If your autonomous procedure has constructor arguments, you can
            // define one or more different autonomous modes with it like this:
            //    new AutonomousMode("DriveFast", () -> new DriveStraight(1.0)),
            //    new AutonomousMode("DriveSlow", () -> new DriveStraight(0.4)),

            new AutonomousMode("DoNothing", () -> new DoNothing()),
        };
    }
}
