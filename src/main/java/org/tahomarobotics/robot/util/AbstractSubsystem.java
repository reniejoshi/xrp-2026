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

import static edu.wpi.first.units.Units.Amps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.ctre.phoenix6.hardware.TalonFX;

import edu.wpi.first.units.measure.Current;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.littletonrobotics.junction.Logger;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine;

public abstract class AbstractSubsystem extends SubsystemBase {

    private static final List<AbstractSubsystem> allSubsystems = new ArrayList<>();
    private static final Map<String, SysIdRoutine> sysIdRoutines = new HashMap<>();
    private static final Map<String, SystemCheckRoutine> systemCheckRoutines = new HashMap<>();

    public static List<AbstractSubsystem> getAllSubsystems() {
        return Collections.unmodifiableList(allSubsystems);
    }

    public static Map<String, SysIdRoutine> getSysIdRoutines() {
        return Collections.unmodifiableMap(sysIdRoutines);
    }

    public static Map<String, SystemCheckRoutine> getSystemCheckRoutines() {
        return Collections.unmodifiableMap(systemCheckRoutines);
    }

    private final String name;

    private Command savedDefaultCommand = null;

    @SuppressWarnings("this-escape")
    public AbstractSubsystem() {
        name = this.getClass().getSimpleName();
        allSubsystems.add(this);
        PerformanceMonitor.registerTask("Subsystem/" + name, PerformanceMonitor.PERIOD_SLOW_MS);
    }

    public void registerSystemCheckRoutine(String routineName, Supplier<Command> command, SystemCheckMotor... motors) {
        systemCheckRoutines.put(routineName, new SystemCheckRoutine(routineName, command, motors));
    }

    public void registerSysIdRoutine(String routineName, SysIdRoutine routine) {
       sysIdRoutines.put(routineName, routine);
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
        PerformanceMonitor.logExecutionTime("Subsystem/" + name, this::subsystemPeriodic);
    }

    public final void simulationPeriodic() {}

    public abstract void subsystemPeriodic();

    public static class SystemCheckMotor {
        String name;
        TalonFX motor;
        Current minNominal;
        Current maxNominal;

        public SystemCheckMotor(String name, TalonFX motor, Current minNominal, Current maxNominal) {
            this.name = name;
            this.motor = motor;
            this.minNominal = minNominal;
            this.maxNominal = maxNominal;
        }

        private Current total = Amps.of(0);
        private int count = 0;

        Current getSupplyCurrent() {
            return motor.getSupplyCurrent().getValue();
        }

        void measureCurrent() {
            total = total.plus(getSupplyCurrent());
            count++;
        }

        void clearAverage() {
            total = Amps.of(0);
            count = 0;
        }

        Current getAverageCurrent() {
            Current avg = count > 0 ? total.div(count) : Amps.of(0);
            Logger.recordOutput("SystemChecks/" + name, roundToTenThou(avg.in(Amps)));
            Logger.recordOutput("SystemChecks/" + name + "Min Nominal", roundToTenThou(minNominal.in(Amps)));
            Logger.recordOutput("SystemChecks/" + name + "Max Nominal", roundToTenThou(maxNominal.in(Amps)));
            return avg;
        }

        boolean getIsNominal() {
            Current avg = getAverageCurrent();
            return avg.gt(minNominal) && avg.lt(maxNominal);
        }

        boolean notFaulted() {
            boolean healthy = true;

            if (motor.getFault_Hardware().getValue()) {
                org.tinylog.Logger.error("{}: Hardware fault detected", name);
                healthy = false;
            }
            if (motor.getFault_DeviceTemp().getValue()) {
                org.tinylog.Logger.error("{}: Device temperature fault detected", name);
                healthy = false;
            }
            if (motor.getFault_ProcTemp().getValue()) {
                org.tinylog.Logger.error("{}: Processor temperature fault detected", name);
                healthy = false;
            }
            if (motor.getFault_UnlicensedFeatureInUse().getValue()) {
                org.tinylog.Logger.error("{}: Unlicensed feature in use", name);
                healthy = false;
            }
            if (motor.getFault_RemoteSensorDataInvalid().getValue()) {
                org.tinylog.Logger.error("{}: Remote sensor data invalid", name);
                healthy = false;
            }
            if (motor.getFault_BootDuringEnable().getValue()) {
                org.tinylog.Logger.error("{}: Device booted while enabled", name);
                healthy = false;
            }
            if (motor.getFault_OverSupplyV().getValue()) {
                org.tinylog.Logger.error("{}: Over supply voltage fault detected", name);
                healthy = false;
            }
            if (motor.getFault_RemoteSensorPosOverflow().getValue()) {
                org.tinylog.Logger.error("{}: Remote sensor position overflow", name);
                healthy = false;
            }

            return healthy;
        }
    }

    public static class SystemCheckRoutine {
        String name;
        Supplier<Command> command;
        SystemCheckMotor[] motors;

        SystemCheckRoutine(String name, Supplier<Command> command, SystemCheckMotor... motors) {
            this.name = name;
            this.command = command;
            this.motors = motors;
        }

        private void measureCurrents() {
            for (SystemCheckMotor motor : motors) {
                motor.measureCurrent();
            }
        }

        private void clearAverages() {
            for (SystemCheckMotor motor : motors) {
                motor.clearAverage();
            }
        }

        public Command getCommand() {
            Command measureCommand = Commands.runOnce(this::clearAverages)
                .andThen(Commands.run(this::measureCurrents));

            return command.get().raceWith(measureCommand)
                .andThen(Commands.runOnce(() -> {
                    boolean motorGood = true;
                    for (SystemCheckMotor motor : motors) {
                        motorGood = true;
                        if (!motor.getIsNominal()) {
                            org.tinylog.Logger.info(motor.name + " average current: " + roundToTenThou(motor.getAverageCurrent().in(Amps)) + "A");
                            org.tinylog.Logger.error(motor.name + " is out of nominal range! Nominal is " + roundToTenThou(motor.minNominal.in(Amps)) + "A to " + roundToTenThou(motor.maxNominal.in(Amps)) + "A");
                            motorGood = false;
                        } else {
                            org.tinylog.Logger.info(motor.name + " average current: " + roundToTenThou(motor.getAverageCurrent().in(Amps)) + "A");
                        }
                        if (!motor.notFaulted()) {
                            org.tinylog.Logger.error(motor.name + " has a fault! Check console above for details.");
                            motorGood = false;
                        }
                        SmartDashboard.putBoolean(motor.name, motorGood);
                    }
            })).withName(name);
        }
    }

    public static double roundToTenThou(double value) {
       double rounded = Math.round(value * 10000.0) / 10000.0;
       return rounded;
    }

    public abstract Current getTotalCurrent();
    
    public static void logCurrent() {
        Current totalCurrent = Amps.of(0);
        for (AbstractSubsystem subsystem : allSubsystems) {
            Current subsystemCurrent = subsystem.getTotalCurrent();
            Logger.recordOutput("Total Currents/" + subsystem.getClass().getSimpleName(), subsystemCurrent.in(Amps));
            totalCurrent = totalCurrent.plus(subsystemCurrent);
        }
        Logger.recordOutput("Total Currents/Total Current", totalCurrent.in(Amps));
    }
}