package com.team766.robot.reva_2025.procedures.autons;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.common.mechanisms.SwerveDrive;
import com.team766.robot.common.procedures.PathSequenceAuto;

import edu.wpi.first.math.geometry.Pose2d;

public class L1Center extends PathSequenceAuto {
    public L1Center(SwerveDrive drive, Pose2d pose){
        super(drive, pose);
        drive.controlFieldOriented(1, 0, 0);
    }
    
}
