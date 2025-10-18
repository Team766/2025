
public class Drive extends MechanismWithStatus <Drive.DriveStatus> {

    private MotorController motor1;
    private MotorController motor2;

    Public Drive () {
        motor1 = RobotProvider.instance.getMotor(configName:"motorLeft");
        motor2 = RobotProvider.instance.getMotor(configName:"motorRight");

    }

    
    public record DriveStatus (double currentPositionLeft, double currentPositionRight) implements Status {
    }

    protected DriveStatus updateStatus() {
            return DriveStatus (motor1.getSensorPosition(), motor2.getSensorPosition());
    }
    

    public void setMotorPower(double powerLeft, double powerRight) {
        motor1.set(powerLeft);
        motor2.set(powerRight);
    }

    

   



    //MotorController.getSensorPosition();
    //MotorController.setPosition();
    //MotorController.setCurrentLimit();
    //MotorController.follow();
    //MotorController.setInverted();




}