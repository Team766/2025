package com.team766.robot.ArthurDoering.mechanisms;

import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.RobotProvider;
import com.team766.simulator.elements.MotorController;

public class Shooter extends MechanismWithStatus<Shooter.ShooterStatus>{

    public ShooterMotor(){

    }

    MotorController ShooterMotor = RobotProvider.instance.getMotor("Motor");
    public record ShooterStatus(double currentPosition) implements Status{
    }

    public void setMotorPower(double power){
        ShooterMotor.set(power);
    }

    protected ShooterStatus updateStatus(){
        return new ShooterStatus(0);
        ShooterMotor.getSensorPosition();
    }
}
