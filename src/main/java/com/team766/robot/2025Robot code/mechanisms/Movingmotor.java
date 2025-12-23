public class MovingMotor.MvingMtorStatus extends MechanismWithStatus<MovingMotor.MovingMotorStatus >{

m1 = RobotProvider.instance.getMotor();

    public record MovingMotorStatus(double currentPosition) implements Status {protected MovingMotorStatus updateStatus() {
        return 3;
    }
 
}        


package com.team766.robot.Rookie_Training.mechanisms;
import com.team766.framework.MechanismWithStatus;
import com.team766.hal.MotorController;
import cm.team766.hal.RobotProvider;

public class MovingMotor extends MechanismWithStatus<MovingMotor.MovingMotorStatus> {

public MovingMotor {}
    MotorController motor = RobotProvider.instance.getMotor(configName: "motor");
    public record MovingMotorStatus (double currentPosition) implements Status {
        protected MovingMotorStatus updateStatus() {
            return new MovingMotorStatus(currentPosition:0);
    }
}
public void setMotorPower(double power) {
    motor.set(power);
}
