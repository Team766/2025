package com.team766.robot.tutorial.mechanisms;

import com.team766.framework.Mechanism;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;
import com.team766.hal.SolenoidController;

public class Intake extends Mechanism {
    private SolenoidController extend;
    private MotorController wheels;

    public Intake() {
        extend = RobotProvider.instance.getSolenoid("intakeArm");
        wheels = RobotProvider.instance.getMotor("intakeWheels");
    }

    public void setExtended(boolean extended) {
        extend.set(extended);
    }

    public void setWheelPower(double power) {
        wheels.set(power);
    }
}
