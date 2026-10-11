# Robot Template 2027
This is a template to allow programming members to start new robot projects with the correct existing architecture.

## How to Use this Repository

To create a new robot code repository:

1. Click **Use this template** near the top of the GitHub page
2. Select bear-metal-2046 or your personal account as the owner
3. Enter a repository name
4. Click **Create repository from template**

After creating your repository:

1. Clone the repository:

   ```bash
   git clone <repository-url>
   ```
2. Open the project in 2026 WPILib VS Code
3. Review the project structure, remove any unused code, and update the README
4. Commit and push your changes

## Project Structure
```
.
├── .devcontainer/           # Development container configuration
├── .github/                 # CI workflows, issue templates, and PR templates
├── .idea                    # IntelliJ settings
├── .vscode/                 # VS Code settings
├── advantagescope_layouts/  # AdvantageScope layouts
├── config/                  # Static analysis and linting configs
├── documents/               # Team documentation
├── gradle/                  # Gradle wrapper files
├── limelight/               # Limelight configuration files
├── src/
│   ├── main/
│   │   ├── deploy/          # Files deployed to the roboRIO
│   │   │   └── pathplanner/ # PathPlanner configuration and path files
│   │   └── java/
│   │       └── org/
│   │           └── tahomarobotics/
│   │               └── robot/
│   │                   ├── sim/      # Simulation code
│   │                   ├── util/     # Shared utilities
│   │                   └── vision/   # Vision subsystem code
│   └── test/                # Unit tests
├── tag_maps/                # AprilTag field maps
└── vendordeps/              # Vendor libraries
```

## Essential GradleRIO Commands
| Command Palette | Gradle Command | Description |
| --- | --- | --- |
| WPILib: Build Robot Code | `./gradlew build` | Compiles robot code and runs all tests |
| WPILib: Simulate Robot Code | `./gradlew simulateJava` | Launches robot code in sim window |
| WPILib: Deploy Robot Code | `./gradlew deploy` | Builds and deploys robot code to the roboRIO |