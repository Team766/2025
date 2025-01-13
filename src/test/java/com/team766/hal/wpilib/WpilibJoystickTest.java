package com.team766.hal.wpilib;

import com.team766.hal.JoystickAbstractTest;
import edu.wpi.first.wpilibj.simulation.JoystickSim;
import org.junit.jupiter.api.BeforeEach;

public class WpilibJoystickTest extends JoystickAbstractTest {
    private JoystickSim driver;

    @BeforeEach
    public void setUp() {
        joystick = new Joystick(0);
        driver = new JoystickSim(0);
    }

    @Override
    protected void setAxis(int axis, double value) {
        driver.setRawAxis(axis, value);
        updateDriverStationData();
    }

    @Override
    protected void setButton(int button, boolean value) {
        driver.setRawButton(button, value);
        updateDriverStationData();
    }
}
