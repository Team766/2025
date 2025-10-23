package com.team766.robot.ArthurDoering.mechanisms;

import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.RobotProvider;
import com.team766.simulator.elements.MotorController;

public class Arm extends MechanismWithStatus<Arm.ArmStatus> {
    
    public ArmMotor(){

    }

    MotorController armMotor = RobotProvider.instance.getMotor("Motor");
    public record ArmStatus(double currentPosition) implements Status{
    }

    public void setMotorPower(double power){
        armMotor.set(power);
    }

    protected ArmStatus updateStatus(){
        return new ArmStatus(0);
        armMotor.getSensorPosition();
    }
}
