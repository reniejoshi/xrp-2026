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

import com.ctre.phoenix6.configs.Slot0Configs;
import com.ctre.phoenix6.configs.TalonFXConfigurator;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Commands;

public class SmartDashboardPID {

    public SmartDashboardPID(String name, TalonFXConfigurator configurator) {


        Slot0Configs pid = new Slot0Configs();
        
        configurator.refresh(pid);

        SmartDashboard.putNumber(name + " kP", pid.kP);
        SmartDashboard.putNumber(name + " kI", pid.kI);
        SmartDashboard.putNumber(name + " kD", pid.kD);
        SmartDashboard.putNumber(name + " kS", pid.kS);
        SmartDashboard.putNumber(name + " kV", pid.kV);
        SmartDashboard.putNumber(name + " kA", pid.kA);
        SmartDashboard.putNumber(name + " kG", pid.kG);
        
        SmartDashboard.putData("Apply " + name + "Gains", Commands.runOnce(() -> {
                
            Slot0Configs newGains = new Slot0Configs()
                .withKP(SmartDashboard.getNumber(name + " kP", 0))
                .withKI(SmartDashboard.getNumber(name + " kI", 0))
                .withKD(SmartDashboard.getNumber(name + " kD", 0))
                .withKS(SmartDashboard.getNumber(name + " kS", 0))
                .withKV(SmartDashboard.getNumber(name + " kV", 0))
                .withKA(SmartDashboard.getNumber(name + " kA", 0))
                .withKG(SmartDashboard.getNumber(name + " kG", 0));

                configurator.apply(newGains);
                org.tinylog.Logger.info("Applied " + name + " gains");

        }).ignoringDisable(true));
    }
}
