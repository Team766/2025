package com.team766.robot.ArthurDoering.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.ArthurDoering.mechanisms.Drive;

public class TestAuton extends Procedure{
    private Drive drive;

    public TestAuton(Drive drive){
        this.drive = reserve(drive);
    }

    public void run(Context context){
        context.runSync(new DriveProcedure(drive, 3));
    }
}
