# AdvantageScope Custom Assets

Custom robot models and assets for AdvantageScope 3D visualization.

## Contents

- **Robot_BetaBot/** - BetaBot robot model with articulated components (bumpers, collector, hopper, shooter hood)
- **Robot_TinySphere/** - Simple sphere model for basic testing

## Installing Assets

Team members can download the latest assets from GitHub:

```bash
./gradlew installAdvantagescopeAssets
```

This downloads and extracts assets to `advantage-scope-assets/`.

**Note:** Requires [GitHub CLI](https://cli.github.com/) (`gh`) to be installed and authenticated.

## Creating a New Release

When you've updated the assets and want to share them:

1. **Bundle the assets:**
   ```bash
   ./gradlew bundleAssets
   ```
   Creates `build/assets/advantagescope-assets.zip`

2. **Create a GitHub release:**
   ```bash
   gh release create assets-v1.x.x build/assets/advantagescope-assets.zip \
       --title "AdvantageScope Assets v1.x.x" \
       --notes "Description of changes"
   ```

   Or upload manually at: https://github.com/bear-metal-2046/robot-2026/releases

## Using in AdvantageScope

To use these assets in AdvantageScope:

1. Open AdvantageScope
2. Go to **Help > Show Assets Folder**
3. Copy the robot folders (e.g., `Robot_BetaBot/`) into the assets folder
4. Restart AdvantageScope
5. Select the robot in the 3D field view settings

## Creating Custom Assets

For complete documentation, see the [AdvantageScope Custom Assets Guide](https://docs.advantagescope.org/more-features/custom-assets).

### 1. Export Models from Autodesk Inventor

1. Open the assembly in Autodesk Inventor
2. Export each component as a separate `.glb` file:
   - **File > Export > CAD Format**
   - Select **glTF Binary (*.glb)** format
3. Name files according to their purpose:
   - `model.glb` - Main robot body
   - `model_0.glb`, `model_1.glb`, etc. - Articulated components (numbered to match config order)

**Tips:**

- Simplify models to reduce file size (remove internal components, fasteners, etc.)
- Ensure the origin point is at the pivot/rotation point for articulated components
- Export at appropriate scale (AdvantageScope expects meters)

### 2. Configure config.json

The `config.json` file defines the robot and its articulated components:

```json
{
  "name": "BetaBot",
  "rotations": [],
  "position": [0.0, 0.0, 0.04445],
  "cameras": [],
  "components": [
    {
      "name": "BlueBumper",
      "zeroedRotations": [
        {"axis": "x", "degrees": 90},
        {"axis": "y", "degrees": 0},
        {"axis": "z", "degrees": 90}
      ],
      "zeroedPosition": [-0.92, 0.17, -0.15]
    }
  ]
}
```

**Key fields:**

- `position` - Robot origin offset from ground (meters)
- `zeroedRotations` - Initial rotation to align the model (Inventor and AdvantageScope use different coordinate systems)
- `zeroedPosition` - Position offset to place the component correctly when at zero pose

**Tip:** Adjusting `zeroedPosition` values is often trial-and-error. Small changes (0.01-0.1m) can help align components.

### 3. Create RobotVisualization.java

The `RobotVisualization` class logs component poses to AdvantageKit for AdvantageScope to render.

See [RobotVisualization.java](src/main/java/org/tahomarobotics/robot/RobotVisualization.java) for the implementation.

**Key points:**

- Component poses are logged as a `Pose3d[]` array
- **The array order must match the component order in config.json**
- Use `Logger.recordOutput("RobotVisualization/Zeroed/Comp", componentPoses)` to log
- Calculate poses based on mechanism state (angles, extensions, etc.)

Example for an articulated collector:

```java
double collectorAngle = pivotAngleSupplier.get().in(Radians);
Pose3d collector = new Pose3d(0.265, 0, 0.16, new Rotation3d(0, -collectorAngle, 0));
```

### Asset Folder Structure

```text
Robot_BetaBot/
├── config.json      # Configuration file
├── model.glb        # Main robot body
├── model_0.glb      # Component 0 (BlueBumper)
├── model_1.glb      # Component 1 (RedBumper)
├── model_2.glb      # Component 2 (Collector)
├── model_3.glb      # Component 3 (Hopper)
└── model_4.glb      # Component 4 (ShooterHood)
```
