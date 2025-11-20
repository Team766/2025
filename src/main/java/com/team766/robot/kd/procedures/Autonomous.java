package com.team766.robot.kd.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.kd.mechanisms.Drive;

public class Autonomous extends Procedure {
    private Drive drive;

    public Autonomous(Drive my_drive) {
        drive = reserve(my_drive);
    }

    public void run(Context context) {}
}
