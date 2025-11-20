package com.team766.robot.Geovanni_P.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.Geovanni_P.Mechanisms.MovingMotor;

public class MoveMotor extends Procedure {

        private MovingMotor motor;

        public MoveMotor(MovingMotor myMotor){
            motor = reserve(myMotor);
        }

        public void run(Context context){
            motor.setMotorPower(1);
            context.waitForSeconds(5); 
            motor.setMotorPower(0);

        }

    }


