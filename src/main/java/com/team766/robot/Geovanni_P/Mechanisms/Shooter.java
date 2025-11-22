package com.team766.robot.Geovanni_P.Mechanisms;

import com.team766.framework.MechanismWithStatus;
import com.team766.hal.MotorController;
import com.team766.framework.Status;
import com.team766.hal.RobotProvider;




public class Shooter extends MechanismWithStatus<Shooter.ShooterStatus> {
    public MotorController motor;
    public Shooter() {
        motor = RobotProvider.instance.getMotor("shootermotor");
    }
    

    public record ShooterStatus(double currentPosition) implements Status {
    }

    public void setMotorPower(double power) {
        motor.set(power);
    }
    
    
    protected ShooterStatus updateStatus() {
        return new ShooterStatus(motor.getSensorPosition());
    }


    public static void shoot() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'shoot'");
    }
}
