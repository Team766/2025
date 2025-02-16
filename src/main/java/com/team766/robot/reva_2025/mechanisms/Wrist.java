package com.team766.robot.reva_2025.mechanisms;

import com.team766.framework3.MechanismWithStatus;
import com.team766.framework3.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;

public class Wrist extends MechanismWithStatus<Wrist.WristStatus> {

    private final double thresholdConstant = 0; // TODO: Update me after testing!
    private double setPoint;
    private WristPosition wristState;
    private static final double NUDGE_AMOUNT = 1.0;

    public record WristStatus(double angle, WristPosition wristState) implements Status {}

    public enum WristPosition {

        // TODO: Change these angles to actual values
        CORAL_BOTTOM(5),
        CORAL_INTAKE(10),
        CORAL_L2_PREP(260),
        CORAL_L2_PLACE(290),
        CORAL_L3_PREP(220),
        CORAL_L3_PLACE(250),
        CORAL_L4_PREP(210),
        CORAL_L4_PLACE(240);

        private final double angle;

        private WristPosition(double angle) {
            this.angle = angle;
        }

        public double getAngle() {
            return angle;
        }
    }

    private MotorController wristMotor;

    public Wrist() {
        wristMotor = RobotProvider.instance.getMotor("Wrist.Motor");
    }

    public void setAngle(WristPosition position) {
        setAngle(position.getAngle());
        wristState = position;
    }

    public void setAngle(double angle) {
        setPoint = angle;
        wristMotor.set(MotorController.ControlMode.Position, angle);
    }

    public void nudge(double sign) {
        double nudgePosition = getStatus().angle() + (NUDGE_AMOUNT * Math.signum(sign));
        setAngle(nudgePosition);
    }

    @Override
    protected WristStatus updateStatus() {
        return new WristStatus(wristMotor.get(), wristState);
    }

    public WristPosition
            getWristState() { // added because WristStatus is inaccessible from DriverOI
        return wristState;
    }

    public boolean isAtPosition() {
        return (Math.abs(setPoint - wristMotor.getSensorPosition()) < thresholdConstant);
    }
}
