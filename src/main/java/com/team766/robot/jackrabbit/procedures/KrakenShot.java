package com.team766.robot.jackrabbit.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.jackrabbit.mechanisms.Hood;
import com.team766.robot.jackrabbit.mechanisms.Shooter;

public class KrakenShot extends Procedure {
    private final Hood hood;
    private final Shooter shooter;

    public KrakenShot(Hood hood, Shooter shooter) {
        this.hood = reserve(hood);
        this.shooter = reserve(shooter);
    }

    @Override
    public void run(Context context) {
        hood.setTargetAngle(40);
        shooter.shoot(40);

        // TODO: Check for 0 velocity
        waitForStatusMatching(context, Hood.HoodStatus.class, s -> s.isAtAngle(40));
        waitForStatusMatching(context, Shooter.ShooterStatus.class, s -> s.isAtSpeed(40));
    }
}
