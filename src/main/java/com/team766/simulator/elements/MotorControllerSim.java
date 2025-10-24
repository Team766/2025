package com.team766.simulator.elements;

import com.team766.simulator.interfaces.ElectricalDevice;

public abstract class MotorControllerSim implements ElectricalDevice {
    protected final DCMotorSim motor;

    public MotorControllerSim(final DCMotorSim motor) {
        this.motor = motor;
    }

    // [-1, 1] representing the command sent from the application processor
    protected abstract double getCommand();

    @Override
    public ElectricalDevice.Action step(ElectricalDevice.State state, double dt) {
        double command = getCommand();
        ElectricalDevice.State motorState = new ElectricalDevice.State(state.voltage() * command);
        ElectricalDevice.Action motorAction = motor.step(motorState, dt);
        return new Action(Math.max(0, motorAction.current() * Math.signum(command)));
    }

    @Override
    public String name() {
        return "MotorController:" + motor.name();
    }
}
