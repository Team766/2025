package com.team766.robot.reva_2025.procedures;

import com.team766.framework3.AdvancedUtils;
import com.team766.framework3.Context;
import com.team766.framework3.LaunchedContext;
import com.team766.framework3.Procedure;
import com.team766.robot.common.mechanisms.SwerveDrive;
import com.team766.robot.reva_2025.constants.CoralConstants.CoralConstant;
import com.team766.robot.reva_2025.mechanisms.CoralIntake;
import com.team766.robot.reva_2025.mechanisms.Elevator;
import com.team766.robot.reva_2025.mechanisms.Wrist;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;

public class ScoreCoral extends Procedure {

    private CoralConstant position;
    private double levelHeight;
    private double angle;
    private SwerveDrive drive;
    private Elevator elevator;
    private Wrist wrist;
    private CoralIntake coral;

    public ScoreCoral(
            CoralConstant position,
            double levelHeight,
            double angle,
            SwerveDrive drive,
            Elevator elevator,
            Wrist wrist,
            CoralIntake coral) {
        this.position = position;
        this.levelHeight = levelHeight;
        this.angle = angle;

        this.drive = reserve(drive);
        this.elevator = reserve(elevator);
        this.wrist = reserve(wrist);
        this.coral = reserve(coral);
    }

    public void run(Context context) {
        context.yield();

        LaunchedContext launchedAutoAlign =
                AdvancedUtils.startAsync(
                        context,
                        new AutoAlign(
                                new Pose2d(
                                        position.getX(),
                                        position.getZ(),
                                        new Rotation2d(position.getAngle())),
                                drive));

        elevator.setPosition(levelHeight);
        wrist.setAngle(angle);

        while (!elevator.isAtPosition()
                || !wrist.isAtPosition()
                || !launchedAutoAlign.isFinished()) {
            context.yield();
        }

        context.runSync(new RunCoralOut(coral, 0.5));
    }
}
