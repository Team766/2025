package com.team766.simulator.elements;

/* Code retained for reference. TODO: Remove this

import com.team766.simulator.ProgramInterface;
import com.team766.simulator.interfaces.MechanicalAngularDevice;

public class EncoderSim {
    public static final int DEFAULT_ENCODER_TICKS_PER_REVOLUTION = 256;

    private final int channel;
    private final double encoderTicksPerRadian;

    public EncoderSim(int channel, double encoderTicksPerRevolution) {
        this.channel = channel;
        this.encoderTicksPerRadian = encoderTicksPerRevolution / (2 * Math.PI);
    }

    public void step(MechanicalAngularDevice.State state, double dt) {
        ProgramInterface.encoderChannels[channel].rate =
                encoderTicksPerRadian * state.angularVelocity();
        ProgramInterface.encoderChannels[channel].distance =
                (long) (encoderTicksPerRadian * state.angularPosition());
    }
}
*/
