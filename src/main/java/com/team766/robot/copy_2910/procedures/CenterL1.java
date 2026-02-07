package com.team766.robot.copy_2910.procedures;

import com.team766.robot.common.mechanisms.SwerveDrive;
import com.team766.robot.common.procedures.PathSequenceAuto;
import com.team766.robot.copy_2910.mechanisms.Elevator;
import com.team766.robot.copy_2910.mechanisms.Elevator.ElevatorPosition;
import com.team766.robot.copy_2910.mechanisms.Intake;
import com.team766.robot.copy_2910.mechanisms.Shoulder;
import com.team766.robot.copy_2910.mechanisms.Shoulder.ShoulderPosition;
import com.team766.robot.copy_2910.mechanisms.Wrist;
import com.team766.robot.copy_2910.mechanisms.Wrist.WristPosition;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;

public class CenterL1 extends PathSequenceAuto {
    public CenterL1(
            SwerveDrive drive, Intake intake, Wrist wrist, Elevator elevator, Shoulder shoulder) {
        super(drive, new Pose2d(7.160, 3.970, Rotation2d.fromDegrees(-180)));
        addProcedure(
                new MoveWristvator(
                        shoulder,
                        elevator,
                        wrist,
                        ShoulderPosition.L1,
                        ElevatorPosition.L1,
                        WristPosition.L1));
        addPath("Center Start - L1 Score");
        addProcedure(new OuttakeCoral(intake));
    }
}
