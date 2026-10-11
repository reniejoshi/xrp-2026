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
package org.tahomarobotics.robot.vision;

import edu.wpi.first.math.geometry.*;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Commands;

import org.tinylog.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.tahomarobotics.robot.FieldConstants;

import static edu.wpi.first.units.Units.Degrees;
import static edu.wpi.first.units.Units.Inches;

public class CameraMountEstimation {
    private static final HashMap<String, double[]> mountPositions = new HashMap<>();

    // Whether to estimate the next AprilTag update from each camera.
    private static final Map<String, Boolean> estimate =
                getCameraNames().stream()
                        .collect(Collectors.toMap(s -> s, $ -> false));
        
    private static List<String> getCameraNames() {
        // TODO: return list of camera names
        return List.of("No cameras implemented");
    }

    public static void publishCameraMountEstimationCommands(Runnable applyEstimatedCameraOffsets, Runnable saveAppliedCameraOffsets) {
        // Initialize all of our inputs on SmartDashboard
        SmartDashboard.putData(
                "Estimate Camera Positions", Commands.runOnce(() -> estimate.replaceAll((n, v) -> true)).ignoringDisable(true)
        );

        SmartDashboard.putData(
                "Update Camera Positions", Commands.runOnce(applyEstimatedCameraOffsets).ignoringDisable(true)
        );

        SmartDashboard.putData(
            "Save Camera Positions", Commands.runOnce(saveAppliedCameraOffsets).ignoringDisable(true)
        );

        SmartDashboard.putNumber("Actual Chassis Pose Out from Tag (Inches)", 0);
        SmartDashboard.putNumber("Actual Chassis Pose Left of Tag (Inches)", 0);
        SmartDashboard.putNumber("Tag Height from Ground (Inches)", 0);
        SmartDashboard.putNumber("Actual Chassis Pose Heading compared to Tag (Degrees)", 180);
    }

    public static void estimateMountPositions(Limelight.EstimatedRobotPose estimatedRobotPose) {
        if (estimate.getOrDefault(estimatedRobotPose.camera().getName(), false)) {
            double out = SmartDashboard.getNumber("Actual Chassis Pose Out from Tag (Inches)", 0);
            double left = SmartDashboard.getNumber("Actual Chassis Pose Left of Tag (Inches)", 0);
            double height = SmartDashboard.getNumber("Tag Height from Ground (Inches)", 0);
            // Heading is 0 when facing the tag and goes up when rotating the robot counterclockwise from that position.
            double h = SmartDashboard.getNumber("Actual Chassis Pose Heading compared to Tag (Degrees)", 180);

            Transform3d targetToChassisTransform = new Transform3d(
                // -x goes out from tag, and -z goes down from tag.
                new Translation3d(Inches.of(-out), Inches.of(left), Inches.of(-height)), 
                new Rotation3d(Degrees.of(0), Degrees.of(0), Degrees.of(h))
            );

            // Get the expected field-pose of the corresponding apriltag on the field
            Pose3d targetPose = FieldConstants.APRIL_TAG_LAYOUT.getTagPose(estimatedRobotPose.poseEstimate().rawFiducials[0].id).orElseThrow();

            Pose3d actualChassisPose = targetPose.plus(targetToChassisTransform);

            // Get the camera position relative to the target or output an error message if none is found.
            Pose3d targetToCameraPose = estimatedRobotPose.camera().getCameraPoseTargetSpace().orElseGet(
                () -> {
                    org.tinylog.Logger.error("CAMERA MOUNT ESTIMATION DID NOT FIND A TARGET. These estimations will not be valid.");
                    return null;
                }
            );

            if (targetToCameraPose == null) {
                return;
            }

            Transform3d targetToCameraTransform = new Transform3d(targetToCameraPose.getTranslation(), targetToCameraPose.getRotation());

            // Subtract the camera-to-target transform from the target field-pose to get the expected field-to-camera position
            Pose3d cameraPose = targetPose.plus(targetToCameraTransform);

            // Subtract the field-to-camera pose from the actual field-to-chassis pose to get the expected camera-to-chassis transform
            Transform3d cameraToRobotTransform = cameraPose.minus(actualChassisPose);

            // Decompose the transformation into it's components
            Translation3d t = cameraToRobotTransform.getTranslation();
            Rotation3d r = cameraToRobotTransform.getRotation();

            // Publish the results to SmartDashboard
            mountPositions.put(
                estimatedRobotPose.camera().getName(), new double[]{
                    t.getX(),
                    t.getY(),
                    t.getZ(),
                    r.getX(),
                    r.getY(),
                    r.getZ()}
            );
            Logger.info(
                "Estimated camera-to-robot transform for {}: translation=({}, {}, {}), rotation(rad)=({}, {}, {})",
                estimatedRobotPose.camera().getName(),
                t.getX(), t.getY(), t.getZ(),
                r.getX(), r.getY(), r.getZ()
            );
            estimate.put(estimatedRobotPose.camera().getName(), false);
        }
    }

    public static double[] getEstimatedMountPose(String cameraName) {
        return mountPositions.get(cameraName);
    }
}