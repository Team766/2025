package com.team766.robot.Kevan.mechanisms;

import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;
import com.team766.robot.reva.mechanisms.Shooter.ShooterStatus;
import com.team766.robot.common.constants.InputConstants;

public class Shooter extends MechanismWithStatus<Shooter.ShooterStatus> {

    MotorController shooter_motor = RobotProvider.instance.getMotor("shooter_motor");
    public record ShooterStatus(double currentPosition) implements Status {
    }
    public Shooter() {}
    
    public void SetShooterSpeed(double motorPower) {
        shooter_motor.set(motorPower);
    }
    protected ShooterStatus updateStatus() {
        return new ShooterStatus(0);
    }
 }