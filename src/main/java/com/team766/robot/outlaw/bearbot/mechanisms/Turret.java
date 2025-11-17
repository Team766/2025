package com.team766.robot.outlaw.bearbot.mechanisms;

import com.ctre.phoenix.motorcontrol.NeutralMode;
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

    private final MotorController turretMotor;
    private final EncoderReader absoluteEncoder;

    private static final double CURRENT_LIMIT = 30.0;

    public Turret() {
        turretMotor = RobotProvider.instance.getMotor(ConfigConstants.TURRET_MOTOR);
        turretMotor.setNeutralMode(NeutralMode.Brake);
        turretMotor.setCurrentLimit(CURRENT_LIMIT);

        absoluteEncoder =
                RobotProvider.instance.getEncoder(ConfigConstants.TURRET_ABSOLUTE_ENCODER);
        turretMotor.setSensorPosition(
                absoluteEncoder.getPosition() * ConfigConstants.TURRET_GEAR_RATIO);
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
