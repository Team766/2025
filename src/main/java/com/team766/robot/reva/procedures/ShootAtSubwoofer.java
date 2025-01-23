package com.team766.robot.reva.procedures;

import com.team766.framework3.Context;
import com.team766.framework3.Procedure;
import com.team766.robot.reva.mechanisms.Intake;
import com.team766.robot.reva.mechanisms.Shooter;
import com.team766.robot.reva.mechanisms.Shoulder;

public class ShootAtSubwoofer extends Procedure {
    private final Shoulder shoulder;
    private final Shooter shooter;
    private final Intake intake;

    public ShootAtSubwoofer(Shoulder shoulder, Shooter shooter, Intake intake) {
        this.shoulder = reserve(shoulder);
        this.shooter = reserve(shooter);
        this.intake = reserve(intake);
    }

    public void run(Context context) {
        shoulder.rotate(Shoulder.ShoulderPosition.SHOOT_LOW);
        context.runSync(new ShootVelocityAndIntake(shooter, intake));
    }
}
