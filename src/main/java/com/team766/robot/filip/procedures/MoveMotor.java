package com.team766.robot.filip.procedures;
import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.filip.mechanisms.FilipMovingMotor;

public class MoveMotor extends Procedure {
  private FilipMovingMotor motor;
  public MoveMotor(FilipMovingMotor myMotor){
    motor = reserve(myMotor);
  }
  @Override
  public void run(Context context) {
    motor.moveSpeed(1);
    context.waitForSeconds(5);
    motor.moveSpeed(0);
  }
}