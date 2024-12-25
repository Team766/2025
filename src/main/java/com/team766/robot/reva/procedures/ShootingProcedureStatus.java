package com.team766.robot.reva.procedures;

import com.team766.framework3.Status;
import com.team766.framework3.StatusHandle;

public record ShootingProcedureStatus(ShootingProcedureStatus.Status status) implements Status, StatusHandle<ShootingProcedureStatus> {
    public enum Status {
        RUNNING,
        OUT_OF_RANGE,
        FINISHED
    }
}
