package com.team766.robot.outlaw.bearbot.mechanisms;

import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;
import com.team766.robot.outlaw.bearbot.constants.ConfigConstants;

public class Feeder extends MechanismWithStatus<Feeder.FeederStatus> {

    private static final double FEEDER_IN_POWER = 1.0;
    private static final double FEEDER_OUT_POWER = 1.0;
    private static final double CURRENT_LIMIT = 30.0;

    private final MotorController feederMotor;

    public static record FeederStatus(double feederPower) implements Status {}

    public Feeder() {
        feederMotor = RobotProvider.instance.getMotor(ConfigConstants.FEEDER_FEEDER_MOTOR);
        feederMotor.setNeutralMode(NeutralMode.Brake);
        feederMotor.setCurrentLimit(CURRENT_LIMIT);
    }

    public void feedIn() {
        feederMotor.set(FEEDER_IN_POWER);
    }

    public void feedOut() {
        feederMotor.set(FEEDER_OUT_POWER);
    }

    public void feedStop() {
        feederMotor.set(0.0);
    }

    @Override
    protected FeederStatus updateStatus() {
        return new FeederStatus(feederMotor.get());
    }
}
