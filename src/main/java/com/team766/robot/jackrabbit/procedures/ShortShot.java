package com.team766.robot.jackrabbit.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.jackrabbit.mechanisms.Hood;
import com.team766.robot.jackrabbit.mechanisms.Shooter;

public class ShortShot extends Procedure {
    private final Hood hood;
    private final Shooter shooter;

    public ShortShot(Hood hood, Shooter shooter) {
        this.hood = reserve(hood);
        this.shooter = reserve(shooter);
    }

    @Override
    public void run(Context context) {
        hood.setTargetAngle(60);
        shooter.shoot(20);

        // TODO: Check for 0 velocity
        waitForStatusMatching(context, Hood.HoodStatus.class, s -> s.isAtAngle(50));
        waitForStatusMatching(context, Shooter.ShooterStatus.class, s -> s.isAtSpeed(20));
    }
}
