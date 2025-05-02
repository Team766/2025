package com.team766.hal;

import java.util.function.BooleanSupplier;

public interface JoystickReader {
    /**
     * Get the value of the axis.
     *
     * If a deadzone has been set for this axis, the returned value will be 0 if the value would be
     * smaller than the size of the deadzone.
     *
     * @param axis The axis to read, starting at 0.
     * @return The value of the axis.
     */
    double getAxis(int axis);

    /**
     * Get whether the axis has an absolute value greater than or equal to the deadzone.
     *
     * @param axis The axis to read, starting at 0.
     * @return True if the axis value is larger than or equal to the deadzone, else false.
     */
    boolean isAxisMoved(int axis);

    /**
     * Returns a condition that can be used when defining a Rule. The condition will trigger
     * whenever the given axis moves out of its deadzone.
     * @see #isAxisMoved(int)
     */
    default BooleanSupplier whenAxisMoved(int axis) {
        return () -> isAxisMoved(axis);
    }

    /**
     * Returns a condition that can be used when defining a Rule. The condition will trigger
     * whenever at least one of the given axes have moved out of their respective deadzones.
     * @see #isAxisMoved(int)
     */
    default BooleanSupplier whenAnyAxisMoved(int... axes) {
        return () -> {
            for (int axis : axes) {
                if (isAxisMoved(axis)) {
                    return true;
                }
            }
            return false;
        };
    }

    /**
     * Set the size of the deadzone for the given axis.
     *
     * @param axis The axis to read, starting at 0.
     * @param deadzone The size of the deadzone. 0 disables the deadzone.
     */
    void setAxisDeadzone(int axis, double deadzone);

    /**
     * Set the size of the deadzone for all axes (overriding any previous calls to setAxisDeadzone).
     *
     * Deadzones for individual axes can be overridden by calling setAxisDeadzone.
     *
     * @param deadzone The size of the deadzone. 0 disables the deadzone.
     */
    void setAllAxisDeadzone(double deadzone);

    /**
     * Get the button value (starting at button 1)
     *
     * The appropriate button is returned as a boolean value.
     *
     * @param button The button number to be read (starting at 1).
     * @return The state of the button.
     */
    boolean getButton(int button);

    /**
     * Returns a condition that can be used when defining a Rule. The condition will trigger
     * whenever the given button is being pressed.
     * @see #getButton(int)
     */
    default BooleanSupplier whenButton(int button) {
        return () -> getButton(button);
    }

    /**
     * Returns a condition that can be used when defining a Rule. The condition will trigger
     * whenever at least one of the given buttons are being pressed.
     * @see #getButton(int)
     */
    default BooleanSupplier whenAnyButton(int... buttons) {
        return () -> {
            for (int button : buttons) {
                if (getButton(button)) {
                    return true;
                }
            }
            return false;
        };
    }

    /** Returns a condition that can be used when defining a Rule.  Ther condition will trigger
     * whenever the joystick's POV has the given value.
     */
    default BooleanSupplier whenPOV(int pov) {
        return () -> getPOV() == pov;
    }

    /**
     * Returns a condition that can be used when defining a Rule. The condition will trigger
     * whenever all of the given buttons are being pressed.
     * @see #getButton(int)
     */
    default BooleanSupplier whenAllButtons(int... buttons) {
        return () -> {
            for (int button : buttons) {
                if (!getButton(button)) {
                    return false;
                }
            }
            return true;
        };
    }

    /**
     * Whether the button was pressed since the last check. Button indexes begin at 1.
     *
     * @param button The button index, beginning at 1.
     * @return Whether the button was pressed since the last check.
     */
    boolean getButtonPressed(int button);

    /**
     * Whether the button was released since the last check. Button indexes begin at 1.
     *
     * @param button The button index, beginning at 1.
     * @return Whether the button was released since the last check.
     */
    boolean getButtonReleased(int button);

    /**
     * Get the value of the POV
     *
     * @return the value of the POV
     */
    int getPOV();
}
