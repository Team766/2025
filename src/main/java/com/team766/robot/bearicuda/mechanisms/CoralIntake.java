package com.team766.robot.bearicuda.mechanisms;

import com.team766.framework.Mechanism;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;

public class CoralIntake extends Mechanism {

    private static final double POWER_IN = 1.0;
    private static final double POWER_OUT = -1.0;

    private MotorController motor;

    public CoralIntake() {
        motor = RobotProvider.instance.getMotor("coralIntake.motor");
    }

    public void in() {
        checkContextOwnership();
        motor.set(POWER_IN);
    }

    public void out() {
        checkContextOwnership();
        motor.set(POWER_OUT);
    }

    public void stop() {
        checkContextOwnership();
        motor.set(0.0);
    }
}
