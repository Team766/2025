package com.team766.simulator.elements;

import com.ctre.phoenix6.hardware.CANcoder;
import com.ctre.phoenix6.sim.CANcoderSimState;
import com.team766.simulator.interfaces.MechanicalAngularDevice;

public class CANcoderSim {
    private final CANcoderSimState simState;

    public CANcoderSim(CANcoder device) {
        this.simState = device.getSimState();
    }

    public void step(MechanicalAngularDevice.State state) {
        simState.setVelocity(state.angularVelocity() / (2 * Math.PI));
        simState.setRawPosition(state.angularPosition() / (2 * Math.PI));
    }
}
