package com.team766.hal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.team766.TestCase3;
import org.junit.jupiter.api.Test;

public abstract class JoystickAbstractTest extends TestCase3 {
    protected JoystickReader joystick;

    protected abstract void setAxis(int axis, double value);

    protected abstract void setButton(int axis, boolean value);

    @Test
    public void testDeadzone() {
        // Deadzone should start at 0.0, so the condition should be true even if the value is 0.
        setAxis(0, 0.0);
        setAxis(1, 0.0);
        assertEquals(0.0, joystick.getAxis(0));
        assertEquals(0.0, joystick.getAxis(1));
        assertTrue(joystick.isAxisMoved(0));
        assertTrue(joystick.isAxisMoved(1));

        // Same result if the deadzone is explicitly set to 0.0.
        joystick.setAllAxisDeadzone(0.0);
        assertTrue(joystick.isAxisMoved(0));
        assertTrue(joystick.isAxisMoved(1));

        // Test with the deadzone larger than the axis values.
        joystick.setAllAxisDeadzone(0.6);
        assertFalse(joystick.isAxisMoved(0));
        assertFalse(joystick.isAxisMoved(1));

        // Calling setAxisDeadzone after setAllAxisDeadzone should set the deadzone for that axis
        // but maintain the deadzone for all other axes.
        setAxis(0, 0.5);
        setAxis(1, 0.3);
        joystick.setAxisDeadzone(1, 0.2);
        assertEquals(0.0, joystick.getAxis(0), 1e-7);
        assertEquals(0.3, joystick.getAxis(1), 1e-7);
        assertFalse(joystick.isAxisMoved(0));
        assertTrue(joystick.isAxisMoved(1));

        // Calling setAllAxisDeadzone should override previously-set per-axis deadzones.
        joystick.setAllAxisDeadzone(0.5);
        assertEquals(0.5, joystick.getAxis(0), 1e-7);
        assertEquals(0.0, joystick.getAxis(1), 1e-7);
        assertTrue(joystick.isAxisMoved(0));
        assertFalse(joystick.isAxisMoved(1));
    }

    @Test
    public void testWhenAxisMoved() {
        var axis0 = joystick.whenAxisMoved(0);
        var axis1 = joystick.whenAxisMoved(1);
        var axis0Or1 = joystick.whenAnyAxisMoved(0, 1);

        joystick.setAllAxisDeadzone(0.5);

        setAxis(0, 1.0);
        setAxis(1, 0.0);
        assertTrue(axis0.getAsBoolean());
        assertFalse(axis1.getAsBoolean());
        assertTrue(axis0Or1.getAsBoolean());

        setAxis(0, 0.0);
        setAxis(1, 0.0);
        assertFalse(axis0.getAsBoolean());
        assertFalse(axis1.getAsBoolean());
        assertFalse(axis0Or1.getAsBoolean());

        setAxis(0, 0.0);
        setAxis(1, 1.0);
        assertFalse(axis0.getAsBoolean());
        assertTrue(axis1.getAsBoolean());
        assertTrue(axis0Or1.getAsBoolean());

        setAxis(0, 1.0);
        setAxis(1, 1.0);
        assertTrue(axis0.getAsBoolean());
        assertTrue(axis1.getAsBoolean());
        assertTrue(axis0Or1.getAsBoolean());
    }

    @Test
    public void testWhenButton() {
        var button1 = joystick.whenButton(1);
        var button2 = joystick.whenButton(2);
        var button1Or2 = joystick.whenAnyButton(1, 2);
        var button1And2 = joystick.whenAllButtons(1, 2);

        setButton(1, true);
        setButton(2, false);
        assertTrue(button1.getAsBoolean());
        assertFalse(button2.getAsBoolean());
        assertTrue(button1Or2.getAsBoolean());
        assertFalse(button1And2.getAsBoolean());

        setButton(1, false);
        setButton(2, false);
        assertFalse(button1.getAsBoolean());
        assertFalse(button2.getAsBoolean());
        assertFalse(button1Or2.getAsBoolean());
        assertFalse(button1And2.getAsBoolean());

        setButton(1, false);
        setButton(2, true);
        assertFalse(button1.getAsBoolean());
        assertTrue(button2.getAsBoolean());
        assertTrue(button1Or2.getAsBoolean());
        assertFalse(button1And2.getAsBoolean());

        setButton(1, true);
        setButton(2, true);
        assertTrue(button1.getAsBoolean());
        assertTrue(button2.getAsBoolean());
        assertTrue(button1Or2.getAsBoolean());
        assertTrue(button1And2.getAsBoolean());
    }
}
