package com.team766.robot.reva.procedures;

import com.team766.robot.common.mechanisms.SwerveDrive;
import com.team766.robot.common.procedures.PathSequenceAuto;
import com.team766.robot.reva.mechanisms.Intake;
import com.team766.robot.reva.mechanisms.Shooter;
import com.team766.robot.reva.mechanisms.Shoulder;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;

public class MidfieldAutonSourceSide extends PathSequenceAuto {
    public MidfieldAutonSourceSide(
            SwerveDrive drive, Shoulder shoulder, Shooter shooter, Intake intake) {
        super(drive, new Pose2d(0.71, 4.39, Rotation2d.fromDegrees(-60)));
        addProcedure(new ShootNow(drive, shoulder, shooter, intake));
        addProcedure(new StartAutoIntake(shoulder, intake));
        addPath("MidfieldSource 1");
        addPath("MidfieldSource 2");
        addProcedure(new ShootNow(drive, shoulder, shooter, intake));
    }
}
