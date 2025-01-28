package com.team766.robot.reva_2025.mechanisms;
import com.team766.framework3.MechanismWithStatus;
import com.team766.framework3.Status;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;

public class AlgaeIntake extends MechanismWithStatus<AlgaeIntake.AlgaeIntakeStatus> {
    private MotorController intakeMotor;
    private MotorController armMotor;
    private MotorController shooterMotor;
    private State state;
    private Level level;

    public record AlgaeIntakeStatus(State state, Level level) implements Status {}

    public AlgaeIntake() {
        intakeMotor = RobotProvider.instance.getMotor("AlgaeIntake.RollerMotor");
        armMotor = RobotProvider.instance.getMotor("AlgaeArm.RollerMotor");
        shooterMotor = RobotProvider.instance.getMotor("AlgaeShooter.RollerMotor");

        level=Level.Stow;

    }
    public enum State{
        In,
        Out,
        Stop,
        Shoot;

    }
    public enum Level{
        GroundIntake(20), 
        L2L3AlgaeIntake(90),
        L3L4AlgaeIntake(180),
        Stow(0);
        private final double angle;
        Level(double level) {
            this.angle = level;
        }

        private double getAngle() {
            return angle;
        }
    }

    public void setArmAngle(Level level) {
        armMotor.set(MotorController.ControlMode.Position,level.getAngle());
        this.level=level;
    }

    public void out(){
        if (level == Level.GroundIntake || level==Level.L2L3AlgaeIntake){
            intakeMotor.set(-1);
        }
        else if (level == Level.L3L4AlgaeIntake){
            intakeMotor.set(1);
        }
        state=State.Out;

    }
    public void in (){
        if (level == Level.GroundIntake || level==Level.L2L3AlgaeIntake){
            intakeMotor.set(1);
        }
        else if (level == Level.L3L4AlgaeIntake){
            intakeMotor.set(-1);
        }
        state=State.In;

    }

    public void stop(){
        intakeMotor.set(0);
        state=State.Stop;
    }

    public void shooterOn(){
       shooterMotor.set(1);
    }

    protected AlgaeIntakeStatus updateStatus() {
        return new AlgaeIntakeStatus(state, level);
    }
  }


