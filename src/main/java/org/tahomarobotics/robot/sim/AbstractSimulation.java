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

import org.tahomarobotics.robot.util.PerformanceMonitor;

public abstract class AbstractSimulation {

    public enum Rate {
        FAST, SLOW
    }

    private final int subFrame;
    public final String name;
    private final boolean highRate;

    public AbstractSimulation(String name) {
        this(name, Rate.SLOW, 0);
    }

     public AbstractSimulation(String name, int subFrame) {
        this(name, Rate.SLOW, subFrame);
    }

    public AbstractSimulation (String name, Rate rate) {
        this(name, rate, 0);
    }
    
    private AbstractSimulation (String name, Rate rate, int subFrame) {
        this.name = name;
        this.highRate = rate == Rate.FAST;
        this.subFrame = subFrame;
        PerformanceMonitor.registerTask("Simulation/" + name,
                highRate ? PerformanceMonitor.PERIOD_FAST_MS : PerformanceMonitor.PERIOD_SLOW_MS);
    }

    public final void periodic(int subFrame) {

        if (highRate || subFrame == this.subFrame) {
           PerformanceMonitor.logExecutionTime("Simulation/" + name, () -> simulationPeriodic(subFrame));
        }
    }

    /**
     * Called periodically during simulation at 500Hz divided into 10 sub-frames.
     * If 50Hz is needed, just use every 10th sub-frame at frame 0.
     * @param subFrame
     */
    protected void simulationPeriodic(int subFrame) {}

}