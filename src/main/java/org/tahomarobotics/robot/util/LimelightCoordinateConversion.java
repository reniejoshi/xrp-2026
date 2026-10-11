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

import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Translation3d;

/**
 * Utility class for converting between WPILib and Limelight coordinate systems.
 *
 * WPILib Convention (right-hand rule):
 *   X+ = Forward
 *   Y+ = Left
 *   Z+ = Up
 *   Rotations: CCW positive around each axis (right-hand rule)
 *
 * Limelight Robot Space Convention (right-hand rule):
 *   X+ = Forward
 *   Y+ = Right   <-- NOTE: opposite of WPILib
 *   Z+ = Up
 *   Rotations: right-hand rule around each axis, but Y-axis flip
 *              means roll, pitch, yaw signs change accordingly
 *
 * Limelight Camera Space Convention:
 *   X+ = Right (from camera's perspective)
 *   Y+ = Down
 *   Z+ = Out of camera (forward)
 *
 * The axis mapping from Limelight Camera Space -> WPILib:
 *   LL_Z (forward) -> WPILib X (forward)
 *   LL_X (right)   -> WPILib Y negated (left = -right)
 *   LL_Y (down)    -> WPILib Z negated (up = -down)
 */
public class LimelightCoordinateConversion {

    // =========================================================================
    // Robot Space Conversions (Limelight Robot Space <-> WPILib)
    //
    // Limelight Robot Space:  X=Forward, Y=Right,  Z=Up
    // WPILib:                 X=Forward, Y=Left,   Z=Up
    //
    // Translation: llY -> -wpiY  (Right becomes Left negated)
    //              X and Z unchanged
    //
    // Rotation: Because Y is negated, rotations about Y (pitch) flip sign.
    //           Rotations about X (roll) and Z (yaw) also flip because
    //           they now rotate in the opposite sense relative to the new Y.
    //           Specifically:
    //             roll  (rotation about X): flips sign  (Y basis flipped)
    //             pitch (rotation about Y): flips sign  (Y axis negated)
    //             yaw   (rotation about Z): flips sign  (Y basis flipped)
    //
    //   Mathematically: if we negate Y, the right-hand rotation axes for
    //   roll and yaw reverse. The full rotation transform is:
    //     R_wpi = M * R_ll * M^T   where M = diag(1, -1, 1)
    //   which negates all three Euler angles simultaneously.
    // =========================================================================

    /**
     * Converts a Pose3d from Limelight Robot Space to WPILib coordinate system.
     *
     * @param limelightPose Pose3d in Limelight Robot Space
     *                      (X=Forward, Y=Right, Z=Up, degrees in Limelight NetworkTables)
     * @return Pose3d in WPILib coordinate system (X=Forward, Y=Left, Z=Up)
     */
    public static Pose3d fromLimelightRobotSpace(Pose3d limelightPose) {
        Translation3d t = limelightPose.getTranslation();
        Rotation3d r = limelightPose.getRotation();

        // Negate Y translation (Right -> Left)
        Translation3d wpiTranslation = new Translation3d(
            t.getX(),
            -t.getY(),
            t.getZ()
        );

        // Negate all rotation components due to Y-axis reflection
        // M * R * M^T with M=diag(1,-1,1) negates roll, pitch, and yaw
        Rotation3d wpiRotation = new Rotation3d(
            -r.getX(),  // roll
            -r.getY(),  // pitch
            -r.getZ()   // yaw
        );

        return new Pose3d(wpiTranslation, wpiRotation);
    }

    /**
     * Converts a Pose3d from WPILib coordinate system to Limelight Robot Space.
     *
     * @param wpiPose Pose3d in WPILib coordinate system (X=Forward, Y=Left, Z=Up)
     * @return Pose3d in Limelight Robot Space (X=Forward, Y=Right, Z=Up)
     */
    public static Pose3d toLimelightRobotSpace(Pose3d wpiPose) {
        // The transform is its own inverse (negating Y and then negating again = identity)
        return fromLimelightRobotSpace(wpiPose);
    }

    // =========================================================================
    // Camera Space Conversions (Limelight Camera Space <-> WPILib)
    //
    // Limelight Camera Space: X=Right, Y=Down,    Z=Forward(out of lens)
    // WPILib:                 X=Forward, Y=Left,  Z=Up
    //
    // Axis mapping:
    //   LL X (right)    -> WPILib -Y  (left = -right)
    //   LL Y (down)     -> WPILib -Z  (up   = -down)
    //   LL Z (forward)  -> WPILib  X  (forward = forward)
    //
    // Rotation: apply the same basis change to the rotation matrix.
    //   The rotation axes transform as the axes do.
    //   In terms of rotation vector components (rx, ry, rz) in LL camera space:
    //     WPILib rx (about X/forward) = LL rz (about Z/forward)
    //     WPILib ry (about Y/left)    = -LL rx (about X/right, negated)
    //     WPILib rz (about Z/up)      = -LL ry (about Y/down, negated)
    // =========================================================================

    /**
     * Converts a Pose3d from Limelight Camera Space to WPILib coordinate system.
     *
     * Use this when working with raw camera-space pose data such as
     * camerapose_targetspace or targetpose_cameraspace outputs.
     *
     * @param limelightCamPose Pose3d in Limelight Camera Space
     *                         (X=Right, Y=Down, Z=Out/Forward)
     * @return Pose3d in WPILib coordinate system (X=Forward, Y=Left, Z=Up)
     */
    public static Pose3d fromLimelightCameraSpace(Pose3d limelightCamPose) {
        Translation3d t = limelightCamPose.getTranslation();
        Rotation3d r = limelightCamPose.getRotation();

        // Remap axes: LL(X,Y,Z) -> WPILib(Z, -X, -Y) mapped to WPILib(X,Y,Z)
        Translation3d wpiTranslation = new Translation3d(
             t.getZ(),   // LL Z (forward)  -> WPILib X (forward)
            -t.getX(),   // LL X (right)    -> WPILib Y (left = -right)
            -t.getY()    // LL Y (down)     -> WPILib Z (up   = -down)
        );

        // Remap rotation axes the same way
        Rotation3d wpiRotation = new Rotation3d(
             r.getZ(),   // LL rz (about forward) -> WPILib rx (about forward/X)
            -r.getX(),   // LL rx (about right)   -> WPILib ry (about left/Y)
            -r.getY()    // LL ry (about down)     -> WPILib rz (about up/Z)
        );

        return new Pose3d(wpiTranslation, wpiRotation);
    }

    /**
     * Converts a Pose3d from WPILib coordinate system to Limelight Camera Space.
     *
     * @param wpiPose Pose3d in WPILib coordinate system (X=Forward, Y=Left, Z=Up)
     * @return Pose3d in Limelight Camera Space (X=Right, Y=Down, Z=Forward)
     */
    public static Pose3d toLimelightCameraSpace(Pose3d wpiPose) {
        Translation3d t = wpiPose.getTranslation();
        Rotation3d r = wpiPose.getRotation();

        // Inverse mapping: WPILib(X,Y,Z) -> LL(-Y, -Z, X) mapped to LL(X,Y,Z)
        Translation3d llTranslation = new Translation3d(
            -t.getY(),   // WPILib Y (left)    -> LL X (right = -left)
            -t.getZ(),   // WPILib Z (up)       -> LL Y (down  = -up)
             t.getX()    // WPILib X (forward)  -> LL Z (forward)
        );

        Rotation3d llRotation = new Rotation3d(
            -r.getY(),   // WPILib ry (about left)    -> LL rx (about right = -left)
            -r.getZ(),   // WPILib rz (about up)      -> LL ry (about down  = -up)
             r.getX()    // WPILib rx (about forward) -> LL rz (about forward)
        );

        return new Pose3d(llTranslation, llRotation);
    }

    // =========================================================================
    // Target Space Conversions (Limelight Target Space <-> WPILib)
    //
    // Limelight Target Space: X=Right (viewing target), Y=Down, Z=Out of target
    // This is the same axis layout as Camera Space, just with a different origin.
    // The conversion is therefore identical to Camera Space.
    // =========================================================================

    /**
     * Converts a Pose3d from Limelight Target Space to WPILib coordinate system.
     *
     * Use this when working with targetpose_cameraspace or robotpose_targetspace.
     *
     * @param limelightTargetPose Pose3d in Limelight Target Space
     *                            (X=Right, Y=Down, Z=Out of target face)
     * @return Pose3d in WPILib coordinate system (X=Forward, Y=Left, Z=Up)
     */
    public static Pose3d fromLimelightTargetSpace(Pose3d limelightTargetPose) {
        // Target space has same axis convention as camera space
        return fromLimelightCameraSpace(limelightTargetPose);
    }

    /**
     * Converts a Pose3d from WPILib coordinate system to Limelight Target Space.
     *
     * @param wpiPose Pose3d in WPILib coordinate system
     * @return Pose3d in Limelight Target Space (X=Right, Y=Down, Z=Out of target)
     */
    public static Pose3d toLimelightTargetSpace(Pose3d wpiPose) {
        return toLimelightCameraSpace(wpiPose);
    }
}