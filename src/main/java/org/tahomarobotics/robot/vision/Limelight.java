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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import edu.wpi.first.math.Vector;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.math.geometry.Translation3d;
import edu.wpi.first.math.numbers.N3;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.wpilibj.Preferences;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.button.RobotModeTriggers;

import static edu.wpi.first.units.Units.Degrees;
import static edu.wpi.first.units.Units.Meters;
import static edu.wpi.first.units.Units.Radians;

import org.littletonrobotics.junction.Logger;
import org.tahomarobotics.robot.FieldConstants;
import org.tahomarobotics.robot.sim.Arena2026Rebuilt;
import org.tahomarobotics.robot.util.LimelightCoordinateConversion;
import org.tahomarobotics.robot.vision.VisionConstants.CameraConfiguration;
import org.tahomarobotics.robot.vision.VisionConstants.IMUMode;

public final class Limelight {

    // advantage scope places the camera 1/2 meter above ground
    public static final double ADVANTAGE_SCOPE_CAMERA_HEIGHT = 0.5;

    // --- Fields and Constants ---
    private final String name;
    private final Supplier<Rotation2d> chassisHeadingSupplier;
    private Transform3d offset;

    // --- Constructor ---

    public Limelight(CameraConfiguration config, Supplier<Rotation2d> chassisHeadingSupplier) {
        this.name = config.name();
        this.chassisHeadingSupplier = chassisHeadingSupplier;
        LimelightHelpers.SetIMUMode(name, IMUMode.EXTERNAL_SEED.mode);
        RobotModeTriggers.disabled()
            .onTrue(Commands.runOnce(() -> LimelightHelpers.SetIMUMode(name, IMUMode.EXTERNAL_SEED.mode)).withName("Disable Vision"));
        // RobotModeTriggers.teleop().or(RobotModeTriggers.autonomous())
        //     .onTrue(Commands.runOnce(() -> LimelightHelpers.SetIMUMode(name, IMUMode.INTERNAL_EXTERNAL_ASSIST.mode)));
        LimelightHelpers.SetIMUAssistAlpha(name, 0.01);
        loadCameraOffset(config.offset());
    }

    public CameraConfiguration getCameraConfiguration() {
        return new CameraConfiguration(name, offset);
    }

    public Optional<EstimatedRobotPose> getEstimatedRobotPoseMegaTag1() {
        LimelightHelpers.PoseEstimate mt1 = LimelightHelpers.getBotPoseEstimate_wpiBlue(name);

        if (mt1 == null || mt1.tagCount <= 0) {
            return Optional.empty();
        }
        return Optional.of(new EstimatedRobotPose(mt1, this, VisionConstants.MEGATAG1_STANDARD_DEVIATIONS));
    }

    public Optional<EstimatedRobotPose> getEstimatedRobotPoseMegaTag2() {
        LimelightHelpers.PoseEstimate mt2 = LimelightHelpers.getBotPoseEstimate_wpiBlue_MegaTag2(name);

        if (mt2 == null || mt2.tagCount <= 0) {
            return Optional.empty();
        }
        return Optional.of(new EstimatedRobotPose(mt2, this, VisionConstants.MEGATAG2_STANDARD_DEVIATIONS));
    }

    /*
     * This function outputs the position of the camera in the following coordinate
     * system
     * relative to the target:
     * x - forward distance from target
     * y - right-left distance from target (right from back of the target is
     * positive)
     * z - height
     * 
     * All of the rotation is relative to the target.
     * 
     */
    public Optional<Pose3d> getCameraPoseTargetSpace() {
        Pose3d cameraPoseTargetSpace = LimelightHelpers.getCameraPose3d_TargetSpace(name);
        if (cameraPoseTargetSpace == null) {
            return Optional.empty();
        }

        // Limelight reports camera pose in target space using:
        // translation x = distance to right of tag,
        // translation y = height compared to tag,
        // translation z = distance in front of tag
        // rotation x = pitch compared to tag
        // rotation y = yaw compared to tag
        // roation z = roll compared to tag
        // so we need to convert from their coordinates to standard coordinates here.
        Pose3d realCameraPose = LimelightCoordinateConversion.fromLimelightTargetSpace(cameraPoseTargetSpace);

        return Optional.of(realCameraPose);
    }

    public void setCameraOffset(Transform3d cameraOffset) {
        offset = cameraOffset;

        Pose3d robotSpaceOffset = LimelightCoordinateConversion.toLimelightRobotSpace(new Pose3d(cameraOffset.getTranslation(), cameraOffset.getRotation()));

        LimelightHelpers.setCameraPose_RobotSpace(
                name,
                robotSpaceOffset.getMeasureX().in(Meters),
                robotSpaceOffset.getMeasureY().in(Meters),
                robotSpaceOffset.getMeasureZ().in(Meters),
                robotSpaceOffset.getRotation().getMeasureX().in(Degrees),
                robotSpaceOffset.getRotation().getMeasureY().in(Degrees),
                robotSpaceOffset.getRotation().getMeasureZ().in(Degrees));
    }

    public void saveAppliedCameraOffset() {
        // Saved in meters and radians
        Preferences.setDouble(name + " Offset X", offset.getX());
        Preferences.setDouble(name + " Offset Y", offset.getY());
        Preferences.setDouble(name + " Offset Z", offset.getZ());
        Preferences.setDouble(name + " Offset Roll", offset.getRotation().getX());
        Preferences.setDouble(name + " Offset Pitch", offset.getRotation().getY());
        Preferences.setDouble(name + " Offset Yaw", offset.getRotation().getZ());
    }

    public void applyEstimatedCameraOffset() {
        double[] offsetArray = CameraMountEstimation.getEstimatedMountPose(name);

        // Assume that if the offsets are 0s, we didn't run estimation correctly
        if (offsetArray == null) {
            org.tinylog.Logger.error("No estimated offset found for " + name + ", skipping.");
            return;
        }

        setCameraOffset(new Transform3d(
                new Translation3d(offsetArray[0], offsetArray[1], offsetArray[2]),
                new Rotation3d(offsetArray[3], offsetArray[4], offsetArray[5])));
    }

    public void loadCameraOffset(Transform3d defaultOffset) {
        if (!Preferences.containsKey(name + "Offset X")) {
            org.tinylog.Logger.warn("No saved camera offset found for " + name + ", using default.");
            setCameraOffset(defaultOffset);
        }

        double x = Preferences.getDouble(name + " Offset X", offset.getX());
        double y = Preferences.getDouble(name + " Offset Y", offset.getY());
        double z = Preferences.getDouble(name + " Offset Z", offset.getZ());
        double roll = Preferences.getDouble(name + " Offset Roll", offset.getRotation().getX());
        double pitch = Preferences.getDouble(name + " Offset Pitch", offset.getRotation().getY());
        double yaw = Preferences.getDouble(name + " Offset Yaw", offset.getRotation().getZ());

        setCameraOffset(new Transform3d(new Translation3d(x, y, z), new Rotation3d(roll, pitch, yaw)));
    }

    private void seedHeading() {
        Angle yaw = chassisHeadingSupplier.get().getMeasure();
        LimelightHelpers.SetRobotOrientation(name, 
            yaw.in(Degrees), 0, 
            0, 0, 
            0, 0
        );
    }

    public void cameraPeriodic() {
        seedHeading();

        LimelightHelpers.IMUData data = LimelightHelpers.getIMUData(name);
        Logger.recordOutput("Vision/" + name + "/ IMU Roll", Degrees.of(data.Roll).in(Radians));
        Logger.recordOutput("Vision/" + name + "/ IMU Pitch", Degrees.of(data.Pitch).in(Radians));
        Logger.recordOutput("Vision/" + name + "/ IMU Yaw", Degrees.of(data.Yaw).in(Radians));
    }

    public record EstimatedRobotPose(LimelightHelpers.PoseEstimate poseEstimate, Limelight camera, Vector<N3> stdDevs) {}

    public String getName() {
        return name;
    }

    public void publishCameraPose() {

        // time = null return current robot pose
        Pose3d robot = new Pose3d(Arena2026Rebuilt.getInstance().getRobotPose());

        // add the camera offsets
        Pose3d camera = robot.plus(offset);

        // lower camera to match advantage scope offset
        camera = new Pose3d(camera.getX(), camera.getY(), camera.getZ() - 0.5,
                camera.getRotation());
        Logger.recordOutput("Vision/" + name + "/cameraPose", camera);
    }

    public void publishTargets(EstimatedRobotPose estPose) {

        List<Pose3d> targets = Arrays.stream(estPose.poseEstimate.rawFiducials)
            .map(t -> FieldConstants.APRIL_TAG_LAYOUT.getTagPose(t.id))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();

        Logger.recordOutput("Vision/" + name + "/targets", targets.toArray(Pose3d[]::new));
    }

}
