// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package com.team766.robot.Geovanni_P.Mechanisms;

import java.util.Set;
import com.team766.framework.MechanismWithStatus;
import com.team766.framework.RuleEngine;
import com.team766.framework.Status;
import com.team766.hal.JoystickReader;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;
import com.team766.robot.Geovanni_P.Mechanisms.MovingMotor;



    public class Drive extends MechanismWithStatus<Drive.DriveStatus> {
        private MovingMotor LeftMotor;
        private MovingMotor RightMotor;
        public Drive() {
          LeftMotor =  new MovingMotor("Leftmotor");
          RightMotor = new MovingMotor("Rightmotor");
        }
        
        public void setMotorPower(double Leftpower, double Rightpower) {
        LeftMotor.setMotorPower(Leftpower);
        RightMotor.setMotorPower(Rightpower);

    }

          public record DriveStatus(double currentPosition) implements Status {
        }
        
        
      protected DriveStatus updateStatus(){
        return new DriveStatus(1);
      }
        
        
    }



