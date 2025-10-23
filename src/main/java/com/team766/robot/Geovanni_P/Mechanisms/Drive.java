// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package com.team766.robot.Geovanni_P.mechanisms;

import java.util.Set;
import com.team766.framework.MechanismWithStatus;
import com.team766.framework.RuleEngine;
import com.team766.framework.Status;
import com.team766.hal.JoystickReader;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;

/** Add your docs here. */
public class Drive {



    public class MovingMotor extends MechanismWithStatus<MovingMotor.MovingMotorStatus> {
    public MotorController motor;
    public MovingMotor() {
      motor = RobotProvider.instance.getMotor("motor");
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


public class OI extends RuleEngine {
    public OI(MovingMotor Rightmotor, MovingMotor Leftmotor) {
       JoystickReader joystick1 = RobotProvider.instance.getJoystick(0);
       JoystickReader joystick2 = RobotProvider.instance.getJoystick(1);
       addRule("Name",
        joystick1.whenButton(5),
        ONCE,
        Set.of(Rightmotor),
        () -> {Rightmotor.setMotorPower(1);});}



}
