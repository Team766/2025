package com.team766.robot.ArthurDoering.mechanisms;

import com.team766.framework.MechanismWithStatus;
import com.team766.hal.RobotProvider;
import com.team766.simulator.elements.MotorController;

public class Arm extends MechanismWithStatus<Arm.ArmStatus> {
    
    public ArmMotor(){

    }

    MotorController armMotor = RobotProvider.instance.getMotor("Motor");
    
}
