package com.team766.robot.Kevan.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.Kevan.mechanisms.MovingMotor;

public class MoveMotor extends Procedure {
    private MovingMotor motor;
    
    public MoveMotor(MovingMotor myMotor) {
        motor = reserve(myMotor);
    
    }
    public void run(Context context) {
        motor.move(1);
        context.waitForSeconds(5);
        motor.move(0);
    }
    
}
