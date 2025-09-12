package com.team766.hal.wpilib;

import com.ctre.phoenix6.StatusCode;
import com.team766.hal.MotorControllerCommandFailedException;
import com.team766.logging.LoggerExceptionUtils;

public class CTREPhoenix6Utils {

    public enum ExceptionTarget {
        THROW,
        LOG,
    }

    public static void statusCodeToException(final ExceptionTarget throwEx, final StatusCode code) {
        if (code.isOK()) {
            return;
        }
        var ex = new MotorControllerCommandFailedException(code.toString());
        switch (throwEx) {
            case THROW:
                throw ex;
            default:
            case LOG:
                LoggerExceptionUtils.logException(ex);
                break;
        }
    }
}
