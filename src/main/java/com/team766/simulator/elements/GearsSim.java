package com.team766.simulator.elements;

import com.team766.simulator.interfaces.MechanicalAngularDevice;

public class GearsSim implements MechanicalAngularDevice {
    // TODO: Add rotational inertia
    // TODO: Add losses

    // Torque ratio (downstream / upstream)
    private final double torqueRatio;

    private MechanicalAngularDevice upstream;

    public GearsSim(final double torqueRatio_, final MechanicalAngularDevice upstream_) {
        this.torqueRatio = torqueRatio_;
        this.upstream = upstream_;
    }

    @Override
    public MechanicalAngularDevice.Action step(MechanicalAngularDevice.State state, double dt) {
        MechanicalAngularDevice.State upstreamState =
                new MechanicalAngularDevice.State(
                        state.angularPosition() * torqueRatio, state.angularVelocity() * torqueRatio);
        MechanicalAngularDevice.Action upstreamAction = upstream.step(upstreamState, dt);
        return new MechanicalAngularDevice.Action(upstreamAction.torque() * torqueRatio);
    }
}
