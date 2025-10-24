package com.team766.simulator.elements;

import com.team766.simulator.interfaces.PneumaticDevice;

public class AirReservoirSim implements PneumaticDevice {

    private double volume;

    /**
     * @param volume Volume that the device contains (m^3)
     */
    public AirReservoirSim(final double volume) {
        this.volume = volume;
    }

    @Override
    public PneumaticDevice.Action step(PneumaticDevice.State state, double dt) {
        return new PneumaticDevice.Action(0, volume);
    }
}
