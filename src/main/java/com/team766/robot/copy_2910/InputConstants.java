package com.team766.robot.copy_2910;

public final class InputConstants extends com.team766.robot.common.constants.InputConstants {
    public static final int LEFT_JOYSTICK = 0;
    public static final int RIGHT_JOYSTICK = 1;
    public static final int BOXOP_GAMEPAD = 2;

    // All controls on gamepad
    public static final int BUTTON_ELEVATOR_WRIST_L1 = GAMEPAD_A_BUTTON;
    public static final int BUTTON_ELEVATOR_WRIST_L2 = GAMEPAD_X_BUTTON;
    public static final int BUTTON_ELEVATOR_WRIST_L3 = GAMEPAD_B_BUTTON;
    public static final int BUTTON_ELEVATOR_WRIST_L4 = GAMEPAD_Y_BUTTON;
    public static final int BUTTON_ELEVATOR_WRIST_MOVE_TARGETPOSITION = GAMEPAD_RIGHT_BUMPER_BUTTON;
    public static final int BUTTON_ALGAE_INTAKE_STOW = GAMEPAD_DPAD_DOWN;
    public static final int BUTTON_ALGAE_INTAKE_GROUND = GAMEPAD_DPAD_RIGHT;
    public static final int BUTTON_ALGAE_INTAKE_L2_L3 = GAMEPAD_DPAD_LEFT;
    public static final int BUTTON_ALGAE_INTAKE_L3_L4 = GAMEPAD_DPAD_UP;
    public static final int BUTTON_ALGAE_INTAKE_MOVE_TARGETPOSITION = GAMEPAD_LEFT_BUMPER_BUTTON;
    public static final int BUTTON_ALGAE_MOTOR_INTAKE_POWER = GAMEPAD_LEFT_TRIGGER;
    public static final int BUTTON_ALGAE_SHOOTER_ON = GAMEPAD_RIGHT_TRIGGER;
    public static final int BUTTON_CLIMB = GAMEPAD_BACK_BUTTON;
    public static final int AXIS_ELEVATOR_FINETUNE = GAMEPAD_LEFT_STICK_YAXIS;
    public static final int AXIS_WRIST_FINETUNE = GAMEPAD_RIGHT_STICK_YAXIS;
    public static final int AXIS_ALGAE_FINETUNE = GAMEPAD_LEFT_STICK_YAXIS;

    // Controls on joysticks
    public static final int BUTTON_CORAL_PLACE = JOYSTICK_RIGHT_BUTTON;
    public static final int BUTTON_ALGAE_SHOOT = JOYSTICK_LEFT_BUTTON;
    public static final int BUTTON_AUTO_SHOOT = JOYSTICK_TRIGGER;
    public static final int BUTTON_CORAL_AUTO_PLACE_LEFT = JOYSTICK_LEFT_BUTTON;
    public static final int BUTTON_CORAL_AUTO_PLACE_RIGHT = JOYSTICK_RIGHT_BUTTON;
    public static final int BUTTON_WINCH_CLIMBER = JOYSTICK_BOTTOM_BUTTON;

    // Macropad buttons
    public static final int CONTROL_ALGAE = 1;
    public static final int CONTROL_ELEVATOR = 2;
    public static final int CONTROL_WRIST = 3;
    public static final int CONTROL_CLIMBER = 4;
    public static final int INTAKE_IN = 5;
    public static final int INTAKE_OUT = 6;
    public static final int NUDGE_NO_PID = 7;
    public static final int NUDGE_UP = 8;
    public static final int STOW_POSITION = 9;
    public static final int ALGAE_SHOOTER_ON = 10;
    public static final int ALGAE_SHOOTER_FEED = 11;
    public static final int NUDGE_DOWN = 12;
    public static final int GROUND_POSITION = 13;
    public static final int SHOOT_POSITION = 14;
    public static final int L2L3_POSITION = 15;
    public static final int L3L4_POSITION = 16;
}
