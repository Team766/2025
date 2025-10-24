package com.team766.simulator.elements;

import com.team766.simulator.interfaces.MechanicalAngularDevice;
import com.team766.simulator.interfaces.MechanicalDevice;

// Simulate a wheel revolving around the positive Y axis.
public class WheelSim implements MechanicalDevice {
    // TODO: Add rotational inertia

    // Diameter of the wheel in meters
    private final double diameter;

    private final MechanicalAngularDevice upstream;

    public WheelSim(double diameter, MechanicalAngularDevice upstream) {
        this.diameter = diameter;
        this.upstream = upstream;
    }

    @Override
    public MechanicalDevice.Action step(MechanicalDevice.State state, double dt) {
        MechanicalAngularDevice.State upstreamState =
                new MechanicalAngularDevice.State(
                        state.position() * 2. / diameter, state.velocity() * 2. / diameter);
        MechanicalAngularDevice.Action upstreamAction = upstream.step(upstreamState, dt);
        double appliedForce = upstreamAction.torque() * 2. / diameter;
        return new MechanicalDevice.Action(appliedForce);
    }
}
