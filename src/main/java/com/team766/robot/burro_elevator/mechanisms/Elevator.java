package com.team766.robot.burro_elevator.mechanisms;

import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.MotorController;
import com.team766.hal.MotorController.ControlMode;
import com.team766.hal.RobotProvider;

public class Elevator extends MechanismWithStatus<Elevator.ElevatorStatus> {
    public record ElevatorStatus(double position) implements Status {}

    private static final double MOTOR_ROTATIONS_TO_ELEVATOR_POSITION =
            (0.25 /*chain pitch = distance per tooth*/)
                    * (18. /*teeth per rotation of sprocket*/)
                    * (1. / (3. * 4. * 4.) /*planetary gearbox*/);

    private final MotorController motor;

    public Elevator() {
        motor = RobotProvider.instance.getMotor("elevator.Motor");
    }

    public void setPower(final double power) {
        motor.set(power);
    }

    public void setPosition(final double position) {
        motor.set(ControlMode.Position, position / MOTOR_ROTATIONS_TO_ELEVATOR_POSITION);
    }

    @Override
    protected ElevatorStatus updateStatus() {
        return new ElevatorStatus(motor.getSensorPosition() * MOTOR_ROTATIONS_TO_ELEVATOR_POSITION);
    }
}
