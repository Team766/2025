package com.team766.robot.reva_2025.mechanisms;

import com.team766.framework3.MechanismWithStatus;
import com.team766.framework3.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;

public class CoralIntake extends MechanismWithStatus<CoralIntake.CoralIntakeStatus> {
    private static final double POWER_IN = 0.25;
    private static final double POWER_OUT = -1.0;
    private MotorController motor;

    public static record CoralIntakeStatus(double currentPower) implements Status {}

    public CoralIntake() {
        motor = RobotProvider.instance.getMotor("coralIntake.motor");
    }

    public void in() {
        motor.set(POWER_IN);
    }

    public void out() {
        motor.set(POWER_OUT);
    }

    public void stop() {
        motor.set(0.0);
    }

    @Override
    protected void onMechanismIdle() {
        stop();
    }

    @Override
    protected CoralIntakeStatus updateStatus() {
        return new CoralIntakeStatus(motor.get());
    }
}
