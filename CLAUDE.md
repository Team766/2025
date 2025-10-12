# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is Team 766's FRC (FIRST Robotics Competition) 2025 robot code, written in Java using WPILib. The codebase uses a custom framework built around a Mechanism/Procedure architecture with resource reservation, plus an integrated physics simulator for testing without hardware.

## Build and Development Commands

### Building and Testing
- `./gradlew build` - Compile code and run all tests
- `./gradlew test` - Run tests only
- `./gradlew spotlessApply` - Auto-format code (Java, Gradle, XML) using Spotless
- `./gradlew spotlessCheck` - Check code formatting without applying changes

### Deployment
- `./gradlew deploy` - Deploy to the RoboRIO (robot must be connected)
- Config files are deployed from `src/main/deploy/configs/` to `/home/lvuser/deploy/configs` on the robot

### Simulation
- Run the "Simulate Robot Code" task in VS Code to start robot code in simulation mode
- `./deploy_sim.py` - Start the external physics simulator (waits for robot code on port 5800, then launches in a `screen` session)
- Simulation mode is configured via `simConfig.txt` (JSON format)
- The simulator provides a web-based 3D visualization of robot physics

## Architecture

### Framework Layer (`com.team766.framework`)
The custom framework provides the core abstractions:

**Mechanism**: Base class for all robot subsystems. Mechanisms must be reserved before use via Procedures. Each Mechanism has a `run()` method called periodically and an `onMechanismIdle()` method called when no Procedure is using it. Mechanisms integrate with WPILib2's Command system via internal proxy subsystems.

**Procedure**: Asynchronous tasks that can reserve Mechanisms. Procedures receive a `Context` object that provides cooperative-multitasking primitives like `waitFor()`, `waitForSeconds()`, etc. Procedures run as WPILib2 Commands under the hood.

**Context**: Execution context for Procedures providing wait/yield operations. Procedures can be interrupted if another Procedure with overlapping Mechanism reservations is started.

**Rule/RuleEngine**: Event-driven behavior system. Rules trigger Procedures based on conditions (e.g., button presses in operator interface). Used for teleoperated control.

### Hardware Abstraction Layer (`com.team766.hal`)
Abstraction layer over WPILib hardware APIs:
- Multiple implementations: `wpilib` (real robot), `simulator` (integrated sim), `mock` (unit tests)
- `RobotProvider` provides factory methods for motors, sensors, etc.
- `MotorController` interface for motor control with position/velocity PID, current limiting, and sensor scaling
- Configuration loaded from JSON files at runtime (see Configuration section)

### Robot Implementations (`com.team766.robot`)
Multiple robot configurations in separate packages:
- `reva` - 2024 robot with swerve drive, shoulder, intake, shooter, climber
- `reva_2025` - Active development for 2025 season
- `common` - Shared code including swerve drive implementation
- `example`, `gatorade`, etc. - Other robot configurations

Each robot package contains:
- `Robot.java` - Implements `RobotConfigurator` to initialize mechanisms, operator interface, and autonomous modes
- `mechanisms/` - Mechanism implementations
- `procedures/` - Procedure implementations for autonomous and complex actions
- `OI.java` - RuleEngine implementation that binds controls to Procedures

### Swerve Drive
Swerve drive is in `com.team766.robot.common.mechanisms.SwerveDrive`:
- Field-oriented control with gyro-based rotation compensation
- Independent control of translation and rotation
- Cross-wheels mode for resisting movement
- Odometry integration for position tracking
- Configuration via `SwerveConfig` class (wheel locations, CAN bus, current limits)
- Controlled via `controlFieldOriented(x, y, rotation)` or `drive(chassisSpeeds)`

See `docs/SwerveDrive.md` for detailed implementation notes and bringup instructions.

### Operator Interface
Controls are organized by role:
- **Driver** (`DriverOI`) - Two Thrustmaster T.16000M joysticks for robot movement
- **Box Operator** (`BoxOpOI`) - Xbox controller for mechanism control
- **Debug** (`DebugOI`) - Megalodon macro pad for pit testing individual mechanisms

See `docs/OperatorInterface.md` for complete control mappings.

## Configuration System

Robot hardware configuration is stored in JSON files:
- Main config: `simConfig.txt` (JSON despite extension) at project root
- Additional configs can be in `src/main/deploy/configs/`
- Config is read via `com.team766.config.ConfigFileReader`
- Access config values using `ConfigValue<T>` types in code
- Supports motor controllers, sensors, PID constants, physical parameters
- Specify which robot configuration to load via `"robotConfigurator"` field pointing to a class implementing `RobotConfigurator`

## Logging and Telemetry

Uses AdvantageKit for logging:
- `@AutoLog` annotation for automatic logging of mechanism state
- Logs written to `logs/` directory
- Web dashboard on port 5800 during simulation
- Replay logs using `./gradlew replayWatch`

## Testing

- JUnit 5 for unit tests
- ErrorProne static analysis (custom check: `DontDiscardProcedures` ensures Procedures are scheduled)
- AspectJ used to enforce that Procedures are not discarded without being scheduled
- Mock HAL implementation for hardware-independent testing

## Common Development Patterns

### Creating a New Mechanism
1. Extend `Mechanism` (or `MechanismWithStatus` for status publishing)
2. Add config entries for hardware in config file
3. Initialize hardware in constructor using `RobotProvider.instance.getXXX(configValue)`
4. Implement `run()` for periodic updates
5. Implement `onMechanismIdle()` to stop/safe state when not reserved
6. Add to `Robot.initializeMechanisms()`

### Creating a New Procedure
1. Extend `Procedure` or `InstantProcedure`
2. Call `reserve(mechanism)` in constructor for any Mechanisms you'll use
3. Implement `run(Context context)` with your logic
4. Use `context.waitFor()`, `context.waitForSeconds()`, etc. for timing
5. Trigger from OI using `Rule.when(condition).then(procedureName, mechanism1, mechanism2, ...)`

### Creating a New Autonomous Mode
1. Create a Procedure for the autonomous routine in `procedures/auton_routines/`
2. Add to `Robot.getAutonomousModes()` array
3. Use WPILib `PathPlanner` for path following with swerve drive

## Simulation Mode

The codebase includes an integrated physics simulator:
- Set `"simulationMode": "VrConnector"` in config to enable
- Simulator runs separately and connects via network sockets
- Simulates motors, sensors, pneumatics, and robot physics
- Web UI shows 3D robot visualization and controls
- Implementation in `com.team766.simulator` package

## Project Structure Notes

- Generated code (protobuf, build constants) in `build/generated/`
- Vendor dependencies in `vendordeps/` (AdvantageKit, CTRE, REV, etc.)
- Uses Gradle with GradleRIO plugin for FRC-specific build tasks
- DevContainer support for consistent development environment
- CI via GitHub Actions: builds and tests on every push/PR
