package com.team766.robot.Kevan.mechanisms;

import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;
import com.team766.common.constants.InputConstants;

public class Elevator extends MechanismWithStatus<Elevator.ElevatorStatus> {

    MotorController elevator_motor = RobotProvider.instance.getMotor("elevator_motor");
    public record ElevatorStatus(double currentPosition) implements Status {
    
    public Elevator() {}
    }
    public void move(double motorPower) {
        elevator_motor.set(motorPower);
    }
    protected ElevatorStatus updateStatus() {
        return new ElevatorStatus(currentPosition:0);
    }
 }