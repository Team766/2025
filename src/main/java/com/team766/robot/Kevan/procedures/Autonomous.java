package com.team766.robot.Kevan.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.Kevan.mechanisms.Drive;

public class Autonomous extends Procedure {
    private Drive drive;
    
    public Autonomous(Drive myDrive) {
        drive = reserve(myDrive);
    }
    
    public void AutonProc(Context context) {
        motor.move(1);
        context.waitForSeconds(5);
        motor.move(0);
    }
    
}