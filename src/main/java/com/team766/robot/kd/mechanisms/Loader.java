package com.team766.robot.kd.mechanisms;

import com.team766.framework.MechanismWithStatus;
import com.team766.framework.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;

public class Loader extends MechanismWithStatus<Loader.LoaderStatus> {
    MotorController loaderMotor = RobotProvider.instance.getMotor("shooter");
    double power;

    public record LoaderStatus(double pos) implements Status {}

    public Loader(double loader_power) {
        power = loader_power;
    }

    public void stop() {
        loaderMotor.set(0);
    }

    public void run() {
        loaderMotor.set(power);
    }

    protected LoaderStatus updateStatus() {
        return new LoaderStatus(loaderMotor.getSensorPosition());
    }
}
