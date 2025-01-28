package com.team766.robot.reva_2025.procedures;

import com.team766.framework3.Context;
import com.team766.framework3.Procedure;
import com.team766.robot.common.mechanisms.SwerveDrive;
import com.team766.robot.reva_2025.constants.AlgaeConstants;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.wpilibj.DriverStation;

public class GoToKnownPositionAndShootAlgae extends Procedure {
    private SwerveDrive drive;
    public GoToKnownPositionAndShootAlgae(SwerveDrive drive, ){
        this.drive = reserve(drive);
    }

    @Override
    public void run(Context context) {
        Pose2d setpoint;
        if(DriverStation.getAlliance().isPresent()){
            if(DriverStation.getAlliance().equals(DriverStation.Alliance.Red)){
                setpoint = AlgaeConstants.RED_ALGAE_POSE;
            }

            setpoint = AlgaeConstants.BLUE_ALGAE_POSE;
        } else {
            return;
        }


        context.runSync(new AutoAlign(setpoint, drive));
    }
    
}
