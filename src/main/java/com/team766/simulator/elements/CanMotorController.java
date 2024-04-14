package com.team766.simulator.elements;

import com.team766.simulator.ProgramInterface;
import com.team766.simulator.interfaces.ElectricalDevice;

public class CanMotorController extends MotorController {

    private int address;

    public CanMotorController(final int address_, final ElectricalDevice downstream) {
        super(downstream);
        this.address = address_;
    }

    @Override
    protected double getPercentOutput() {
        return ProgramInterface.canMotorControllerChannels[address].command.percentOutput;
    }

    public void setSensorPosition(double position) {
        ProgramInterface.canMotorControllerChannels[address].status.sensorPosition = position;
    }

    public void setSensorVelocity(double velocity) {
        ProgramInterface.canMotorControllerChannels[address].status.sensorVelocity = velocity;
    }
}
