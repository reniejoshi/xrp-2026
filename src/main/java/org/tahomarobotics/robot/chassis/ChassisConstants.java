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
package org.tahomarobotics.robot.chassis;

public class ChassisConstants {
    public static final double CONTROLLER_ROTATIONAL_SENSITIVITY = 2d;
    public static final double CONTROLLER_TRANSLATIONAL_SENSITIVITY = 1.3d;
    public static final double CONTROLLER_DEADBAND = 0.09d;

    private enum DriveGearRatio {
        L1(12d), L2(14d), L3(16d);

        public final double driveRatio;
        public final double couplingRatio;
        public final double steeringRatio;

        DriveGearRatio(double value) {
            this.driveRatio = (54d / value) * (25d / 32d) * (30d / 15d);
            this.couplingRatio = 54d / value;
            this.steeringRatio = (49d / 7d) * (41d / 11d);
        }
    }

    public static final double DRIVE_GEAR_RATIO = DriveGearRatio.L1.driveRatio;
    public static final double STEER_GEAR_RATIO = DriveGearRatio.L1.steeringRatio;
    public static final double COUPLING_GEAR_RATIO = DriveGearRatio.L1.couplingRatio;
}
