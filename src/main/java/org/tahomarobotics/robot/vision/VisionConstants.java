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

import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.math.Vector;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.math.numbers.N3;
import edu.wpi.first.units.Units;

public class VisionConstants {
    public record CameraConfiguration(String name, Transform3d offset){
        public double[] getTransformArray() {
            return new double[] {
                    offset.getTranslation().getMeasureX().in(Units.Meters),
                    offset.getTranslation().getMeasureY().in(Units.Meters),
                    offset.getTranslation().getMeasureZ().in(Units.Meters),
                    offset.getRotation().getMeasureX().in(Units.Degree),
                    offset.getRotation().getMeasureY().in(Units.Degree),
                    offset.getRotation().getMeasureZ().in(Units.Degree)
            };
        }
    }

    public static final Vector<N3> MEGATAG1_STANDARD_DEVIATIONS = VecBuilder.fill(99999, 99999, 0);
    public static final Vector<N3> MEGATAG2_STANDARD_DEVIATIONS = VecBuilder.fill(0.15,0.15, 0);

    public static enum IMUMode {
        EXTERNAL_ONLY(0),
        EXTERNAL_SEED(1),
        INTERNAL_ONLY(2),
        INTERNAL_MT1_ASSIST(3),
        INTERNAL_EXTERNAL_ASSIST(4);

        final int mode;

        IMUMode(int mode) {
            this.mode = mode;
        }
    }
}
