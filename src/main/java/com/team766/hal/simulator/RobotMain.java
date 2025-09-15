package com.team766.hal.simulator;

import java.io.IOException;

// This is a backwards compatibility shim for existing 3d simulator packages.
// New applications should run `./gradlew simulateJava` directly.
public class RobotMain {
    public static void main(final String[] args) throws IOException {
        var process = new ProcessBuilder().command("./gradlew", "simulateJava").start();
        while (true) {
            try {
                System.exit(process.waitFor());
            } catch (InterruptedException e) {
            }
        }
    }
}
