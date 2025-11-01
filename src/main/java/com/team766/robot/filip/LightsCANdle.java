package com.team766.robot.filip;

import com.ctre.phoenix.led.Animation;
import com.ctre.phoenix.led.FireAnimation;
import com.ctre.phoenix.led.RainbowAnimation;
import com.team766.framework.Conditions;
import com.team766.framework.Conditions.LogicalAnd;
import com.team766.framework.RuleEngine;
import com.team766.framework.Status;
import com.team766.logging.Category;
import com.team766.robot.common.constants.ColorConstants;
import com.team766.robot.common.mechanisms.LEDString;
import com.team766.robot.common.mechanisms.SwerveDrive;
import edu.wpi.first.wpilibj.DriverStation;
import com.ctre.phoenix.led.CANdle;
import com.ctre.phoenix.led.CANdle.LEDStripType;
import edu.wpi.first.wpilib.TimedRobot;
import static com.team766.framework.RulePersistence;
import com.team766.common.constants.InputConstants;
import com.tean766.framework.JoystickReader;;


public class Robot extends RuleEngine {


    JoystickReader button1 = RobotProvider.instance.getButton(1);
    JoystickReader button2 = RobotProvider.instance.getButton(2);
    JoystickReader button3 = RobotProvider.instance.getButton(3);
    
    public void RobotInit() {
        CANdle _candle = new CANdle(1, "light bus");
        Joystick _joystick = new Joystick(0);
        _candle.configLEDType(LEDStripType.GRB);
    }
    addRule("Yellow for Cone"
        button1.whenButton();
        ONCE_AND_HOLD,
        _candle,
    )
    addRule("Purple for Cube"
        button2.whenButton();
        ONCE_AND_HOLD,
        _candle,
        () -> {ledString.setLEDs(128, 0, 128, 0, 0, 8);}
    )
    addRule("Rainbow for Defense"
        button3.whenButton(),
        ONCE_AND_HOLD,
        anim,
        () -> {RainbowAnimation anim = new RainbowAnimation();
        _candle.animate(anim);}
    )
}