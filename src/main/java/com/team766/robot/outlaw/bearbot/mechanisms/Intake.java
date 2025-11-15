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
import com.team766.robot.outlaw.bearbot.constants.SetPointConstants;

public class Intake extends MechanismWithStatus<Intake.IntakeStatus> {
    private final MotorController intakeRollerMotor;
    private final MotorController deploymentMotor;
    private final EncoderReader absoluteEncoder;
    private IntakeState state = IntakeState.STOP;

    private static final double CURRENT_LIMIT = 30.0;

    public enum IntakeState {
        IN,
        OUT,
        STOP
    }

    public enum DeploymentPosition {
        RETRACTED(90.0),
        DEPLOYED(0.0);

        private final double position;

        DeploymentPosition(double position) {
            this.position = position;
        }

        public double getPosition() {
            return position;
        }
    }

    public static record IntakeStatus(IntakeState state, double deploymentAngle)
            implements Status {}

    public Intake() {
        intakeRollerMotor = RobotProvider.instance.getMotor(ConfigConstants.INTAKE_ROLLER_MOTOR);
        intakeRollerMotor.setNeutralMode(NeutralMode.Brake);
        intakeRollerMotor.setCurrentLimit(CURRENT_LIMIT);
        deploymentMotor = RobotProvider.instance.getMotor(ConfigConstants.INTAKE_DEPLOYMENT_MOTOR);
        deploymentMotor.setNeutralMode(NeutralMode.Brake);
        deploymentMotor.setCurrentLimit(CURRENT_LIMIT);

        // Use the absolute encoder to set offset
        absoluteEncoder =
                RobotProvider.instance.getEncoder(ConfigConstants.INTAKE_ABSOLUTE_ENCODER);
        deploymentMotor.setSensorPosition(
                absoluteEncoder.getPosition() * ConfigConstants.INTAKE_GEAR_RATIO);
    }

    public void deploy(DeploymentPosition position) {
        deploy(position.getPosition());
    }

    public void deploy(double degrees) {
        deploymentMotor.set(
                ControlMode.Position, EncoderUtils.intakeDeployerDegreesToRotations(degrees));
    }

    public void in() {
        state = IntakeState.IN;
        intakeRollerMotor.set(SetPointConstants.INTAKE_IN_POWER);
    }

    public void out() {
        state = IntakeState.OUT;
        intakeRollerMotor.set(SetPointConstants.INTAKE_OUT_POWER);
    }

    public void stop() {
        state = IntakeState.STOP;
        intakeRollerMotor.set(0.0);
    }

    @Override
    protected IntakeStatus updateStatus() {
        return new IntakeStatus(
                state,
                EncoderUtils.intakeDeployerRotationsToDegrees(deploymentMotor.getSensorPosition()));
    }
}
