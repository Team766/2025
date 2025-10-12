# AprilTag Simulation

This document explains how to simulate AprilTag detections during robot simulation, mimicking the behavior of the Orin coprocessor that normally provides vision data.

## Overview

In the real robot:
- **Orin coprocessor** runs vision processing
- Detects AprilTags using cameras
- Publishes detections to **NetworkTables** as double arrays
- **RoboRIO** reads these via `GetOrinRawValue` class
- Data format: `[timestamp, tagId, x, y, z, timestamp, tagId, x, y, z, ...]`

In simulation:
- **AprilTagSimulator** replaces the Orin
- Publishes fake AprilTag detections based on simulated robot position
- Uses the same NetworkTables interface
- Robot code works identically - it just reads from NetworkTables

## Architecture

```
┌──────────────────────────────────────────────────────────────┐
│  Robot Code (simulateJava)                                   │
│                                                               │
│  ┌─────────────────┐          ┌──────────────────────┐      │
│  │ RobotMain       │          │ Orin Mechanism       │      │
│  │ simulationInit()│          │                      │      │
│  │                 │          │ GetOrinRawValue      │      │
│  │ Creates         │          │ .getRawPoseData()    │      │
│  │ AprilTagSim ────┼──────┐   │ reads from NT        │      │
│  └─────────────────┘      │   └──────────────────────┘      │
│                           │                                  │
│  ┌─────────────────┐      │                                  │
│  │ simulationPeriodic() │ │                                  │
│  │ calls:           │   │                                    │
│  │ aprilTagSim      │   │                                    │
│  │ .simulateAprilTags() │                                    │
│  └─────────────────┘   │                                     │
│                        │                                     │
│  ┌─────────────────────▼───────────────────────┐            │
│  │ AprilTagSimulator                           │            │
│  │                                              │            │
│  │ 1. Read robot position from                 │            │
│  │    ProgramInterface.robotPosition           │            │
│  │                                              │            │
│  │ 2. Calculate which field tags are visible   │            │
│  │    (within detection range)                 │            │
│  │                                              │            │
│  │ 3. Transform tag positions from field       │            │
│  │    frame to robot-relative frame            │            │
│  │                                              │            │
│  │ 4. Publish to NetworkTables:                │            │
│  │    /SmartDashboard/<topicName>              │            │
│  └──────────────────┬──────────────────────────┘            │
│                     │                                        │
└─────────────────────┼────────────────────────────────────────┘
                      │
                      │ NetworkTables
                      │ (UDP port 5810)
                      │
                      ↓
         [Robot code reads it back via GetOrinRawValue]
```

## Usage

### Option 1: Integrated Mode (Recommended)

The AprilTag simulator runs **inside** the robot code process. This is simpler and already integrated.

1. **Start simulation**:
   ```bash
   ./gradlew simulateJava
   ```

2. **Configuration** (in `RobotMain.simulationInit()`):
   ```java
   aprilTagSimulator = new AprilTagSimulator(
       "apriltag_detections",  // NetworkTables topic name
       5.0,                     // Detection range in meters
       20.0                     // Publish rate in Hz
   );
   ```

3. **Configure field tags** (in `AprilTagSimulator.initializeFieldTags()`):
   ```java
   // Add tags at known field positions
   fieldTags.add(new FieldAprilTag(1, 0.0, 0.0, 0.5));
   fieldTags.add(new FieldAprilTag(2, 5.0, 0.0, 0.5));
   // ... add more based on your field layout
   ```

4. **Update your Orin mechanism** to use the correct topic:
   ```java
   // In your robot's Orin mechanism or wherever you create GetOrinRawValue:
   GetOrinRawValue orinReader = new GetOrinRawValue("apriltag_detections", 0.1);
   ```

### Option 2: Standalone Process

Run the AprilTag simulator as a **separate Java process**. This better mimics the real Orin architecture.

1. **Add to `build.gradle`**:
   ```gradle
   task runAprilTagSim(type: JavaExec) {
       mainClass = "com.team766.simulator.StandaloneAprilTagSimulator"
       classpath = sourceSets.main.runtimeClasspath
       args = ["localhost", "apriltag_detections", "5.0", "20.0"]
       // args: [ntServer, topicName, rangeMeters, rateHz]
   }
   ```

2. **Start robot code** (in one terminal):
   ```bash
   ./gradlew simulateJava
   ```

3. **Start AprilTag simulator** (in another terminal):
   ```bash
   ./gradlew runAprilTagSim
   ```

   Or with custom arguments:
   ```bash
   ./gradlew runAprilTagSim --args="localhost my_topic 8.0 30.0"
   ```

## Configuration

### NetworkTables Topic Name

The topic name must match what your Orin mechanism expects:

**In simulation** (`RobotMain.java`):
```java
aprilTagSimulator = new AprilTagSimulator(
    "apriltag_detections",  // ← This topic name
    5.0, 20.0
);
```

**In robot code** (wherever you create `GetOrinRawValue`):
```java
GetOrinRawValue orinReader = new GetOrinRawValue(
    "apriltag_detections",  // ← Must match!
    0.1  // covariance
);
```

### Field Tag Positions

Update the field tag positions in `AprilTagSimulator.initializeFieldTags()`:

```java
private void initializeFieldTags() {
    // Tag ID, X (meters), Y (meters), Z (meters - height)
    fieldTags.add(new FieldAprilTag(1, 0.0, 0.0, 0.5));
    fieldTags.add(new FieldAprilTag(2, 5.0, 0.0, 0.5));
    // ... add all field tags here
}
```

**Important**: These positions should match the actual AprilTag layout for your game's field. You can usually find these in the FRC game manual or WPILib's `AprilTagFieldLayout`.

### Detection Parameters

**Detection Range**: Maximum distance (in meters) at which tags can be detected
```java
aprilTagSimulator = new AprilTagSimulator(
    "apriltag_detections",
    5.0,  // ← Detection range in meters
    20.0
);
```

**Publish Rate**: How often (Hz) to publish detections
```java
aprilTagSimulator = new AprilTagSimulator(
    "apriltag_detections",
    5.0,
    20.0  // ← 20 Hz = every 50ms
);
```

## How It Works

1. **Every simulation tick** (`simulationPeriodic`):
   - `aprilTagSimulator.simulateAprilTags(currentTime)` is called

2. **Inside `simulateAprilTags()`**:
   - Reads robot position from `ProgramInterface.robotPosition` (x, y, heading)
   - For each field tag:
     - Calculates distance from robot to tag
     - If within detection range:
       - Transforms tag position from field frame to robot frame
       - Adds to detection array: `[timestamp, tagId, x, y, z]`
   - Publishes array to NetworkTables

3. **Your robot code**:
   - `Orin` mechanism calls `GetApriltagPoseData.getAllTags()`
   - Which calls `GetOrinRawValue.getRawPoseData()`
   - Which reads from NetworkTables
   - Returns list of `TimestampedApriltag` objects
   - Your code uses these for localization/targeting/etc.

## Testing

### Verify NetworkTables Communication

1. Start simulation with AprilTag simulator
2. Open **OutlineViewer** (WPILib tool):
   ```bash
   ~/wpilib/2025/tools/OutlineViewer.jar
   ```
3. Connect to `localhost`
4. Look for `/SmartDashboard/apriltag_detections`
5. You should see arrays of doubles being published

### Debug Output

The simulator prints debug info when tags are detected:
```
[AprilTagSim] Published 2 tags at t=5.43
```

### Test with Specific Tags

For testing, you can bypass the automatic detection and inject specific tags:
```java
// In your test code:
aprilTagSimulator.simulateSpecificTag(
    currentTime,  // timestamp
    1,            // tag ID
    2.0, 0.5, 0.3 // x, y, z in robot frame
);
```

## Troubleshooting

### "No tags detected"
- Check that field tags are configured in `initializeFieldTags()`
- Verify detection range is large enough
- Check that robot is moving in simulation (look at `ProgramInterface.robotPosition`)

### "GetOrinRawValue throws ValueNotFoundOnTableError"
- Verify topic names match between simulator and `GetOrinRawValue`
- Check NetworkTables connection (use OutlineViewer)
- Make sure simulator is running and publishing

### "Wrong tag positions"
- Verify field tag coordinates are correct (check field layout)
- Check that coordinate frame is correct (WPILib uses blue alliance origin)
- Make sure robot position is being set correctly in simulation

## Future Enhancements

Potential improvements to make simulation more realistic:

1. **Camera FOV**: Only detect tags within camera field of view
2. **Occlusion**: Don't detect tags behind obstacles
3. **Noise**: Add realistic sensor noise to detections
4. **Latency**: Add network latency to simulate real Orin→RoboRIO delay
5. **Multiple cameras**: Simulate multiple cameras with different poses
6. **Tag orientation**: Include full 6DOF pose (currently only translation)

## See Also

- `src/main/java/com/team766/simulator/AprilTagSimulator.java` - Main simulator
- `src/main/java/com/team766/simulator/StandaloneAprilTagSimulator.java` - Standalone version
- `src/main/java/com/team766/orin/GetOrinRawValue.java` - NetworkTables reader
- `src/main/java/com/team766/orin/GetApriltagPoseData.java` - Data parser
- `src/main/java/com/team766/robot/reva/mechanisms/Orin.java` - Orin mechanism
