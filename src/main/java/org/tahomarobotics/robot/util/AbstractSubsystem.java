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

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractSubsystem extends SubsystemBase {

  private static final List<AbstractSubsystem> allSubsystems = new ArrayList<>();

  public static List<AbstractSubsystem> getAllSubsystems() {
    return Collections.unmodifiableList(allSubsystems);
  }

  private Command savedDefaultCommand = null;

  @SuppressWarnings("this-escape")
  public AbstractSubsystem() {
    allSubsystems.add(this);
  }

  public void restoreDefaultCommand() {
    removeDefaultCommand();
    if (savedDefaultCommand != null) {
      setDefaultCommand(savedDefaultCommand);
    }
  }

  public void clearDefaultCommand() {
    savedDefaultCommand = getDefaultCommand();
    removeDefaultCommand();
  }

  public final void periodic() {
    subsystemPeriodic();
  }

  public final void simulationPeriodic() {}

  public abstract void subsystemPeriodic();
}
