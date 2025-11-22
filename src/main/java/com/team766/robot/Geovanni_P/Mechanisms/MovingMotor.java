// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package com.team766.robot.Geovanni_P.Mechanisms;

import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider; 

/** Add your docs here. */

public class MovingMotor extends MechanismWithStatus<MovingMotor.MovingMotorStatus> {
    public MotorController motor;
    public MovingMotor(String motorname) {
      motor = RobotProvider.instance.getMotor(motorname);
    }
    
    public record MovingMotorStatus(double currentPosition) implements Status {
    }

    public void setMotorPower(double power) {
        motor.set(power);
    }

    protected MovingMotorStatus updateStatus(){
        return new MovingMotorStatus(3);
    }
    
}