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
    public static final int RIGHTSTICK_AXIS_LEFT_RIGHT = 4;
    public static final int RIGHTSTICK_AXIS_FORWARD_BACKWARD = 5;

    // POV (Note: 0 is up, 180 is down, increases clockwise, no button pressed returns -1)
    public static final int POV_TURRET_LEFT = 270; // POV left (negative) right (positive)
    public static final int POV_TURRET_RIGHT = 90; // POV left (negative) right (positive)
    public static final int POV_TURRET_CENTER = 0; // POV center

    // Buttons
    public static final int BUTTON_RESET_GYRO = 7; // Start button
    // public static final int BUTTON_RESET_POS = 8;     // Back button
    public static final int BUTTON_FINE_DRIVING = 10; // Right stick button
    //
    public static final int BUTTON_DEPLOY = 2; // The "B" button (toggles deployment)
    public static final int BUTTON_INTAKE_IN = 5; // Left bumper button (toggles intake on/off)
    public static final int BUTTON_INTAKE_OUT = 4; // Left stick button
    public static final int BUTTON_FEEDER_IN = 3; // The "X" button (starts feeder)
    public static final int BUTTON_FEEDER_OUT =
            1; // The "A" button (ends feeder, reverses feeder if currently stopped)
    public static final int BUTTON_SHOOT = 6; // Right bumper button (must be held to shoot)
}
