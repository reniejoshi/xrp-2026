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
package org.tahomarobotics.robot;

import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.apriltag.AprilTagFields;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.units.measure.Distance;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.geometry.Rectangle2d;

import static edu.wpi.first.units.Units.Meters;

import com.pathplanner.lib.util.FlippingUtil;

public final class FieldConstants {
    public static final AprilTagFieldLayout APRIL_TAG_LAYOUT = AprilTagFieldLayout.loadField(AprilTagFields.k2026RebuiltWelded);
    public static final Distance FIELD_LENGTH = Meters.of(APRIL_TAG_LAYOUT.getFieldLength()); // 54 feet
    public static final Distance FIELD_WIDTH = Meters.of(APRIL_TAG_LAYOUT.getFieldWidth());  // 27 feet

    public static final Pose2d BLUE_HUB_POSE = new Pose2d(
            4.62583,
            FIELD_WIDTH.in(Meters) / 2d,
            new Rotation2d());

    public static final Pose2d RED_HUB_POSE = FlippingUtil.flipFieldPose(BLUE_HUB_POSE);

    public static final Pose2d BLUE_DEPOT_PASSING_POSE = new Pose2d(
            0.5,
            6.5,
            new Rotation2d());

    public static final Pose2d BLUE_OUTPOST_PASSING_POSE = new Pose2d(
            0.5,
            1.5,
            new Rotation2d());

    public static final Pose2d RED_DEPOT_PASSING_POSE =
            FlippingUtil.flipFieldPose(BLUE_OUTPOST_PASSING_POSE);

    public static final Pose2d RED_OUTPOST_PASSING_POSE =
            FlippingUtil.flipFieldPose(BLUE_DEPOT_PASSING_POSE);

    public static final double WIDTH_CENTER_LINE = FIELD_WIDTH.in(Meters) / 2d;

    public static Distance getTrenchLine() {
        boolean isRedAlliance = DriverStation.getAlliance().orElse(DriverStation.Alliance.Blue)
                == DriverStation.Alliance.Red;
        return isRedAlliance ? RED_TRENCH_LINE_X : BLUE_TRENCH_LINE_X;
    }

    public static boolean isOnScoringSide(Pose2d pose) {
        boolean isRedAlliance = DriverStation.getAlliance().orElse(DriverStation.Alliance.Blue)
                == DriverStation.Alliance.Red;
        Distance poseX = pose.getMeasureX();

        return isRedAlliance
            ? poseX.gt(RED_TRENCH_LINE_X)
            : poseX.lt(BLUE_TRENCH_LINE_X);
    }

    public static boolean isOnPoachingSide(Pose2d pose) {
        boolean isRedAlliance = DriverStation.getAlliance().orElse(DriverStation.Alliance.Blue)
                == DriverStation.Alliance.Red;
        Distance poseX = pose.getMeasureX();

        return isRedAlliance
            ? poseX.lt(BLUE_TRENCH_LINE_X)
            : poseX.gt(RED_TRENCH_LINE_X);
    }

    public static final Distance BLUE_TRENCH_LINE_X = Meters.of(5.25);
    public static final Distance RED_TRENCH_LINE_X = Meters.of(11.25);

    public static final RectangleWithCorners BLUE_LEFT_TRENCH = new RectangleWithCorners(
            new Translation2d(3.75, 0.0), new Translation2d(5.5, 2.0));
    public static final RectangleWithCorners BLUE_RIGHT_TRENCH = new RectangleWithCorners(
            new Translation2d(3.75, 6.0), new Translation2d(5.5, 8.0));
    public static final RectangleWithCorners RED_LEFT_TRENCH = new RectangleWithCorners(
            new Translation2d(11, 0.0), new Translation2d(12.75, 2.0));
    public static final RectangleWithCorners RED_RIGHT_TRENCH = new RectangleWithCorners(
            new Translation2d(11, 6.0), new Translation2d(12.75, 8.0));

    public static final double TRENCH_EXPAND_VELOCITY_THRESHOLD = 2.5; // m/s - minimum speed to start expanding
    public static final double TRENCH_EXPAND_SCALE = 0.3; // meters expansion per m/s of velocity

    public record RectangleWithCorners(Translation2d ll, Translation2d ur) {
        public Rectangle2d rect() {
            return new Rectangle2d(ll, ur);
        }

        public RectangleWithCorners expand(double amount) {
            return new RectangleWithCorners(
                    new Translation2d(ll.getX() - amount, ll.getY() - amount),
                    new Translation2d(ur.getX() + amount, ur.getY() + amount));
        }

        public boolean contains(Translation2d point) {
            return rect().contains(point);
        }
    }
}
