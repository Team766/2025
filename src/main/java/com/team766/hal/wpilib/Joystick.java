package com.team766.hal.wpilib;

import com.team766.hal.JoystickReader;
import com.team766.math.Maths;
import java.util.HashMap;
import java.util.Map;

public class Joystick extends edu.wpi.first.wpilibj.Joystick implements JoystickReader {
    private final Map<Integer, Double> axisDeadzoneMap = new HashMap<>();
    private double defaultAxisDeadzone = 0.0;

    public Joystick(final int port) {
        super(port);
    }

    @Override
    public double getAxis(final int axis) {
        return Maths.deadzone(
                getRawAxis(axis), axisDeadzoneMap.getOrDefault(axis, defaultAxisDeadzone));
    }

    @Override
    public boolean isAxisMoved(int axis) {
        return Math.abs(getRawAxis(axis))
                >= axisDeadzoneMap.getOrDefault(axis, defaultAxisDeadzone);
    }

    @Override
    public void setAxisDeadzone(int axis, double deadzone) {
        axisDeadzoneMap.put(axis, deadzone);
    }

    @Override
    public void setAllAxisDeadzone(double deadzone) {
        axisDeadzoneMap.clear();
        defaultAxisDeadzone = deadzone;
    }

    @Override
    public boolean getButton(final int button) {
        return getRawButton(button);
    }
}
