package com.team766.robot.mayhem_shooter.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.robot.mayhem_shooter.mechanisms.Drive;
import com.team766.robot.mayhem_shooter.mechanisms.Shooter;
import com.team766.robot.mayhem_shooter.mechanisms.Vision;

public class Autonomous extends Procedure {

    private Vision vision;
    private Shooter shooter;
    private Drive drive;

    public Autonomous(Vision vision, Shooter shooter, Drive drive) {
        this.vision = reserve(vision);
        this.shooter = reserve(shooter);
        this.drive = reserve(drive);
    }

    @Override
    public void run(Context context) {
        // Begin autonomus phase
        // Step 1: Drive forward and back into the ball
        //Step 2: Activate intake to collect ball
        // Step 3: Shoot at target using vision
    }
}
