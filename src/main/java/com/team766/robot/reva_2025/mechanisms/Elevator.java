package com.team766.robot.reva_2025.mechanisms;

import com.team766.framework.Mechanism;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;

public class Elevator extends Mechanism {
    private MotorController elevatorLeftMotor;
    private MotorController elevatorRightMotor;
    private final double MIN_HEIGHT = 0;
    private final double MAX_HEIGHT = 150;
    private final double NUDGE_AMOUNT = 5;
    private double setPoint = 0;
    private final double thresholdConstant = 0; // TODO: Update me after testing!

    // values are untested and are set to change

    public Elevator() {
        elevatorLeftMotor = RobotProvider.instance.getMotor("elevator.leftMotor");
        elevatorRightMotor = RobotProvider.instance.getMotor("elevator.rightMotor");
        elevatorRightMotor.follow(elevatorLeftMotor);
        elevatorLeftMotor.setSensorPosition(0);
        setPoint = 0;
    }

    public void setPosition(double setPosition) {
        checkContextOwnership();
        setPoint = setPosition;
        if (setPosition >= MIN_HEIGHT && setPosition <= MAX_HEIGHT) {
            elevatorLeftMotor.set(MotorController.ControlMode.Position, setPosition);
        }
    }

    public void nudgeUp() {
        checkContextOwnership();
        double nudgePosition = elevatorLeftMotor.getSensorPosition() + NUDGE_AMOUNT;
        setPoint = elevatorLeftMotor.getSensorPosition() + NUDGE_AMOUNT;
        setPosition(nudgePosition);
    }

    public void nudgeDown() {
        checkContextOwnership();
        double nudgePosition = elevatorLeftMotor.getSensorPosition() - NUDGE_AMOUNT;
        setPoint = elevatorLeftMotor.getSensorPosition() - NUDGE_AMOUNT;
        setPosition(nudgePosition);
    }

    
    public boolean isAtPosition(){ return (Math.abs(setPoint - elevatorLeftMotor.getSensorPosition()) < thresholdConstant);}
    
}
