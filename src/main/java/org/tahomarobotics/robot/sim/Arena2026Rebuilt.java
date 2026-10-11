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
package org.tahomarobotics.robot.sim;

import org.dyn4j.dynamics.Body;
import org.dyn4j.world.World;
import org.ironmaple.simulation.SimulatedArena;
import org.ironmaple.simulation.drivesims.AbstractDriveTrainSimulation;

import edu.wpi.first.math.geometry.Pose2d;

/**
 * Extended the default Arena to include some needed functionality
 */
public class Arena2026Rebuilt extends org.ironmaple.simulation.seasonspecific.rebuilt2026.Arena2026Rebuilt {

    static {
        SimulatedArena.overrideInstance(new Arena2026Rebuilt());
    }
    private AbstractDriveTrainSimulation driveTrainSimulation = null;
    
    public Arena2026Rebuilt() {
        super(false);
    }
    
    public static Arena2026Rebuilt getInstance() {
        var arena = SimulatedArena.getInstance();
        assert(arena instanceof Arena2026Rebuilt);
        return (Arena2026Rebuilt) arena;
    }

    public void setDriveTrainSimulation(AbstractDriveTrainSimulation driveTrainSimulation) {
        super.addDriveTrainSimulation(driveTrainSimulation);
        this.driveTrainSimulation = driveTrainSimulation;
    }

    public Pose2d getRobotPose() {
        if (driveTrainSimulation != null) {
            return driveTrainSimulation.getSimulatedDriveTrainPose();
        } else {
            return new Pose2d();
        }
    }

    @Override
    public void placeGamePiecesOnField() {
        super.placeGamePiecesOnField();
    }

    public World<Body> getWorld() {
        return this.physicsWorld;
    }

    public AbstractDriveTrainSimulation getDriveTrainSimulation() {
        assert(driveTrainSimulation != null);
        return driveTrainSimulation;
    }
}
