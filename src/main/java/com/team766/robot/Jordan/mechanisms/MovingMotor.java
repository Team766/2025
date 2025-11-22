package com.team766.robot.Rookie_Training.mechanisms;

import com.team766.framework.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;
import com.team776.framework.MechanismWithStatus;

public class MovingMotor extends MechanismWithStatus<MovingMotor.MovingMotorStatus> {

    public MovingMotor() {}

    MotorController motor = RobotProvider.instance.getMotor("motor");

    public record MovingMotorStatus(double currentPosition) implements Status {}

    public void setMotorPower(double power) {
        motor.set(power);
    }

    protected MovingMotorStatus updateStatus() {
        return MovingMotorStatus(MotorController.getSensorPosition());
    }

    private MovingMotorStatus MovingMotorStatus(double sensorPosition) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'MovingMotorStatus'");
    }

    // MotorController.getSensorPosition();
    // MotorController.setPosition();
    // MotorController.setCurrentLimit();
    // MotorController.follow();
    // MotorController.setInverted();

}
