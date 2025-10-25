package com.team766.robot.reva;

import com.team766.framework.Context;
import com.team766.framework.RuleEngine;
import com.team766.framework.RuleGroup;
import com.team766.robot.common.mechanisms.LEDString;
import com.team766.robot.reva.mechanisms.ForwardApriltagCamera;
import com.team766.robot.reva.mechanisms.Orin;
import com.team766.robot.reva.procedures.IntakeUntilIn;
import com.team766.robot.reva.procedures.ShootingProcedureStatus;
import edu.wpi.first.wpilibj.DriverStation;
import java.util.function.BooleanSupplier; 
import JoystickReader;
  
public class CANdle extends RuleEngine {  

    private final LEDString ledString = new LEDString("leds");
    private final LEDString.Segment segment = ledString.makeSegment(0, 512);

    JoystickReader button1 = RobotProvider.instance.getJoystick(INDEX)
    JoystickReader button2 = RobotProvider.instance.getJoystick(INDEX)
    JoystickReader button3 = RobotProvider.instance.getJoystick(INDEX)

    addRule ("Cube",
        joystickLeft.whenButton(button1),
        ONCE, 
        () -> {
            segment.setLEDs(128, 255, 0)
        });
        
    addRule ("Cone",
        joystickLeft.whenButton(button2),
        ONCE,
        () -> {
            segment.setLEDs(0, 0, 225)
        });
       
    addRule ("Defense",
        joystickLeft.whenButton(button3),
        ONCE,
        segment,
        () -> {
            Animation rainbowAnim = new RainbowAnimation();
            segment.animate(rainbowAnim);
        });
}
       

 
  
