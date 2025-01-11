package com.team766.robot.reva_2025.mechanisms;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;
public class AlgaeIntake {
    private MotorController intakemotor;
    private MotorController armmotor;
    private State state;
    private Level level;

    public AlgaeIntake() {
        intakemotor = RobotProvider.instance.getMotor("AlgaeIntake.RollerMotor");
        armmotor = RobotProvider.instance.getMotor("AlgaeArm.RollerMotor");
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
        private final double height;
        Level(double level) {
            this.height = level;
        }

        private double getAngle() {
            return height;
        }
    }

    public void setArmAngle(Level level) {
        armmotor.set(MotorController.ControlMode.Position,level.getAngle());
        this.level=level;
    }

    public void out(){
        if (level == Level.GroundIntake || level==Level.L2L3AlgaeIntake){
            intakemotor.set(-1);
        }
        if (level == Level.L3L4AlgaeIntake){
            intakemotor.set(1);
        }
        state=State.Out;

    }
    public void in (){
        if (level == Level.GroundIntake || level==Level.L2L3AlgaeIntake){
            intakemotor.set(1);
        }
        if (level == Level.L3L4AlgaeIntake){
            intakemotor.set(-1);
        }
        state=State.In;

    }

    public void stop(){
        intakemotor.set(0);
        state=State.Out;
    }
    }


