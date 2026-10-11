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

import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Locale;
import org.tahomarobotics.robot.Robot;
import org.tinylog.Logger;

public final class Identity {
  private static RobotIdentity identity;

  public enum RobotIdentity {
    // Put MACs here exactly as logged by this code ("AA:BB:CC:DD:EE:FF").
    XRP("28:cd:c1:15:8c:49"),
    SIMULATION("");

    final String[] macs;

    RobotIdentity(String... macs) {
      this.macs = macs;
    }

    boolean matches(String mac) {
      for (String expected : macs) {
        if (mac.equals(expected)) return true;
      }
      return false;
    }
  }

  private Identity() {}

  /** Detect identity by comparing NIC MACs against the enum's MAC strings. */
  public static RobotIdentity getIdentity() {
    if (identity != null) {
      return identity;
    }

    org.tinylog.Logger.info("Checking for robot identity...");
    try {
      Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
      Logger.info("MAC addresses:");
      while (e.hasMoreElements()) {
        byte[] mac = e.nextElement().getHardwareAddress();
        if (mac == null || mac.length == 0) continue;

        String macStr = formatMac(mac);
        Logger.debug("Detected MAC address: {}", macStr);
        for (RobotIdentity id : RobotIdentity.values()) {
          if (id.matches(macStr)) {
            identity = id;
            return id;
          }
        }
      }
    } catch (Exception ignored) {
      Logger.error("Couldn't find Robot Identity");
    }

    identity = Robot.isSimulation() ? RobotIdentity.SIMULATION : RobotIdentity.XRP;
    return identity;
  }

  private static String formatMac(byte[] mac) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < mac.length; i++) {
      sb.append(String.format(Locale.ROOT, "%s%02X", i == 0 ? "" : ":", mac[i]));
    }
    return sb.toString();
  }
}
