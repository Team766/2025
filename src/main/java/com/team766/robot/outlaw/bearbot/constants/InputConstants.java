package com.team766.robot.outlaw.bearbot.constants;

/**
 * Constants used for the Operator Interface, eg for joyticks, buttons, dials, etc.
 */
public final class InputConstants {

    // Joysticks
    public static final int DRIVER_CONTROLLER = 0;

    // Axes
    public static final int LEFTSTICK_AXIS_LEFT_RIGHT = 0;
    public static final int LEFTSTICK_AXIS_FORWARD_BACKWARD = 1;
    public static final int RIGHTSTICK_AXIS_LEFT_RIGHT = 4; // double-check this
    public static final int RIGHTSTICK_AXIS_FORWARD_BACKWARD = 5; // double-check this

    // Buttons
    public static final int BUTTON_RESET_GYRO = 7; // Start button
    public static final int BUTTON_RESET_POS = 8; // Back button
    public static final int BUTTON_FINE_DRIVING = 5; // Right bumper
}
