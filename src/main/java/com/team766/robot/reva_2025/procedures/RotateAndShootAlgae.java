package com.team766.robot.reva_2025.procedures;

import com.team766.framework3.Context;
import com.team766.framework3.Procedure;
import com.team766.robot.common.mechanisms.SwerveDrive;
import edu.wpi.first.math.geometry.Pose2d;

public class RotateAndShootAlgae extends Procedure{
    private SwerveDrive drive;
    public RotateAndShootAlgae(SwerveDrive drive){
        this.drive = reserve(drive);
    }
    @Override
    public void run(Context context) {
        Pose2d currentPosition = getStatusOrThrow(SwerveDrive.DriveStatus.class).currentPosition();

        context.runParallel(new AutoAlign(, null));
    }
    
}
