package com.team766.robot.ArthurDoering.mechanisms;

import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;

public class MovingMotor extends MechanismWithStatus<MovingMotor.MovingMotorStatus> {

    public MovingMotor(){
    }

    MotorController motor = RobotProvider.instance.getMotor(configName:"motor");

    public record MovingMotorStatus(double currentPosition) implements Status{
    }

    public void setMotorPower(double power){
        motor.set(power);
    }

    protected MovingMotorStatus updateStatus(){
        return new MovingMotorStatus(0);
    }
}
