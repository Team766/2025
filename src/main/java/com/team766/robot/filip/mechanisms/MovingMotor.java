package com.team766.robot.filip.mechanisms;

// importing previous functions, classes needed

import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;



// double is a type (int.int)

public class FilipMovingMotor extends MechanismWithStatus<MovingMotor.MovingMotorStatus> {
  
  public record MovingMotorStatus(double currentPosition) implements Status
  }
 // creating a motor object
  protected MovingMotorStatus updateStatus(){
    return new MovingMotorStatus(motor.getSensorPosition());
  }
  MotorController motor = RobotProvider.instance.getMotor("leftMotor");
  public FilipMovingMotor() {}
  public void moveSpeed(double speed) {
    motor.set(speed);
  }

}
