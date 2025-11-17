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

public class Deployment extends MechanismWithStatus<Deployment.DeploymentStatus> {
    private final MotorController deploymentMotor;
    private final EncoderReader absoluteEncoder;
    private DeploymentState state = DeploymentState.RETRACTED;

    private static final double CURRENT_LIMIT = 30.0;

    public enum DeploymentState {
        DEPLOYED,
        RETRACTED
    }

    public static record DeploymentStatus(DeploymentState state, double deploymentAngle)
            implements Status {}

    public Deployment() {
        deploymentMotor = RobotProvider.instance.getMotor(ConfigConstants.INTAKE_DEPLOYMENT_MOTOR);
        deploymentMotor.setNeutralMode(NeutralMode.Brake);
        deploymentMotor.setCurrentLimit(CURRENT_LIMIT);

        // Use the absolute encoder to set offset
        absoluteEncoder =
                RobotProvider.instance.getEncoder(ConfigConstants.INTAKE_ABSOLUTE_ENCODER);
        deploymentMotor.setSensorPosition(
                absoluteEncoder.getPosition() * ConfigConstants.INTAKE_GEAR_RATIO);
    }

    public void deploy(double degrees) {
        deploymentMotor.set(
                ControlMode.Position, EncoderUtils.intakeDeployerDegreesToRotations(degrees));
    }

    @Override
    protected DeploymentStatus updateStatus() {
        return new DeploymentStatus(
                state,
                EncoderUtils.intakeDeployerRotationsToDegrees(deploymentMotor.getSensorPosition()));
    }
}
