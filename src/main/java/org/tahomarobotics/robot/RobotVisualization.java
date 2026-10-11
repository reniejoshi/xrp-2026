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

import static edu.wpi.first.units.Units.Amps;

import org.littletonrobotics.junction.Logger;
import org.tahomarobotics.robot.util.AbstractSubsystem;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.units.measure.Current;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;

public class RobotVisualization extends AbstractSubsystem{

    private static final Pose3d ZEROED_POSE = new Pose3d(0, 0, 0, new Rotation3d(0, 0, 0));
    private static final Pose3d VISABLE_BUMPER_POSE = new Pose3d(0, 0, 0, new Rotation3d(0, 0, 0));
    private static final Pose3d HIDDEN_BUMPER_POSE = new Pose3d(0, 0, -100, new Rotation3d(0, 0, 0));

    public RobotVisualization() {}

    @Override
    public void subsystemPeriodic() {

        Logger.recordOutput("RobotVisualization/Zeroed/Robot", ZEROED_POSE);

        Pose3d blueBumper = DriverStation.getAlliance().orElse(Alliance.Blue) == Alliance.Blue ? VISABLE_BUMPER_POSE: HIDDEN_BUMPER_POSE;
        Pose3d redBumper = DriverStation.getAlliance().orElse(Alliance.Blue) == Alliance.Red ? VISABLE_BUMPER_POSE: HIDDEN_BUMPER_POSE;

        // TODO: add mechanism visualization when new subsystems are implemented

        Pose3d[] componentPoses = new Pose3d[] {
            blueBumper, 
            redBumper,
        };

        Logger.recordOutput("RobotVisualization/Zeroed/Components", componentPoses);
    }
    
    @Override
    public Current getTotalCurrent() {
        return Amps.of(0);
    }
}
