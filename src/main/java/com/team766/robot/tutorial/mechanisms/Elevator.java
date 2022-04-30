package com.team766.robot.tutorial.mechanisms;

import com.team766.config.ConfigFileReader;
import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.EncoderReader;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;
import com.team766.library.ValueProvider;
import com.team766.logging.Severity;

public class Elevator extends MechanismWithStatus<Elevator.ElevatorStatus> {
    public record ElevatorStatus(double position, double setpoint) implements Status {}

    private MotorController motor;
    private EncoderReader encoder;

    private double setpoint;
    private boolean enabled = false;

    private ValueProvider<Double> gain;
    private ValueProvider<Double> minPosition;
    private ValueProvider<Double> maxPosition;

    public Elevator() {
        motor = RobotProvider.instance.getMotor("elevator.motor");
        encoder = RobotProvider.instance.getEncoder("elevator.encoder");
        gain = ConfigFileReader.instance.getDouble("elevator.control_gain");
        minPosition = ConfigFileReader.instance.getDouble("elevator.min_position");
        maxPosition = ConfigFileReader.instance.getDouble("elevator.max_position");
    }

    public void setSetpoint(double position) {
        setpoint = position;
        if (minPosition.hasValue()) {
            setpoint = Math.max(setpoint, minPosition.get());
        }
        if (maxPosition.hasValue()) {
            setpoint = Math.min(setpoint, maxPosition.get());
        }
        enabled = true;
    }

    @Override
    protected ElevatorStatus updateStatus() {
        return new ElevatorStatus(encoder.getDistance(), setpoint);
    }

    @Override
    public void run() {
        if (!enabled) {
            return;
        }
        if (!gain.hasValue()) {
            log(Severity.ERROR, "Control gain not specified in config file");
            return;
        }

        double error = setpoint - getStatus().position();
        double power = gain.get() * error;
        motor.set(power);
    }
}
