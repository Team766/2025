package com.team766.robot.reva_2025.mechanisms;

import com.team766.framework3.MechanismWithStatus;
import com.team766.framework3.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;
import com.team766.robot.reva.mechanisms.MotorUtil;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class CoralIntake extends MechanismWithStatus<CoralIntake.CoralIntakeStatus> {
    private static final double POWER_IN = 0.40;
    private static final double POWER_IDLE = 0.20;
    private static final double POWER_OUT = -1.0;
    private MotorController motor;

    public static record CoralIntakeStatus(double intakePower, double current) implements Status {}

    public CoralIntake() {
        motor = RobotProvider.instance.getMotor("coralIntake.motor");
        motor.setCurrentLimit(30);
    }

    public void in() {
        motor.set(POWER_IN);
    }

    public void out() {
        motor.set(POWER_OUT);
    }

    public void idle() {
        motor.set(POWER_IDLE);
    }

    public void stop() {
        motor.set(0.0);
    }

    @Override
    protected void onMechanismIdle() {
        idle();
    }

    @Override
    protected void run() {
        SmartDashboard.putNumber("Coral Current", MotorUtil.getCurrentUsage(motor));
    }

    @Override
    protected CoralIntakeStatus updateStatus() {
        return new CoralIntakeStatus(motor.get(), MotorUtil.getCurrentUsage(motor));
    }
}
