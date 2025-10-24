package com.team766.simulator.elements;

import com.team766.simulator.ProgramInterface;

public class CanMotorControllerSim extends MotorControllerSim {

    private int address;

    public CanMotorControllerSim(final int address_, final DCMotorSim motor) {
        super(motor);
        this.address = address_;
    }

    @Override
    protected double getCommand() {
        return ProgramInterface.canMotorControllerChannels[address].command.output;
    }

    @Override
    public Action step(State state, double dt) {
        setSensorPosition(motor.getMechanicalState().angularPosition());
        setSensorVelocity(motor.getMechanicalState().angularVelocity());

        return super.step(state, dt);
    }

    private void setSensorPosition(double position) {
        ProgramInterface.canMotorControllerChannels[address].status.sensorPosition = position;
    }

    private void setSensorVelocity(double velocity) {
        ProgramInterface.canMotorControllerChannels[address].status.sensorVelocity = velocity;
    }
}
