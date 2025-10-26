package com.team766.robot.ArthurDoering.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.ArthurDoering.mechanisms.MovingMotor;

public class MoveMotor extends Procedure{
    private MovingMotor motor;

    public void Move(Autonomous myMotor){
        motor = reserve(myMotor);
    }

    public void run(Context context){
        motor.set(1);
        context.waitForSeconds(5);
        motor.set(0);
    }
}