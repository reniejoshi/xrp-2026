/*
 * MIT License
 *
 * Copyright (c) 2026 Bear Metal
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.tahomarobotics.robot.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import edu.wpi.first.wpilibj.RobotController;
import org.littletonrobotics.junction.Logger;

/**
 * Performance monitoring utility that measures execution times and computes
 * windowed statistics (mean, std dev, peak, % utilization) for registered tasks.
 * Unregistered tasks fall back to raw per-call logging.
 */
public class PerformanceMonitor {

    private static final double US_TO_MS = 0.001;

    /** Expected cycle period for 500Hz tasks (2ms). */
    public static final double PERIOD_FAST_MS = 2.0;

    /** Expected cycle period for 50Hz tasks (20ms). */
    public static final double PERIOD_SLOW_MS = 20.0;

    private static final Map<String, TaskStats> statsMap = new HashMap<>();

    /**
     * Windowed statistics using Welford's online algorithm.
     * Accumulates samples over a ~1 second window, publishes summary, then resets.
     */
    private static class TaskStats {
        final double periodMs;
        final int publishInterval;

        int count;
        double mean;
        double m2;
        double peak;

        TaskStats(double periodMs) {
            this.periodMs = periodMs;
            // ~1 second window: 50 calls at 50Hz, 500 calls at 500Hz
            this.publishInterval = (int) (1000.0 / periodMs);
        }

        void update(double execTimeMs) {
            count++;
            double delta = execTimeMs - mean;
            mean += delta / count;
            double delta2 = execTimeMs - mean;
            m2 += delta * delta2;
            peak = Math.max(peak, execTimeMs);
        }

        double stdDev() {
            return count > 1 ? Math.sqrt(m2 / count) : 0;
        }

        double utilizationPct() {
            return (mean / periodMs) * 100.0;
        }

        boolean shouldPublish() {
            return count >= publishInterval;
        }

        void reset() {
            count = 0;
            mean = 0;
            m2 = 0;
            peak = 0;
        }
    }

    /**
     * Register a task for statistical tracking.
     *
     * @param logName  the task name (e.g. "Subsystem/ChassisSubsystem")
     * @param periodMs the expected cycle period in milliseconds (use PERIOD_FAST_MS or PERIOD_SLOW_MS)
     */
    public static void registerTask(String logName, double periodMs) {
        statsMap.put(logName, new TaskStats(periodMs));
    }

    /**
     * Measures and logs the execution time of a void operation.
     */
    public static void logExecutionTime(String logName, Runnable task) {
        long startTime = RobotController.getFPGATime();
        try {
            task.run();
        } finally {
            long endTime = RobotController.getFPGATime();
            double executionTimeMs = (endTime - startTime) * US_TO_MS;
            recordStats(logName, executionTimeMs);
        }
    }

    /**
     * Measures and logs the execution time of an operation that returns a value.
     */
    public static <T> T logExecutionTime(String logName, Supplier<T> supplier) {
        long startTime = RobotController.getFPGATime();
        try {
            return supplier.get();
        } finally {
            long endTime = RobotController.getFPGATime();
            double executionTimeMs = (endTime - startTime) * US_TO_MS;
            recordStats(logName, executionTimeMs);
        }
    }

    private static void recordStats(String logName, double executionTimeMs) {
        TaskStats stats = statsMap.get(logName);
        if (stats != null) {
            stats.update(executionTimeMs);
            if (stats.shouldPublish()) {
                Logger.recordOutput("PerfMon/" + logName + "/avgMs", stats.mean);
                Logger.recordOutput("PerfMon/" + logName + "/stdDevMs", stats.stdDev());
                Logger.recordOutput("PerfMon/" + logName + "/utilPct", stats.utilizationPct());
                Logger.recordOutput("PerfMon/" + logName + "/peakMs", stats.peak);
                stats.reset();
            }
        } else {
            // Unregistered task: fall back to raw per-call logging
            Logger.recordOutput("ExecTimes/" + logName, executionTimeMs);
        }
    }
}
