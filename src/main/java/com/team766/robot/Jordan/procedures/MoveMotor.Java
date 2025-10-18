

public class MoveMotor.Java extends Procedure{
    private MovingMotor motor;

    public MoveMotor(MovingMotor myMotor) motor = reserve(myMotor);

    public void run(Context context){
        motor.setMotorPower(1)
        context.waitForSeconds(5)
        motor.setMotorPower(0)
    }
}