package com.team766.robot.Geovanni_P.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.Geovanni_P.Mechanisms.MovingMotor;
import com.team766.robot.Geovanni_P.Mechanisms.Shooter;

public class Autonomous extends Procedure {

        private MovingMotor motor; 
        private Shooter shooter;
        public Autonomous(MovingMotor motor, Shooter shooter){
            this.motor = reserve(motor);
            this.shooter = reserve(shooter);
        }

        public void run(Context context){
            motor.setMotorPower(1);
            context.waitForSeconds(3);
            motor.setMotorPower(0);
            Shooter.shoot();
        }




}
    






    

