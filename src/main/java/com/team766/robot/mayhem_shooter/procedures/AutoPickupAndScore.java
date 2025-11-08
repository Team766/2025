package com.team766.robot.mayhem_shooter.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.mayhem_shooter.mechanisms.Drive;
import com.team766.robot.mayhem_shooter.mechanisms.Shooter;
import com.team766.robot.mayhem_shooter.mechanisms.Vision;

public class AutoPickupAndScore extends Procedure {

    private Vision vision;
    private Shooter shooter;
    private Drive drive;

    public AutoPickupAndScore(Vision vision, Shooter shooter, Drive drive) {
        this.vision = reserve(vision);
        this.shooter = reserve(shooter);
        this.drive = reserve(drive);
    }

    @Override
    public void run(Context context) {}
}
