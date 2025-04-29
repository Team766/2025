package com.team766.robot.reva.mechanisms;

import static com.team766.robot.reva.constants.ConfigConstants.*;

import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.playingwithfusion.TimeOfFlight;
import com.playingwithfusion.TimeOfFlight.RangingMode;
import com.team766.config.ConfigFileReader;
import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;
import com.team766.library.ValueProvider;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Intake extends MechanismWithStatus<Intake.IntakeStatus> {
    public record IntakeStatus(boolean hasNoteInIntake, boolean isNoteClose) implements Status {}

    private record IntakePosition(double intakePower, double proximityValue) {}

    IntakePosition[] positions =
            new IntakePosition[] {
                new IntakePosition(0, 150),
                new IntakePosition(0.2, 200),
                new IntakePosition(0.4, 400),
                new IntakePosition(1.0, 480)
            };

    private static final double DEFAULT_POWER = 1.0;
    private static final double NUDGE_INCREMENT = 0.05;
    private static final double CURRENT_LIMIT = 30.0; // a little lower than max efficiency
    private static final double MAX_POWER = 1.0;
    private static final double MIN_POWER = -1 * MAX_POWER;
    private static final double IS_CLOSE_THRESHOLD = 350;

    // This should be the amount that getRange() should return less than for a note to be classified
    // as in
    private static ValueProvider<Double> threshold =
            ConfigFileReader.getInstance()
                    .getDouble("RightProximitySensor.threshold"); // needs calibration

    private MotorController intakeMotor;
    private TimeOfFlight sensor;

    private boolean setIntakePowerForSensorDistance = false;

    public Intake() {
        intakeMotor = RobotProvider.instance.getMotor(INTAKE_MOTOR);
        intakeMotor.setNeutralMode(NeutralMode.Brake);
        intakeMotor.setCurrentLimit(CURRENT_LIMIT);
        sensor = new TimeOfFlight(0); // needs calibration

        sensor.setRangingMode(RangingMode.Short, 24);
    }

    public void in() {
        intakeMotor.set(DEFAULT_POWER);
        setIntakePowerForSensorDistance = false;
    }

    public void out() {
        intakeMotor.set(-1 * DEFAULT_POWER);
        setIntakePowerForSensorDistance = false;
    }

    public void stop() {
        intakeMotor.set(0.0);
        setIntakePowerForSensorDistance = false;
    }

    public void nudgeUp() {
        intakeMotor.set(Math.min(intakeMotor.get() + NUDGE_INCREMENT, MAX_POWER));
        setIntakePowerForSensorDistance = false;
    }

    public void nudgeDown() {
        intakeMotor.set(Math.max(intakeMotor.get() - NUDGE_INCREMENT, MIN_POWER));
        setIntakePowerForSensorDistance = false;
    }

    public void setIntakePowerFromSensorDistance() {
        setIntakePowerForSensorDistance = true;
    }

    @Override
    protected void onMechanismIdle() {
        stop();
    }

    @Override
    protected void run() {
        if (setIntakePowerForSensorDistance) {
            intakeMotor.set(
                    com.team766.math.Math.interpolate(
                            positions,
                            sensor.getRange(),
                            IntakePosition::proximityValue,
                            IntakePosition::intakePower));
        }
    }

    @Override
    protected IntakeStatus updateStatus() {
        // SmartDashboard.putNumber("[INTAKE POWER]", intakePower);
        // SmartDashboard.putNumber("[INTAKE] Current", MotorUtil.getCurrentUsage(intakeMotor));
        SmartDashboard.putNumber("Prox Sensor", sensor.getRange());
        return new IntakeStatus(
                (threshold.get()) > sensor.getRange() && sensor.isRangeValid(),
                (IS_CLOSE_THRESHOLD) > sensor.getRange() && sensor.isRangeValid());
    }
}
