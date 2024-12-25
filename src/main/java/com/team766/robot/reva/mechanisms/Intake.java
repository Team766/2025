package com.team766.robot.reva.mechanisms;

import static com.team766.robot.reva.constants.ConfigConstants.*;

import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.playingwithfusion.TimeOfFlight;
import com.playingwithfusion.TimeOfFlight.RangingMode;
import com.team766.config.ConfigFileReader;
import com.team766.framework3.Mechanism;
import com.team766.framework3.Request;
import com.team766.framework3.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;
import com.team766.library.ValueProvider;

public class Intake extends Mechanism<Intake.IntakeStatus> {
    public record IntakeStatus(double motorPower, boolean hasNoteInIntake, boolean isNoteClose)
            implements Status {}

    public Request<Intake> setMotorPower(double power) {
        return setRequest(intakeMotor.requestPercentOutput(power));
    }

    public Request<Intake> requestIn() {
        return setMotorPower(DEFAULT_POWER);
    }

    public Request<Intake> requestOut() {
        return setMotorPower(-1 * DEFAULT_POWER);
    }

    public Request<Intake> requestStop() {
        return setMotorPower(0.0);
    }

    public Request<Intake> requestNudgeUp() {
        return setMotorPower(Math.min(getStatus().motorPower() + NUDGE_INCREMENT, MAX_POWER));
    }

    public Request<Intake> requestNudgeDown() {
        return setMotorPower(Math.max(getStatus().motorPower() - NUDGE_INCREMENT, MIN_POWER));
    }

    public Request<Intake> requestPowerForSensorDistance() {
        return setRequest(
                () -> {
                    intakeMotor.set(
                            com.team766.math.Math.interpolate(
                                    positions,
                                    sensor.getRange(),
                                    IntakePosition::proximityValue,
                                    IntakePosition::intakePower));
                    return true;
                });
    }

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

    public Intake() {
        intakeMotor = RobotProvider.instance.getMotor(INTAKE_MOTOR);
        intakeMotor.setNeutralMode(NeutralMode.Brake);
        intakeMotor.setCurrentLimit(CURRENT_LIMIT);
        sensor = new TimeOfFlight(0); // needs calibration

        sensor.setRangingMode(RangingMode.Short, 24);
    }

    @Override
    protected Request<Intake> applyIdleRequest() {
        return requestStop();
    }

    @Override
    protected IntakeStatus reportStatus() {
        // SmartDashboard.putNumber("[INTAKE POWER]", intakePower);
        // SmartDashboard.putNumber("[INTAKE] Current", MotorUtil.getCurrentUsage(intakeMotor));
        // SmartDashboard.putNumber("Prox Sensor", sensor.getRange());

        return new IntakeStatus(
                intakeMotor.get(),
                (threshold.get()) > sensor.getRange() && sensor.isRangeValid(),
                (IS_CLOSE_THRESHOLD) > sensor.getRange() && sensor.isRangeValid());
    }
}
