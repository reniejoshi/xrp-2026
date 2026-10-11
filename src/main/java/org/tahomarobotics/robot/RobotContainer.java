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

/**
 * The RobotContainer declares the robot's structure: subsystems, commands and simulations.
 * All the robot's components and their relationships are declared here rather than scattered
 * throughout the codebase.
 */
public final class RobotContainer implements AutoCloseable {

    // must be created first here to override the instance provided
    // by org.ironmaple.simulation.SimulatedArena.getInstance()
    // since it is used by some of MapleSim
    // TODO: create arena for MapleSim
    // private final Arena2026Rebuilt arena = Robot.isSimulation() ? Arena2026Rebuilt.getInstance() : null;

    final OI oi;
    final RobotVisualization visualization;

    public RobotContainer() {
        visualization = new RobotVisualization();
        
        oi = new OI(this);
    }

    @Override
    public void close() {

    }

}