Package for robot-specific code.

Each robot package will contain the following:
* `Robot.java` - contains static references to that robot's mechanisms.
* `OI.java` - set of rules for reading joystick input, controlling the robot via the Operator Interface.
* `Lights.java` - set of rules for controlling LEDs on the robot.
* `mechanisms` - sub-package containing mechanisms for this robot.
* `procedures` - sub-package containing procedures for this robot.
