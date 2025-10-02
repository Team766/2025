public class MovingMotor extends MechanismWithStatus<MovingMotor.MovingMotorStatus>
{
    MotorController motor = RobotProvider.instance.getMotor ("Left Motor");
    public MovingMotor() {
    }
    public void MoveMotor(double currentPosition) implements status {
        motor.set(motorPower);
    }
    public record MovingMotorStatus(double currentPosition) implements Status 
    {
        protected MovingMotorStatus updateStatus() 
        {
            return new MovingMotorStatus(motor.getPosition());
        }
    }
}