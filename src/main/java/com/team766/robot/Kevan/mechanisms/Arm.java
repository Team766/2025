package com.team766.robot.Kevan.mechanisms;

import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;
import com.team766.robot.common.constants.InputConstants;

public class Arm extends MechanismWithStatus<Arm.ArmStatus> {

    MotorController arm_motor = RobotProvider.instance.getMotor("arm_motor");
    public record ArmStatus(double currentPosition) implements Status {
    
    public Arm() {}
    }
    public void move(double motorPower) {
        arm_motor.set(motorPower);
    }
    protected ArmStatus updateStatus() {
        return new ArmStatus(currentPosition:0);
    }
    }