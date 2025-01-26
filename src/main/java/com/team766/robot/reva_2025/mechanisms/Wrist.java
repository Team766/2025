package com.team766.robot.reva_2025.mechanisms;

import com.team766.framework3.MechanismWithStatus;
import com.team766.framework3.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;

public class Wrist extends MechanismWithStatus<Wrist.WristStatus> {

    public record WristStatus(double angle) implements Status {}

    private double angle;

    private MotorController wristMotor;

    public Wrist() {
        wristMotor = RobotProvider.instance.getMotor("Wrist.Motor");
        angle = 0;
    }

    public void setAngle(double angle) {
        wristMotor.set(MotorController.ControlMode.Position, angle);
        this.angle = angle;
    }
}
