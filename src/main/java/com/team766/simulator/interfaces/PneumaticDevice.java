package com.team766.simulator.interfaces;

public interface PneumaticDevice {
    /**
     * @param pressure Pascals (relative pressure)
     */
    record State(double pressure) {}

    /**
     * Note that an expanding volume (such as a cylinder expanding)
     * should increase volume, but have 0 flow volume because no
     * pressurized air is actually leaving the system.
     *
     * @param flowVolume
     *     Volumetric flow (delta m^3 at atmospheric pressure)
     *     Positive flow is into the system, e.g. from a compressor
     *     Negative flow is out of the system, e.g. from venting to atmosphere
     * @param deviceVolume
     *     Volume of air that the device contains (m^3)
     */
    record Action(double flowVolume, double deviceVolume) {}

    Action step(State state, double dt);
}
