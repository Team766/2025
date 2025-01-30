package com.team766.robot.reva_2025.mechanisms;

import com.team766.framework3.MechanismWithStatus;
import com.team766.framework3.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;

public class Wrist extends MechanismWithStatus<Wrist.WristStatus> {

    public record WristStatus(double angle) implements Status {}

    public enum WristPosition {

        // TODO: update all of these to real things!
        PICKUP_CORAL(0),
        PLACE_CORAL(180);

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
    }

    public void setAngle(double angle) {
        wristMotor.set(MotorController.ControlMode.Position, angle);
    }

    @Override
    protected WristStatus updateStatus() {
        return new WristStatus(wristMotor.get());
    }
}
