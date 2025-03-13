package com.team766.robot.reva_2025.procedures;

import com.team766.framework3.Context;
import com.team766.framework3.Procedure;
import com.team766.framework3.StatusBus;
import com.team766.logging.Severity;
import com.team766.robot.common.mechanisms.SwerveDrive;
import com.team766.robot.common.mechanisms.SwerveDrive.DriveStatus;
import com.team766.robot.reva_2025.constants.CoralConstants.ReefPos;
import com.team766.robot.reva_2025.constants.CoralConstants.RelativeReefPos;
import com.team766.robot.reva_2025.constants.CoralConstants.ScoreHeight;
import com.team766.robot.reva_2025.mechanisms.CoralIntake;
import com.team766.robot.reva_2025.mechanisms.Elevator;
import com.team766.robot.reva_2025.mechanisms.Wrist;
import com.team766.robot.reva_2025.mechanisms.Elevator.ElevatorPosition;
import com.team766.robot.reva_2025.mechanisms.Wrist.WristPosition;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ScoreCoral extends Procedure {

    private RelativeReefPos side;
    private ScoreHeight scoreLevel;
    private SwerveDrive drive;
    private Elevator elevator;
    private Wrist wrist;
    private CoralIntake coral;

    public ScoreCoral(
            RelativeReefPos side,
            ScoreHeight scoreLevel,
            SwerveDrive drive,
            Elevator elevator,
            Wrist wrist,
            CoralIntake coral) {

        this.drive = reserve(drive);
        this.elevator = reserve(elevator);
        this.wrist = reserve(wrist);
        this.coral = reserve(coral);

        this.side = side;
        this.scoreLevel = scoreLevel;
    }

    private Pose2d nearestPose(double dist, boolean rotated) {
        final Optional<DriveStatus> driveStatus =
                StatusBus.getInstance().getStatus(SwerveDrive.DriveStatus.class);
        final Optional<Alliance> alliance = DriverStation.getAlliance();
        if (driveStatus.isEmpty()) {
            log(Severity.ERROR, "Cannot find drive status");
            return new Pose2d();
        } else if (alliance.isEmpty()) {
            log(Severity.ERROR, "Cannot find alliance");
            return driveStatus.get().currentPosition();
        }
        List<Pose2d> points = new ArrayList<>();
        for (ReefPos reefPos : ReefPos.values()) {
            if (reefPos.getRelativeReefPos(alliance.get()).equals(side)) {
                Pose2d pose = reefPos.getPosition(alliance.get(), dist);
                if (rotated) {
                    pose =
                            new Pose2d(
                                    pose.getTranslation(),
                                    pose.getRotation().plus(Rotation2d.k180deg));
                }
                points.add(pose);
            }
        }
        Pose2d curPose = driveStatus.get().currentPosition();
        if (points.size() == 0) {
            log(Severity.ERROR, "Cannot find nearest point");
            return curPose;
        }
        Pose2d target = curPose.nearest(points);
        log("Target scoring position: " + target);
        return target;
    }

    public void run(Context context) {
        elevator.setPosition(scoreLevel.getElevatorPosition());
        wrist.setAngle(scoreLevel.getWristPosition());

        Pose2d nearestPose;

        switch (scoreLevel) {
            case L1:
                nearestPose = nearestPose(0, true);
                break;
            case L2:
                nearestPose = nearestPose(0, false);
                break;
            case L3:
                nearestPose = nearestPose(0.0, false);
                break;
            case L4:
                nearestPose = nearestPose(0.17, false);
                break;
            default:
                log(Severity.ERROR, "Invalid scoreLevel");
                return;
        }

        context.runSync(new AutoAlign(nearestPose, drive));
        waitForStatusMatchingOrTimeout(
                context, Elevator.ElevatorStatus.class, s -> s.isAtHeight(), 1);
        waitForStatusMatchingOrTimeout(context, Wrist.WristStatus.class, s -> s.isAtAngle(), 0.5);

        if (scoreLevel.equals(ScoreHeight.L4)) {
            wrist.nudge(1);
            coral.out();
            wrist.nudge(1);
            context.runParallel(new AutoAlign(nearestPose(0.13, false), drive));
            context.waitForSeconds(0.25);
        } else {
            coral.out();
            context.waitForSeconds(0.25);
        }
        coral.stop();
        wrist.setAngle(WristPosition.CORAL_INTAKE);
        elevator.setPosition(ElevatorPosition.ELEVATOR_BOTTOM);
    }
}
