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
import com.team766.robot.reva_2025.mechanisms.AlgaeIntake;
import com.team766.robot.reva_2025.mechanisms.CoralIntake;
import com.team766.robot.reva_2025.mechanisms.Elevator;
import com.team766.robot.reva_2025.mechanisms.Elevator.ElevatorPosition;
import com.team766.robot.reva_2025.mechanisms.Wrist;
import com.team766.robot.reva_2025.mechanisms.AlgaeIntake.Level;
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
    private AlgaeIntake algaeIntake;

    public ScoreCoral(
            RelativeReefPos side,
            ScoreHeight scoreLevel,
            SwerveDrive drive,
            Elevator elevator,
            Wrist wrist,
            CoralIntake coral,
            AlgaeIntake algaeIntake) {

        this.drive = reserve(drive);
        this.elevator = reserve(elevator);
        this.wrist = reserve(wrist);
        this.coral = reserve(coral);
        this.algaeIntake = reserve(algaeIntake);

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

        if (scoreLevel == ScoreHeight.L2) {
            algaeIntake.setArmAngle(Level.GroundIntake);
        }

        Pose2d nearestPose;

        switch (scoreLevel) {
            case L1:
                nearestPose = nearestPose(0, true);
                break;
            case L2:
                nearestPose = nearestPose(0, false); // FIXME: figure out better position
                break;
            case L3:
                nearestPose = nearestPose(0.05, false);
                break;
            case L4:
                nearestPose = nearestPose(0.25, false);
                break;
            default:
                log(Severity.ERROR, "Invalid scoreLevel");
                return;
        }

        // TODO: clean up sequence/logic
        if (scoreLevel.equals(ScoreHeight.L4)) {
            context.runSync(new AutoAlign(nearestPose, 0.10, drive));
            waitForStatusMatchingOrTimeout(
                    context, Elevator.ElevatorStatus.class, s -> s.isAtHeight(), 1);
            waitForStatusMatchingOrTimeout(
                    context, Wrist.WristStatus.class, s -> s.isAtAngle(), 0.5);
            context.runSync(new AutoAlign(nearestPose(0.04, false), drive));
            coral.out();
            context.runSync(new AutoAlign(nearestPose(-0.01, false), drive));
        } else {
            context.runSync(new AutoAlign(nearestPose, drive));
            waitForStatusMatchingOrTimeout(
                    context, Elevator.ElevatorStatus.class, s -> s.isAtHeight(), 1);
            waitForStatusMatchingOrTimeout(
                    context, Wrist.WristStatus.class, s -> s.isAtAngle(), 0.5);
            coral.out();
            context.waitForSeconds(0.5);
        }
        coral.stop();
        wrist.setAngle(WristPosition.CORAL_INTAKE);
        elevator.setPosition(ElevatorPosition.ELEVATOR_BOTTOM);
        if (scoreLevel == ScoreHeight.L2) {
            algaeIntake.setArmAngle(Level.Stow);
        }
    }
}
