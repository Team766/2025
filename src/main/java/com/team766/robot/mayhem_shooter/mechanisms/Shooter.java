package com.team766.robot.mayhem_shooter.mechanisms;

import com.team766.framework3.Mechanism;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;

public class Shooter extends Mechanism {
    private MotorController leftMotor;
    private MotorController rightMotor;

    public ExampleMechanism() {
        leftMotor = RobotProvider.instance.getMotor("mayhemShooter.leftMotor");
        rightMotor = RobotProvider.instance.getMotor("mayhemShooter.rightMotor");

        leftMotor.setCurrentLimit(20);
        rightMotor.setCurrentLimit(20);
    }

    public void setMotorPower(final double leftPower, final double rightPower) {
        leftMotor.set(leftPower);
        rightMotor.set(rightPower);
    }

    @Override
    protected void onMechanismIdle() {
        // Stop mechanism when nothing is using it.
        setMotorPower(0, 0);
    }
}
