package com.team766.robot.outlaw.bearbot.mechanisms;

import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;
import com.team766.robot.outlaw.bearbot.constants.ConfigConstants;
import com.team766.robot.outlaw.bearbot.constants.SetPointConstants;

public class Shooter extends MechanismWithStatus<Shooter.ShooterStatus> {

    private static final double CURRENT_LIMIT = 30.0;

    private final MotorController shooterMotor;

    public static record ShooterStatus(double shooterPower) implements Status {}

    public Shooter() {

        shooterMotor = RobotProvider.instance.getMotor(ConfigConstants.SHOOTER_SHOOTER_MOTOR);
        shooterMotor.setNeutralMode(NeutralMode.Brake);
        shooterMotor.setCurrentLimit(CURRENT_LIMIT);
    }

    public void shoot() {
        shooterMotor.set(SetPointConstants.SHOOTER_POWER);
    }

    public void stopShooter() {
        shooterMotor.set(0.0);
    }

    @Override
    protected ShooterStatus updateStatus() {
        return new ShooterStatus(shooterMotor.get());
    }
}
