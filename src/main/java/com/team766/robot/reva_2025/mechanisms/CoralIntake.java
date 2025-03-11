package com.team766.robot.reva_2025.mechanisms;

import com.team766.framework3.MechanismWithStatus;
import com.team766.framework3.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;
import com.team766.robot.reva.mechanisms.MotorUtil;

public class CoralIntake extends MechanismWithStatus<CoralIntake.CoralIntakeStatus> {
    private static final double POWER_IN = 0.25;
    private static final double POWER_OUT = -1.0;
    private State state = State.Stop;
    private MotorController motor;

    public static record CoralIntakeStatus(double intakePower, double current, State state)
            implements Status {
        public boolean coralIntakeSuccessful() {
            return true; // FIX THIS WITH SENSOR
        }
    }

    public enum State {
        // velocity is in revolutions per minute
        In,
        Out,
        Stop
    }

    public CoralIntake() {
        motor = RobotProvider.instance.getMotor("coralIntake.motor");
    }

    public void in() {
        motor.set(POWER_IN);
        state = State.In;
    }

    public void out() {
        motor.set(POWER_OUT);
        state = State.Out;
    }

    public void stop() {
        motor.set(0.0);
        state = State.Stop;
    }

    @Override
    protected void onMechanismIdle() {
        stop();
    }

    @Override
    protected CoralIntakeStatus updateStatus() {
        return new CoralIntakeStatus(motor.get(), MotorUtil.getCurrentUsage(motor), state);
    }
}
