package com.team766.robot.reva_2025.procedures;

import com.team766.framework3.Context;
import com.team766.framework3.Procedure;
import com.team766.robot.common.mechanisms.SwerveDrive;
import com.team766.robot.reva_2025.mechanisms.AlgaeIntake;
import com.team766.robot.reva_2025.mechanisms.AlgaeIntake.Level;
import com.team766.robot.reva_2025.mechanisms.AlgaeIntake.State;
import edu.wpi.first.math.geometry.Pose2d;

public class AutoShoot extends Procedure {
    private AlgaeIntake algaeIntake;
    private SwerveDrive drive;
    private Pose2d pose2D;

    public AutoShoot(Pose2d pose2D, AlgaeIntake algaeIntake, SwerveDrive drive) {
        this.algaeIntake = reserve(algaeIntake);
        this.drive = reserve(drive);
        this.pose2D = pose2D;
    }

    @Override
    public void run(Context context) {
        algaeIntake.setArmAngle(Level.Shoot);
        context.runSync(new AutoAlign(pose2D, drive));
        // use auto allign to move to a position
        // move shoot arm to correct position
        waitForStatusMatching(context, AlgaeIntake.AlgaeIntakeStatus.class, s -> s.isAtAngle());
        // shoot
        algaeIntake.setState(State.Shoot);
        context.waitForSeconds(1);
        algaeIntake.setState(State.Idle);
    }
}
