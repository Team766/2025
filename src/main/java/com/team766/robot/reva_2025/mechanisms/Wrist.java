import com.ctre.phoenix.motorcontrol.ControlMode;
import com.team766.framework.Mechanism;
import com.team766.hal.MotorController;
import com.team766.hal.RobotProvider;
import com.team766.hal.wpilib.CANSparkMaxMotorController;

public class Wrist extends Mechanism{

    public enum Position {

        // TODO: update all of these to real things!
        PICKUP_CORAL(0),
        PLACE_CORAL(180);

        private final double angle;

        private Position(double angle) {
            this.angle = angle;
        }

        public double getAngle(){
            return angle;
        }
    }

    private MotorController wristMotor;
    public Wrist (){
        wristMotor = RobotProvider.instance.getMotor("Wrist.Motor");
    }

    public void setAngle(Position position){
        setAngle(position.getAngle());
    }

    public void setAngle(double angle){
        checkContextOwnership();
        wristMotor.set(MotorController.ControlMode.Position, angle);
    }
}
