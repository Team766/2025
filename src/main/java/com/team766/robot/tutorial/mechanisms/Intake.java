package com.team766.robot.tutorial.mechanisms;

import com.team766.framework.Mechanism;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;
import com.team766.hal.SolenoidController;

public class Intake extends Mechanism {
    private SolenoidController intakeArm;
    private MotorController intakeWheels;

    public Intake() {
        intakeArm = RobotProvider.instance.getSolenoid("intakeArm");
        intakeWheels = RobotProvider.instance.getMotor("intake");
    }

    public void setIntakePower(double intakePower) {
        intakeWheels.set(intakePower);
    }

    public void setIntakeArm(boolean state) {
        intakeArm.set(state);
    }
}
