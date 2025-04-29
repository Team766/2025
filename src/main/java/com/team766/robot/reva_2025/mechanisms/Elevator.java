package com.team766.robot.reva_2025.mechanisms;

import com.team766.config.ConfigFileReader;
import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.EncoderReader;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;
import com.team766.hal.TimeOfFlightReader;
import com.team766.library.ValueProvider;
import com.team766.math.Maths;
import com.team766.robot.reva_2025.constants.ConfigConstants;

public class Elevator extends MechanismWithStatus<Elevator.ElevatorStatus> {
    private MotorController elevatorLeftMotor;
    private static final double NUDGE_AMOUNT = 5;
    private static final double POSITION_LOCATION_THRESHOLD = 1;
    private final EncoderReader absoluteEncoder;
    private final TimeOfFlightReader timeOfFlight;
    private final ValueProvider<Double> ffGain;
    private boolean encoderInitialized = false;
    private double setPoint;
    private boolean noPIDMode;

    public static record ElevatorStatus(double currentHeight, double targetHeight)
            implements Status {
        public boolean isAtHeight() {
            return Math.abs(targetHeight() - currentHeight()) < POSITION_LOCATION_THRESHOLD;
        }
    }

    public enum ElevatorPosition {
        ELEVATOR_TOP(28.4),
        ELEVATOR_BOTTOM(0.5),
        ELEVATOR_INTAKE(14.5),
        ELEVATOR_L1(2),
        ELEVATOR_L2(0.5),
        ELEVATOR_L3(2.5),
        ELEVATOR_L4(ELEVATOR_TOP.getHeight()),
        ELEVATOR_CLIMB(5);

        final double height;

        ElevatorPosition(double height) {
            this.height = height;
        }

        public double getHeight() {
            return height;
        }
    }

    public Elevator() {
        elevatorLeftMotor = RobotProvider.instance.getMotor(ConfigConstants.LEFT_ELEVATOR_MOTOR);
        absoluteEncoder = RobotProvider.instance.getEncoder(ConfigConstants.ELEVATOR_ENCODER);
        timeOfFlight =
                RobotProvider.instance.getTimeOfFlight(ConfigConstants.ELEVATOR_INTAKESENSOR);
        ffGain = ConfigFileReader.instance.getDouble(ConfigConstants.ELEVATOR_FFGAIN);
        elevatorLeftMotor.setCurrentLimit(40);
        setPoint = ElevatorPosition.ELEVATOR_BOTTOM.getHeight();
    }

    public void setPosition(double setPosition) {
        noPIDMode = false;
        setPoint =
                Maths.clamp(
                        setPosition,
                        ElevatorPosition.ELEVATOR_BOTTOM.getHeight(),
                        ElevatorPosition.ELEVATOR_TOP.getHeight());
    }

    public void setPosition(ElevatorPosition position) {
        setPosition(position.getHeight());
    }

    public void nudge(double sign) {
        double nudgePosition =
                elevatorLeftMotor.getSensorPosition() + (NUDGE_AMOUNT * Math.signum(sign));
        setPosition(nudgePosition);
    }

    public void nudgeNoPID(double power) {
        noPIDMode = true;
        elevatorLeftMotor.set(power);
    }

    @Override
    protected void run() {
        if (!noPIDMode) {
            elevatorLeftMotor.set(
                    MotorController.ControlMode.Position,
                    EncoderUtils.elevatorHeightToRotations(setPoint),
                    ffGain.valueOr(0.0));
        }
    }

    @Override
    protected ElevatorStatus updateStatus() {
        if (!encoderInitialized
                && absoluteEncoder.isConnected()
                && timeOfFlight.wasLastMeasurementValid()
                && timeOfFlight.getDistance().isPresent()) {
            double encoderPos = absoluteEncoder.getPosition();
            double timeOfFlightReading =
                    timeOfFlight.getDistance().get() * 39.37
                            - 1.8; // to inches, zero is at bottom of elevator
            double convertedPos =
                    timeOfFlightReading
                            + Math.IEEEremainder(
                                    encoderPos * 1.61 * Math.PI - timeOfFlightReading,
                                    EncoderUtils.ELEVATOR_ABSOLUTE_ENCODER_RANGE);
            elevatorLeftMotor.setSensorPosition(
                    EncoderUtils.elevatorHeightToRotations(convertedPos));
            encoderInitialized = true;
        }
        return new ElevatorStatus(
                EncoderUtils.elevatorRotationsToHeight(elevatorLeftMotor.getSensorPosition()),
                setPoint);
    }
}
