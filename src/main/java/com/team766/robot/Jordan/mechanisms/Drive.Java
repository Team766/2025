package com.team766.robot.Rookie_Training.mechanisms;

import com.team776.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;

public class MovingMotor extends MechanismWithStatus <MovingMotor.MovingMotorStatus> {

    Public MovingMotor () {

    }

    MotorController motor = RobotProvider.instance.getMotor(configName:"motor");

    public record MovingMotorStatus (double currentPosition) implements Status {
    }

    public void setMotorPower(double power) {
        motor.set(power);
    }

    protected MovingMotorStatus updateStatus() {
            return MovingMotorStatus (MotorController.getSensorPosition());
    }

    //MotorController.getSensorPosition();
    //MotorController.setPosition();
    //MotorController.setCurrentLimit();
    //MotorController.follow();
    //MotorController.setInverted();



}