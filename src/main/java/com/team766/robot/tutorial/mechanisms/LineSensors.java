package com.team766.robot.tutorial.mechanisms;

import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.DigitalInputReader;
import com.team766.hal.RobotProvider;

public class LineSensors extends MechanismWithStatus<LineSensors.LineSensorsStatus> {
    public record LineSensorsStatus(boolean left, boolean center, boolean right)
            implements Status {}

    private DigitalInputReader lineSensorLeft;
    private DigitalInputReader lineSensorCenter;
    private DigitalInputReader lineSensorRight;

    public LineSensors() {
        lineSensorLeft = RobotProvider.instance.getDigitalInput("lineSensorLeft");
        lineSensorCenter = RobotProvider.instance.getDigitalInput("lineSensorCenter");
        lineSensorRight = RobotProvider.instance.getDigitalInput("lineSensorRight");
    }

    @Override
    protected LineSensorsStatus updateStatus() {
        return new LineSensorsStatus(
                lineSensorLeft.get(), lineSensorCenter.get(), lineSensorRight.get());
    }
}
