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

import edu.wpi.first.math.MathSharedStore;
import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.math.trajectory.TrapezoidProfile.Constraints;
import edu.wpi.first.util.sendable.SendableBuilder;

public class FeedForwardProfiledPIDController extends ProfiledPIDController {
    private double kS;
    private double kV;

    private double lastFeedforward = 0;

    public FeedForwardProfiledPIDController(double kP, double kI, double kD, double kS, double kV, Constraints constraints) {
        super(kP, kI, kD, constraints);
        this.kS = kS;
        this.kV = kV;
    }

    @Override
    public double calculate(double setpoint, double measurement) {
        // Calculate the PID output so the profile is updated first
        double pidOutput = super.calculate(setpoint, measurement);

        // Calculate the feedforward based on the target velocity from the profile
        double targetVelocity = getSetpoint().velocity;
        double velSignum = Math.signum(targetVelocity);

        // In order to get target acceleration from the profile, we'd essentially have to re-implement the trapezoid profile
        // class, so the acceleration feedforward has been omitted for now. If we find that we need it, we can add it in later.
        double feedforward = 
            kS * velSignum // Multiply kS by the sign of the velocity to get the correct direction of the static feedforward
            + kV * targetVelocity; // Multiply kV by the target velocity to get the velocity feedforward

        lastFeedforward = feedforward;

        // Return feedback + feedforward
        return pidOutput + feedforward;
    }

    public double getFeedForwardComponent() {
        return lastFeedforward;
    }

    public double getS() {
        return kS;
    }

    public double getV() {
        return kV;
    }

    public void setS(double kS) {
        this.kS = kS;
    }

    public void setV(double kV) {
        this.kV = kV;
    }

    @Override
    public void initSendable(SendableBuilder builder) {
        builder.setSmartDashboardType("ProfiledPIDController");
        builder.addDoubleProperty("p", this::getP, this::setP);
        builder.addDoubleProperty("i", this::getI, this::setI);
        builder.addDoubleProperty("d", this::getD, this::setD);
        builder.addDoubleProperty("s", this::getS, this::setS);
        builder.addDoubleProperty("v", this::getV, this::setV);
        builder.addDoubleProperty(
            "izone",
            this::getIZone,
            (double toSet) -> {
            try {
                setIZone(toSet);
            } catch (IllegalArgumentException e) {
                MathSharedStore.reportError("IZone must be a non-negative number!", e.getStackTrace());
            }
            });
        builder.addDoubleProperty(
            "maxVelocity",
            () -> getConstraints().maxVelocity,
            maxVelocity ->
                setConstraints(
                    new TrapezoidProfile.Constraints(maxVelocity, getConstraints().maxAcceleration)));
        builder.addDoubleProperty(
            "maxAcceleration",
            () -> getConstraints().maxAcceleration,
            maxAcceleration ->
                setConstraints(
                    new TrapezoidProfile.Constraints(getConstraints().maxVelocity, maxAcceleration)));
        builder.addDoubleProperty("goal", () -> getGoal().position, this::setGoal);
    }
}
