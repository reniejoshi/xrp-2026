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

import edu.wpi.first.net.WebServer;
import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import org.littletonrobotics.junction.LoggedRobot;
import org.littletonrobotics.junction.Logger;
import org.littletonrobotics.junction.networktables.NT4Publisher;
import org.littletonrobotics.junction.wpilog.WPILOGWriter;
import org.tahomarobotics.robot.util.CommandLogger;
import org.tahomarobotics.robot.util.Identity;
import org.tahomarobotics.robot.util.WatchDog;

public class Robot extends LoggedRobot {

  private final RobotContainer robotContainer;

  private static final int SUB_FRAMES_PER_FRAME = 10;
  private int subFrame = SUB_FRAMES_PER_FRAME - 1;

  Robot() {
    super(Robot.isSimulation() ? 0.002 : 0.02);

    Logger.addDataReceiver(new NT4Publisher());

    this.robotContainer = new RobotContainer();

    if (Robot.isSimulation()) {
      WatchDog.disableWatchdog(this);
      WatchDog.disableWatchdog(CommandScheduler.getInstance());
    } else {
      WatchDog.disableWatchdog(this);
      WatchDog.disableWatchdog(CommandScheduler.getInstance());
      // initialize AdvantageScope publishing
      Logger.addDataReceiver(new WPILOGWriter("/home/lvuser/alogs"));
    }

    Logger.start();

    // logs the duration of commands that finish or get interrupted
    CommandLogger.install();
  }

  @Override
  public void robotInit() {
    // TODO: schedule pathplanner warmup

    WebServer.start(5800, Filesystem.getDeployDirectory().getAbsolutePath());
  }

  @Override
  public void robotPeriodic() {

    // downsample high-frequency simulation updates
    if (Robot.isSimulation()) {
      subFrame = ++subFrame % SUB_FRAMES_PER_FRAME;
      if (subFrame != 0) {
        return;
      }
    }
  }

  @Override
  public void autonomousInit() {
    // TODO: restore autonomous command selection when autonomous is implemented
  }

  @Override
  public void autonomousExit() {
    // TODO: cancel auto command when autonomous is implemented
  }

  @Override
  public void autonomousPeriodic() {
    // no need for this other than removing console printouts
  }

  @Override
  public void disabledInit() {
    robotContainer.oi.exitTestMode();
  }

  @Override
  public void teleopPeriodic() {
    // no need for this other than removing console printouts
  }

  @Override
  public void disabledPeriodic() {
    // no need for this other than removing console printouts
  }

  @Override
  public void simulationInit() {}

  @Override
  public void simulationPeriodic() {}

  @Override
  public void testInit() {
    robotContainer.oi.enterTestMode();
  }

  @Override
  public void close() {
    super.close();
    robotContainer.close();
  }

  public static void main(String... args) {
    // Determine which robot this code is running on before any configs are built.
    org.tinylog.Logger.info("Selected RobotIdentity: {}", Identity.getIdentity());

    // start this class or restart if something bad happened
    RobotBase.startRobot(Robot::new);
  }
}
