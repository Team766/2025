package com.team766.simulator.elements;

import com.team766.simulator.ProgramInterface;

public class PwmMotorControllerSim extends MotorControllerSim {

    private int channel;

    public PwmMotorControllerSim(final int channel, final DCMotorSim downstream) {
        super(downstream);

        this.channel = channel;
    }

    @Override
    protected double getCommand() {
        return ProgramInterface.pwmChannels[channel];
    }
}
