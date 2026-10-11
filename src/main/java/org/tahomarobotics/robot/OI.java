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

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import org.tahomarobotics.robot.util.AbstractSubsystem;

public class OI {
  private static final int DRIVER_CONTROLLER_INDEX = 0;
  private static final int MANIP_CONTROLLER_INDEX = 1;

  @SuppressWarnings("unused")
  private final CommandXboxController driverController =
      new CommandXboxController(DRIVER_CONTROLLER_INDEX);

  @SuppressWarnings("unused")
  private final CommandXboxController manipController =
      new CommandXboxController(MANIP_CONTROLLER_INDEX);

  private enum Bindings {
    TELEOP,
    TEST
  }

  @SuppressWarnings("unused")
  private final RobotContainer robotContainer;

  private Bindings currentBindings;

  public OI(RobotContainer robotContainer) {
    this.robotContainer = robotContainer;

    DriverStation.silenceJoystickConnectionWarning(Robot.isSimulation());

    bindTeleopControls();
  }

  private void bindTeleopControls() {
    currentBindings = Bindings.TELEOP;

    // -- Drive Controller Bindings --

    // -- Manip Controller Bindings --

    // -- Simulation Controller Bindings --
  }

  public void enterTestMode() {
    currentBindings = Bindings.TEST;

    // save and clear all default commands and button bindings
    AbstractSubsystem.getAllSubsystems().forEach(AbstractSubsystem::clearDefaultCommand);
    CommandScheduler.getInstance().getActiveButtonLoop().clear();
    CommandScheduler.getInstance().cancelAll();
  }

  public void exitTestMode() {
    if (currentBindings == Bindings.TELEOP) {
      return;
    }

    // restore saved default commands
    AbstractSubsystem.getAllSubsystems().forEach(AbstractSubsystem::restoreDefaultCommand);

    bindTeleopControls();
  }
}
