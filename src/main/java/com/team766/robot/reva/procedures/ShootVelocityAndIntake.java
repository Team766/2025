package com.team766.robot.reva.procedures;

import com.team766.framework3.Context;
import com.team766.framework3.Procedure;
import com.team766.robot.reva.mechanisms.Intake;
import com.team766.robot.reva.mechanisms.Shooter;
import com.team766.robot.reva.procedures.ShootingProcedureStatus.Status;

public class ShootVelocityAndIntake extends Procedure {

    private final double speed;

    private final Shooter shooter;
    private final Intake intake;

    public ShootVelocityAndIntake(Shooter shooter, Intake intake) {
        this(4800, shooter, intake);
    }

    public ShootVelocityAndIntake(double speed, Shooter shooter, Intake intake) {
        this.speed = speed;
        this.shooter = reserve(shooter);
        this.intake = reserve(intake);
    }

    public void run(Context context) {
        shooter.shoot(speed);
        context.waitForConditionOrTimeout(() -> shooter.getStatus().isCloseToSpeed(speed), 1.5);

        intake.in();

        // FIXME: change this value back to 1.5s if doesn't intake for long enough
        context.waitForSeconds(1.0);

        intake.stop();
        publishStatus(new ShootingProcedureStatus(Status.FINISHED));

        // Shooter stopped at the end of auton
    }
}
