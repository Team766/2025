package com.team766.simulator.elements;

import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.sim.TalonFXSimState;
import com.team766.simulator.interfaces.ElectricalDevice;

public class TalonFXSim implements ElectricalDevice {
    private final TalonFXSimState simState;
    private final DCMotorSim motor;

    public TalonFXSim(TalonFX device, DCMotorSim motor) {
        this.simState = device.getSimState();
        this.motor = motor;
    }

    @Override
    public ElectricalDevice.Action step(ElectricalDevice.State state, double dt) {
        simState.setRawRotorPosition(motor.getMechanicalState().angularPosition());
        simState.setRotorVelocity(motor.getMechanicalState().angularVelocity());
        simState.setSupplyVoltage(state.voltage());
        // TODO: simState.setRotorAcceleration();

        final double motorVoltage = simState.getMotorVoltage();
        ElectricalDevice.State motorState = new ElectricalDevice.State(motorVoltage);
        ElectricalDevice.Action motorAction = motor.step(motorState, dt);
        return new Action(Math.max(0, motorAction.current() * Math.signum(motorVoltage)));
    }

    @Override
    public String name() {
        return "MotorController:" + motor.name();
    }
}
