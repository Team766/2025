package com.team766.robot.Jordan.mechanisms;

import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;

public class MovingMotor extends MechanismWithStatus<MovingMotor.MovingMotorStatus> {

    public MovingMotor () {
    }

    MotorController motor = RobotProvider.instance.getMotor("motor");

    public record MovingMotorStatus(double currentPosition) implements Status {
    }

    public void setMotorPower(double power) {
        motor.set(power);
    }

    protected MovingMotorStatus updateStatus() {
        return new MovingMotorStatus(motor.getSensorPosition());
    }

   

    //MotorController.getSensorPosition();
    //MotorController.setPosition();
    //MotorController.setCurrentLimit();
    //MotorController.follow();
    //MotorController.setInverted();



}