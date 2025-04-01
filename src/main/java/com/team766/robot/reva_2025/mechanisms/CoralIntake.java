package com.team766.robot.reva_2025.mechanisms;

import com.team766.framework3.MechanismWithStatus;
import com.team766.framework3.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;
import com.team766.robot.reva.mechanisms.MotorUtil;

public class CoralIntake extends MechanismWithStatus<CoralIntake.CoralIntakeStatus> {
    private static final double POWER_IN = 0.50;
    private static final double POWER_IDLE = 0.1;
    private static final double POWER_OUT = -0.75;
    private State state = State.Stop;
    private MotorController motor;
    public static final double INTAKE_CURRENT_THRESHOLD = 16;

    public static record CoralIntakeStatus(double intakePower, double current, State state)
            implements Status {
        public boolean coralIntakeSuccessful() {
            return current > INTAKE_CURRENT_THRESHOLD;
        }
    }

    public enum State {
        // velocity is in revolutions per minute
        In,
        Out,
        Stop,
        Idle
    }

    public CoralIntake() {
        motor = RobotProvider.instance.getMotor("coralIntake.motor");
        motor.setCurrentLimit(30);
    }

    public void in() {
        motor.set(POWER_IN);
        state = State.In;
    }

    public void out() {
        motor.set(POWER_OUT);
        state = State.Out;
    }

    public void idle() {
        motor.set(POWER_IDLE);
        state = State.Idle;
    }

    public void stop() {
        motor.set(0.0);
        state = State.Stop;
    }

    @Override
    protected CoralIntakeStatus updateStatus() {
        return new CoralIntakeStatus(motor.get(), MotorUtil.getStatorCurrentUsage(motor), state);
    }
}
