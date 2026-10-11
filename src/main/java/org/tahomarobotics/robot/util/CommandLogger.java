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

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.*;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class CommandLogger  {

    public static final CommandLogger INSTANCE = new CommandLogger(CommandScheduler.getInstance());

    public static void install() {
        Logger.info("CommandLogger installed");
    }

    private record CommandInfo(Command cmd, double initialized) {}
    private final Map<Command, CommandInfo> commands = new HashMap<>();

    private CommandLogger(CommandScheduler scheduler) {
        scheduler.onCommandInitialize(this::onCommandInitialize);
        scheduler.onCommandFinish(this::onCommandFinished);
        scheduler.onCommandInterrupt(this::onCommandInterrupt);
    }

    private void onCommandInitialize(Command cmd) {
        CommandInfo info = commands.get(cmd);
        if (info != null) {
            Logger.warn("Already had command <" + info.cmd.getName() + "> in CommandLogger");
        }
        commands.put(cmd, new CommandInfo(cmd, Timer.getTimestamp()));
    }

    private void onCommandFinished(Command cmd) {
        CommandInfo info = commands.remove(cmd);
        if (info == null) {
            Logger.error("Command <" + cmd.getName() + "> not found in CommandLogger");
            return;
        }
        Logger.info(String.format("Command <%s> completed in %6.3f seconds", cmd.getName(), (Timer.getTimestamp() - info.initialized)));
    }

    private void onCommandInterrupt(Command cmd, Optional<Command> interruptedBy) {
        CommandInfo info = commands.remove(cmd);
        if (info == null) {
            Logger.error("Interrupted Command <" + cmd.getName() + "> not found in CommandLogger");
            return;
        }

        Logger.info(String.format("Command <%s> interrupted in %6.3f seconds %s", cmd.getName(),
                (Timer.getTimestamp() - info.initialized),
                interruptedBy.map(command -> "by <" + command.getName() + ">").orElse(""))
        );
    }

    private static final AtomicInteger indentLevel = new AtomicInteger(0);

    public static Command log(Command command) {
        return log(command, false);
    }

    public static Command log(Command command, boolean indent) {
        final Timer timer = new Timer();
        return new WrapperCommand(command){
            @Override
            public void initialize() {
                if (indent) indentLevel.incrementAndGet();
                timer.restart();
                super.initialize();
            }

            @Override
            public void end(boolean interrupted) {
                super.end(interrupted);
                if (indent) indentLevel.decrementAndGet();
                Logger.info(String.format("%s%s completed in %6.3f seconds (%s)", getIndent(), getName(), timer.get(), interrupted ? "interrupted" : ""));
            }

            private String getIndent() {
                return " ".repeat(indentLevel.get() * 4);
            }

            @Override
            public WrapperCommand withName(String name) {
                setName(name);
                return this;
            }
        };
    }

    public static Command sequence(Command ...commands) {
        return CommandLogger.log(Commands.sequence(commands), true);
    }

    public static Command parallel(Command ...commands) {
        return CommandLogger.log(Commands.parallel(commands), true);
    }

}