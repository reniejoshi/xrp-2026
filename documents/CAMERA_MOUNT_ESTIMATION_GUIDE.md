# Camera Mount Estimation Guide

This guide walks you through the process of calibrating the position of a Limelight camera on the robot. The goal is to tell the robot software **exactly where the camera is mounted** so that vision-based position estimates are accurate.

> **Who is this for?** Anyone on the team — no programming experience required. You just need access to the robot, a tape measure, and a computer connected to the robot.

---

## Overview

When a camera is mounted on the robot, the software needs to know exactly where it sits (how far forward, left/right, how high, and what angle). Instead of measuring the camera mount with calipers, this tool lets us **estimate** the camera's position by:

1. Placing the robot at a **known, measured position** in front of an AprilTag.
2. Letting the camera see the tag and do the math for us.
3. Applying and saving the result.

---

## What You Need

| Item | Purpose |
|---|---|
| The robot, powered on | Runs the estimation code |
| A visible AprilTag on the field | The reference point the camera will look at |
| A tape measure (metric, in meters) | Measuring the robot's position relative to the tag |
| A computer connected to the robot | To open SmartDashboard / Shuffleboard and press buttons |
| A protractor or known angle reference | To determine the robot's heading relative to the tag (optional — usually 0°) |

---

## Step-by-Step Instructions

### Step 1 — Position the Robot

Place the robot in front of an AprilTag in a **known, repeatable position**. You will need to measure and record **four values**:

| Value | What to Measure | Unit |
|---|---|---|
| **Distance out from the tag** | How far the **center of the robot** is directly in front of the tag face. Measure straight out, perpendicular to the tag surface. | Meters |
| **Distance left of the tag** | How far the **center of the robot** is to the left of the tag (when looking at the front of the tag). If the robot is to the right, use a negative number. | Meters |
| **Tag height from the ground** | The height of the **center of the AprilTag** above the floor. You can find this in the official field drawings or measure it yourself. | Meters |
| **Heading compared to the tag** | The angle of the robot relative to the tag. **On our robot the collector is the front.** The camera is typically on the back, so when the camera is facing the tag, the front (collector) points away — that means the heading is **0°**. If the collector side faces the tag instead, the heading is **180°**. The angle increases when the robot rotates counter-clockwise. | Degrees |

> **Tip — Simplest setup:** Place the robot so the **camera is facing the tag** (collector pointing away), centered directly in front of it. Then your "left of tag" is `0`, and your heading is `0`.

#### Diagram (top-down view, looking down from above)

```
  ════════════════ WALL ════════════════

      ┌──────────────────┐
      │                  │
      │    APRILTAG      │  The tag is flat on the wall.
      │                  │  Its face points downward in
      └──────────┬───────┘  this diagram (away from wall,
                 │          toward the robot).
                 │
                 │ ← "Distance out from tag"
                 │    (measure this gap, in meters)
                 │
                 │
          ┌──────┴──────┐
          │ ▼ ▼ ▼ ▼ ▼ ▼ │  ← Back of robot (CAMERA side)
          │             │     (this side faces the tag)
          │    ROBOT    │
          │             │
          │ ▲ ▲ ▲ ▲ ▲ ▲ │  ← Front of robot (COLLECTOR side)
          └─────────────┘     (this side faces away from tag)

     ← Left of tag (+)    Right of tag (−) →
        (when you stand         
         behind the tag         
         and look outward)      

  On our robot the collector is the "front."
  In this position the camera (back) faces the tag and the
  collector (front) faces away, so "Heading compared to
  Tag" = 0°.

  If you flip the robot so the collector faces the tag
  instead, the heading would be 180°.

  Rotation direction for heading:

         Counter-clockwise (+)
                ↺
          ┌───────────┐
          │   ROBOT   │
          └───────────┘
                ↻
          Clockwise (−)

  If you rotate the robot counter-clockwise (as seen from
  above), the heading number goes UP from 0°.
  If you rotate it clockwise, the number goes DOWN.
```

---

### Step 2 — Connect to SmartDashboard

1. Open **SmartDashboard** or **Shuffleboard** on your driver station laptop.
2. Make sure you can see the following items (they appear automatically when the robot code starts):
   - `Actual Chassis Pose Out from Tag (Meters)`
   - `Actual Chassis Pose Left of Tag (Meters)`
   - `Tag Height from Ground (Meters)`
   - `Actual Chassis Pose Heading compared to Tag (Degrees)`
   - Three buttons: **Estimate Camera Positions**, **Update Camera Positions**, **Save Camera Positions**

> If you don't see these, make sure the robot code is running and the dashboard is connected to the robot.

---

### Step 3 — Enter Your Measurements

Type your four measured values into the corresponding SmartDashboard fields:

| SmartDashboard Field | Example Value |
|---|---|
| `Actual Chassis Pose Out from Tag (Meters)` | `1.0` |
| `Actual Chassis Pose Left of Tag (Meters)` | `0` |
| `Tag Height from Ground (Meters)` | `1.22` |
| `Actual Chassis Pose Heading compared to Tag (Degrees)` | `0` |

> **Double-check your values before continuing!** Incorrect measurements will produce incorrect calibration.

---

### Step 4 — Make Sure the Camera Can See the Tag

Before estimating, **confirm the camera has a clear view of the AprilTag**.

- Check the Limelight web interface (usually at `http://limelight.local:5801`) to verify the tag is being detected.
- The tag should be clearly visible and not obstructed.

---

### Step 5 — Estimate Camera Positions

1. Click the **"Estimate Camera Positions"** button on SmartDashboard.
2. The software will capture the **next** AprilTag reading from each camera and calculate where the camera must be mounted on the robot.
3. This happens automatically in the background — you just need to wait a moment (less than a second).

> **You do NOT need to repeat the full procedure separately for each camera.** When you click Estimate, every camera that can see an AprilTag at that moment is calibrated in one shot. However, if a camera does *not* see a tag at that moment, it will be skipped (an error will appear in the logs). If that happens, reposition the robot so the missed camera can see a tag, re-enter your measurements, and click **Estimate** again — cameras that were already successfully estimated will simply be re-estimated with the new data, which is fine.

---

### Step 6 — Apply the Estimated Offsets

1. Click the **"Update Camera Positions"** button on SmartDashboard.
2. This tells each Limelight to start using the newly estimated camera position for all future vision pose estimates.
3. The robot is now using the calibrated camera position **in memory only** (it will be lost on reboot).

> **Before saving, verify the calibration!** See the [Verifying the Calibration](#verifying-the-calibration) section below.

---

## Verifying the Calibration

After clicking **Update Camera Positions** (Step 6), you should verify the calibration is correct before saving. There are two ways to check.

### Method A — Limelight Web Dashboard

1. Open the Limelight web interface in a browser (usually `http://limelight.local:5801`).
2. Go to the **3D** tab — you should see the camera's estimated position overlaid on the field.
3. Place the robot at a **known location** on the field (e.g., lined up against a field element with a known position).
4. Check that the position shown on the Limelight's 3D view **matches where the robot actually is**. If the position is offset or rotated, the calibration is likely wrong.

### Method B — AdvantageScope (Recommended)

AdvantageScope lets you compare the vision-estimated position against the integrated robot pose in real time.

1. **Set up the robot's address** in AdvantageScope (only needed once):
   - Open the **Preferences** window.
   - Enter the robot's address (e.g., `10.20.46.2`, or `localhost` for simulation).

2. **Connect** to the robot by pressing **Ctrl+K** in AdvantageScope.

3. **Open the 2D field view** (or 3D if you prefer) and add these two poses:
   - **Integrated pose**: drag `Drive/OdometryPose` onto the field. This is the robot's best-guess position that fuses wheel encoders, gyro, and vision together.
   - **Vision-only pose**: drag `Vision/limelight/Position` onto the field. This is the raw position reported by the camera alone.
   
   *(Replace `limelight` with your camera's name if it differs.)*

4. **Drive the robot around** while it can see AprilTags. Watch both poses on the field view:
   - The **integrated pose** combines all sensor data and should be smooth and stable.
   - The **vision pose** comes directly from the camera's AprilTag reading. If the camera offset is correct, it should closely match the integrated pose.

5. **What to look for:**

   | Result | What It Means |
   |---|---|
   | Both poses are close together and track similarly | ✅ Calibration is good |
   | Vision pose is consistently offset in one direction from the integrated pose | ❌ The camera offset is wrong — re-do the estimation with more accurate measurements |
   | Vision pose jumps around wildly | ❌ The camera may not have a clear view of tags, or the calibration is very far off |
   | Vision pose is rotated compared to the integrated pose | ❌ The heading value entered during estimation was likely wrong |

6. If everything looks good, go back to SmartDashboard and click **Save Camera Positions** (Step 7).

> **Tip:** Drive the robot to several different spots on the field and check the vision pose at each one. A good calibration will be accurate everywhere, not just near the tag you used for estimation.

---

### Step 7 — Save the Calibration

Once you are satisfied with the results:

1. Click the **"Save Camera Positions"** button on SmartDashboard.
2. This saves the calibrated camera offsets to the RoboRIO's persistent storage (Preferences).
3. **The calibration will now survive reboots** — you do not need to repeat this process unless the camera is physically moved.

---

## Summary — Button Order

| # | Button | What It Does |
|---|---|---|
| 1 | **Estimate Camera Positions** | Calculates where the camera is based on your measurements and the seen tag |
| 2 | **Update Camera Positions** | Applies the calculated offsets to the active camera configuration |
| 3 | **Save Camera Positions** | Permanently saves the offsets so they persist across reboots |

---

## Troubleshooting

| Problem | Solution |
|---|---|
| Dashboard fields don't appear | Make sure robot code is running and the dashboard is connected |
| "CAMERA MOUNT ESTIMATION DID NOT FIND A TARGET" error in logs | The camera couldn't see any AprilTag when estimation ran. Re-position the robot or check camera view, then click **Estimate** again |
| Vision position looks wrong after applying | Double-check your measurements. Common mistakes: wrong units (make sure you're using **meters** and **degrees**), or the heading is wrong — remember the collector is the front, so if the camera (back) faces the tag the heading should be **0°**, not 180° |
| Calibration lost after reboot | You forgot to click **Save Camera Positions**. Re-do the estimation or just click Save if the offsets are still applied |
| Camera was physically moved | You need to redo the entire procedure from Step 1 |

---

## Frequently Asked Questions

**Q: How accurate does my measurement need to be?**
A: As accurate as possible — even a centimeter or two of error in your measurement will directly affect the calibration. Use a tape measure, not eyeballing.

**Q: Can I do this with the robot disabled?**
A: Yes! All three buttons work even when the robot is disabled.

**Q: Do I need to repeat this process for each camera separately?**
A: No — you only need to do it once. When you click **Estimate Camera Positions**, every camera that can currently see an AprilTag is calibrated at the same time. You only need to repeat the process if one of the cameras *didn't* see a tag (check the logs for an error message). In that case, reposition the robot so the missed camera has a clear view of a tag, re-enter your measurements, and click **Estimate** again. The other cameras will simply be re-estimated alongside it.

**Q: What if the heading isn't exactly 0°?**
A: The heading value represents the rotation of the robot relative to the tag. 0° means the robot's front (collector) is pointing the same direction as the tag face (away from the wall) — this is the most common setup since the camera is on the back. 180° means the collector is pointed directly at the tag. If the robot is angled, measure the angle and enter it. Counter-clockwise rotation (as seen from above) increases the number.

**Q: Where are the saved offsets stored?**
A: They are stored in the RoboRIO's Preferences system (persistent key-value storage on the robot controller). They will survive code re-deploys and reboots.
