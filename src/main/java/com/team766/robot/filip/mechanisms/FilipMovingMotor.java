package com.team766.robot.filip.mechanisms;

// importing previous functions, classes needed

import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;


public class FilipMovingMotor extends MechanismWithStatus<FilipMovingMotor.MovingMotorStatus> {
  MotorController motor = RobotProvider.instance.getMotor("leftMotor");
  public record MovingMotorStatus(double currentPosition) implements Status {}
 // creating a motor object

  public FilipMovingMotor() {}
  protected MovingMotorStatus updateStatus(){
    return new MovingMotorStatus(motor.getSensorPosition());
  }
  
  public void moveSpeed(double speed) {
    motor.set(speed);
  }

}
