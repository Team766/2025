package com.team766.simulator;

import edu.wpi.first.networktables.DoubleArrayPublisher;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simulates an Orin coprocessor publishing AprilTag detections to NetworkTables.
 * Simplified version - publishes same data to multiple camera topics without worrying
 * about camera extrinsics or field of view.
 *
 * The Orin publishes AprilTag data as double arrays with the format:
 * [timestamp, tagId, x, y, z, timestamp, tagId, x, y, z, ...]
 *
 * Usage:
 * - Create simulator with camera topic names
 * - Call simulateAprilTags() periodically during simulation
 */
public class AprilTagSimulator {

    private final NetworkTableInstance ntInstance;
    private final NetworkTable table;
    private final Map<String, DoubleArrayPublisher> publishers = new HashMap<>();

    // Configuration
    private final double detectionRange; // meters
    private final double publishRateHz;

    // State
    private double lastPublishTime = 0.0;

    /**
     * Represents an AprilTag on the field with a known position.
     */
    public static class FieldAprilTag {
        public final int id;
        public final double x; // meters
        public final double y; // meters
        public final double z; // meters (height)

        public FieldAprilTag(int id, double x, double y, double z) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    // Known AprilTag locations on the field
    private final List<FieldAprilTag> fieldTags = new ArrayList<>();

    /**
     * Create AprilTag simulator.
     *
     * @param cameraTopics List of NetworkTables topic names (e.g., ["left_back", "right_front"])
     * @param detectionRangeMeters Maximum detection range in meters
     * @param publishRateHz How often to publish detections
     */
    public AprilTagSimulator(
            String[] cameraTopics, double detectionRangeMeters, double publishRateHz) {
        this.detectionRange = detectionRangeMeters;
        this.publishRateHz = publishRateHz;

        ntInstance = NetworkTableInstance.getDefault();
        table = ntInstance.getTable("/SmartDashboard");

        // Create publishers for each camera topic
        for (String topic : cameraTopics) {
            DoubleArrayPublisher publisher = table.getDoubleArrayTopic(topic).publish();
            publishers.put(topic, publisher);
            System.out.printf("[AprilTagSim] Publishing to topic: %s%n", topic);
        }

        // Initialize with some example tags (update these for your field)
        initializeFieldTags();
    }

    /**
     * Configure the known AprilTag positions on the field.
     * You should update this based on your actual field layout.
     */
    private void initializeFieldTags() {
        // Example: Add some test tags
        // Format: (tagId, x, y, z)
        fieldTags.add(new FieldAprilTag(1, 0.0, 0.0, 0.5)); // Tag 1 at origin
        fieldTags.add(new FieldAprilTag(2, 5.0, 0.0, 0.5)); // Tag 2 at 5m
        fieldTags.add(new FieldAprilTag(3, 5.0, 5.0, 0.5)); // Tag 3 at corner
        fieldTags.add(new FieldAprilTag(6, 0.0, 5.0, 0.5)); // Tag 6 at corner
        // Add more tags based on your field layout
    }

    /**
     * Add a field tag dynamically (useful for testing).
     */
    public void addFieldTag(int id, double x, double y, double z) {
        fieldTags.add(new FieldAprilTag(id, x, y, z));
    }

    /**
     * Simulates AprilTag detections based on robot position.
     * Publishes the same data to all camera topics.
     * Call this periodically during simulation (e.g., every 20ms in simulationPeriodic).
     *
     * @param currentTime Current simulation time in seconds
     */
    public void simulateAprilTags(double currentTime) {
        // Rate limiting - only publish at the configured rate
        double timeSinceLastPublish = currentTime - lastPublishTime;
        if (timeSinceLastPublish < (1.0 / publishRateHz)) {
            return;
        }
        lastPublishTime = currentTime;

        // Get robot position from simulation
        double robotX = ProgramInterface.robotPosition.x;
        double robotY = ProgramInterface.robotPosition.y;
        double robotHeading = ProgramInterface.robotPosition.heading; // degrees

        // Find all tags within detection range
        List<Double> detectionData = new ArrayList<>();

        for (FieldAprilTag tag : fieldTags) {
            double dx = tag.x - robotX;
            double dy = tag.y - robotY;
            double distance = Math.sqrt(dx * dx + dy * dy);

            // Only detect tags within range
            if (distance <= detectionRange) {
                // Convert tag position from field frame to robot frame
                // This simulates what the camera would see
                double angleToTag = Math.atan2(dy, dx) - Math.toRadians(robotHeading);
                double relativeX = distance * Math.cos(angleToTag);
                double relativeY = distance * Math.sin(angleToTag);
                double relativeZ = tag.z; // Simplified - doesn't account for robot tilt

                // Add to detection array: [timestamp, tagId, x, y, z]
                detectionData.add(currentTime);
                detectionData.add((double) tag.id);
                detectionData.add(relativeX);
                detectionData.add(relativeY);
                detectionData.add(relativeZ);
            }
        }

        // Publish to all camera topics
        if (!detectionData.isEmpty()) {
            double[] array = detectionData.stream().mapToDouble(Double::doubleValue).toArray();
            for (Map.Entry<String, DoubleArrayPublisher> entry : publishers.entrySet()) {
                entry.getValue().set(array);
            }

            // Debug output
            System.out.printf(
                    "[AprilTagSim] Published %d tags at t=%.2f (robot: %.2f, %.2f, %.1f°)%n",
                    detectionData.size() / 5, currentTime, robotX, robotY, robotHeading);
        } else {
            // Publish empty array when no tags detected
            double[] emptyArray = new double[0];
            for (DoubleArrayPublisher publisher : publishers.values()) {
                publisher.set(emptyArray);
            }
        }
    }

    /**
     * Stop publishing and clean up resources.
     */
    public void close() {
        for (DoubleArrayPublisher publisher : publishers.values()) {
            publisher.close();
        }
    }
}
