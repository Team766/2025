package com.team766.simulator.elements;

import com.team766.simulator.PhysicalConstants;
import com.team766.simulator.interfaces.MechanicalDevice;
import com.team766.simulator.interfaces.PneumaticDevice;

public class SingleActingPneumaticCylinderSim implements PneumaticDevice, MechanicalDevice {
    private final double boreDiameter;
    private final double stroke;
    private final double returnSpringForce;

    private boolean isExtended = false;
    private boolean commandExtended = false;

    private PneumaticDevice.State pneumaticState = new PneumaticDevice.State(0);

    public SingleActingPneumaticCylinderSim(
            final double boreDiameter_, final double stroke_, final double returnSpringForce_) {
        this.boreDiameter = boreDiameter_;
        this.stroke = stroke_;
        this.returnSpringForce = returnSpringForce_;
    }

    public void setExtended(final boolean state) {
        commandExtended = state;
    }

    @Override
    public PneumaticDevice.Action step(PneumaticDevice.State state, double dt) {
        pneumaticState = state;
        PneumaticDevice.Action action;
        double deviceVolume = isExtended ? boreArea() * stroke : 0;
        if (isExtended && !commandExtended) {
            action =
                    new PneumaticDevice.Action(
                            -deviceVolume
                                    * (state.pressure() + PhysicalConstants.ATMOSPHERIC_PRESSURE)
                                    / PhysicalConstants.ATMOSPHERIC_PRESSURE,
                            deviceVolume);
        } else {
            action = new PneumaticDevice.Action(0, deviceVolume);
        }
        isExtended = commandExtended;
        return action;
    }

    @Override
    public MechanicalDevice.Action step(MechanicalDevice.State state, double dt) {
        final double force = isExtended ? boreArea() * pneumaticState.pressure() : -returnSpringForce;
        return new MechanicalDevice.Action(force);
    }

    private double boreArea() {
        return Math.PI * Math.pow(boreDiameter / 2., 2);
    }
}
