// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package com.team766.robot.Geovanni_P.Mechanisms;

import com.team766.framework3.MechanismWithStatus;
import com.team766.framework3.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;
import com.team766.robot.Geovanni_P.Mechanisms.MovingMotor.MovingMotorStatus;

/** Add your docs here. */

public class MovingMotor extends MechanismWithStatus<MovingMotorStatus> {

MotorController Motor=RobotProvider.instance.getMotor("Motor")

    public record MovingMotorStatus(double currentPosition) implements Status{

    }

}
protected MovingMotorStatus updateStatus () {
    return new MovingMotorStatus(currentPosition:0);
}
 

}

