package com.team766.robot.Jordan.mechanisms;

<<<<<<< HEAD
=======
import com.team766.framework.MechanismWithStatus;
>>>>>>> 7b9ac55c4804e634feb9f152307033780becd5a1
import com.team766.framework.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;
import com.team776.framework.MechanismWithStatus;

public class MovingMotor extends MechanismWithStatus<MovingMotor.MovingMotorStatus> {
<<<<<<< HEAD

    public MovingMotor() {}

    MotorController motor = RobotProvider.instance.getMotor("motor");

    public record MovingMotorStatus(double currentPosition) implements Status {}
=======

    public MovingMotor () {
    }

    MotorController motor = RobotProvider.instance.getMotor("motor");

    public record MovingMotorStatus(double currentPosition) implements Status {
    }
>>>>>>> 7b9ac55c4804e634feb9f152307033780becd5a1

    public void setMotorPower(double power) {
        motor.set(power);
    }

    protected MovingMotorStatus updateStatus() {
<<<<<<< HEAD
        return MovingMotorStatus(MotorController.getSensorPosition());
=======
        return new MovingMotorStatus(motor.getSensorPosition());
>>>>>>> 7b9ac55c4804e634feb9f152307033780becd5a1
    }

    private MovingMotorStatus MovingMotorStatus(double sensorPosition) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'MovingMotorStatus'");
    }

<<<<<<< HEAD
    // MotorController.getSensorPosition();
    // MotorController.setPosition();
    // MotorController.setCurrentLimit();
    // MotorController.follow();
    // MotorController.setInverted();

}
=======
}
>>>>>>> 7b9ac55c4804e634feb9f152307033780becd5a1
