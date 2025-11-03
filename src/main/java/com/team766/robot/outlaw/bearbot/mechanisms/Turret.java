package com.team766.robot.outlaw.bearbot.mechanisms;

import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.EncoderReader;
import com.team766.hal.MotorController;
import com.team766.hal.MotorController.ControlMode;
import com.team766.hal.RobotProvider;
import com.team766.robot.outlaw.bearbot.EncoderUtils;
import com.team766.robot.outlaw.bearbot.constants.ConfigConstants;

public class Turret extends MechanismWithStatus<Turret.TurretStatus> {
    public static record TurretStatus(double position) implements Status {}

    public enum TurretPosition {
        LOW(-45.0),
        NEUTRAL(0.0),
        HIGH(45.0);

        private final double position;

        TurretPosition(double position) {
            this.position = position;
        }

        public double getPosition() {
            return position;
        }
    }

    private final MotorController turretMotor;
    private final EncoderReader absoluteEncoder;

    public Turret() {
        turretMotor = RobotProvider.instance.getMotor(ConfigConstants.TURRET_MOTOR);
        absoluteEncoder =
                RobotProvider.instance.getEncoder(ConfigConstants.TURRET_ABSOLUTE_ENCODER);
    }

    public void rotate(TurretPosition position) {
        rotate(position.getPosition());
    }

    public void rotate(double degrees) {
        turretMotor.set(ControlMode.Position, EncoderUtils.turretDegreesToRotations(degrees));
    }

    @Override
    protected TurretStatus updateStatus() {
        return new TurretStatus(
                EncoderUtils.turretRotationsToDegrees(turretMotor.getSensorPosition()));
    }
}
