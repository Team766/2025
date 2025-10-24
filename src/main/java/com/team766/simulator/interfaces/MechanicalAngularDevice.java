package com.team766.simulator.interfaces;

public interface MechanicalAngularDevice {
    /**
     * @param angularPosition Position of device in radians. Should be continuous (e.g. does not roll over from PI -PI).
     * @param angularVelocity Velocity of device in radians per second.
     */
    public record State(double angularPosition, double angularVelocity) {}

    /**
     * @param torque Torque in Newton-meters
     */
    public record Action(double torque) {}

    Action step(State state, double dt);
}
