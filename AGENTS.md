# AGENTS.md

This file provides guidance to AI Coding Agents when working with code in this repository.

## FRC Mentoring System Guidelines

### Mandatory Startup Sequence
1. ALWAYS load user profile using `mcp__frc-mentoring__get-user-profile` at conversation start
2. ALWAYS read full mentoring context using `mcp__frc-mentoring__get-frc-mentoring-context` and follow mentoring context
3. ALWAYS ping session start with `mcp__frc-mentoring__ping-activity` (activityType: "session_start")
4. Greet user by their given name and adapt teaching style to their skill level and learning preference

## Project Overview

This is a FRC (FIRST Robotics Competition) 2025 robot codebase for team "Bear Metal" implementing a prototyping robot for the off-season Prototyping Challenge. The robot is built using WPILib's Command-based framework with Java and includes comprehensive simulation capabilities.

## Key Commands

### Building and Testing
- `./gradlew build` - Build the entire project (compile, test, jar)
- `./gradlew test` - Run all JUnit 5 tests
- `./gradlew jacocoTestReport` - Generate test coverage reports
- `./gradlew clean` - Clean build artifacts

### Robot Deployment
- `./gradlew deploy` - Deploy robot code to RoboRIO
- `./gradlew deployroborio` - Deploy specifically to RoboRIO target

### Simulation
- `./gradlew simulateJava` - Start robot simulation with GUI
- Set Robot State to "Teleoperated" in the simulation window
- Connect Xbox controller or use keyboard mapping for control

### Development Tools
- `./gradlew Glass` - Run WPILib Glass tool for debugging
- `./gradlew SmartDashboard` - Run SmartDashboard
- `./gradlew SysId` - Run system identification tool

## Architecture

### Core Structure
- **Robot.java** - Main robot class extending LoggedRobot (AdvantageKit)
- **RobotContainer.java** - Dependency injection container for all subsystems
- **OI.java** - Operator Interface where all teleop controller bindings are configured
- **Autonomous.java** - Autonomous mode routines and path following
- **Subsystem Pattern** - Each major robot mechanism is a separate subsystem:
  - `chassis/` - Drivetrain with swerve drive
  - `vision/` - Vision system

### Key Patterns
- **Command-based Framework** - Uses WPILib's command system for robot actions
- **Simulation Architecture** - Each subsystem has a corresponding simulation class
- **AdvantageKit Integration** - Comprehensive logging and replay capabilities
- **Dependency Injection** - RobotContainer manages all subsystem dependencies

### Important Packages
- `org.tahomarobotics.auto` - Autonomous routines and constants
- `org.tahomarobotics.sim` - Simulation classes including Arena2025Reefscape
- `org.tahomarobotics.util` - Utilities like CommandLogger, WatchDog, AbstractSubsystem

## Development Workflow

### Simulation Setup
1. Run `./gradlew simulateJava` 
2. Change Robot State to "Teleoperated" in simulation window
3. Drag Xbox Controller or Keyboard 0 to Joystick[0] slot
4. For AdvantageScope: Connect to simulator (Ctrl+Shift+K), use team address "10.20.46.2"

### Testing
- Tests are located in `src/test/java/org/tahomarobotics/`
- Uses JUnit 5 with parameterized tests
- Test coverage reports generated with JaCoCo
- ByteBuddy used for mocking in tests

### Code Quality
- Uses tinylog for logging (not System.out.println)
- Includes lint warnings for unchecked and deprecation
- Project configured for Java 17

## API Documentation

Comprehensive API documentation is available in the `documents/` folder:

### Core APIs
- **PHOENIX6.md** - CTRE motor controllers, sensors, and Motion Magic
- **ADVANTAGEKIT.md** - Logging framework with IO layer patterns
- **PATHPLANNER.md** - Path planning and autonomous navigation
- **PHOTONVISION.md** - Computer vision and AprilTag pose estimation
- **WPILIB.md** - Core FRC framework classes and utilities

### Additional APIs
- **TINYLOG.md** - Lightweight logging configuration and usage
- **MAPLESIM.md** - 2D physics simulation framework
- **DYN4J.md** - Underlying 2D physics engine
- **SYSID.md** - System identification for motor characterization
- **LIMELIGHT.md** - Vision system integration (if using Limelight)

**Always reference these documentation files when working with the respective APIs for best practices, configuration examples, and troubleshooting guidance.**



## Important Notes

- **Team Number**: Configured via .wpilib/wpilib_preferences.json
- **Simulation Data**: CTRE sim data stored in `ctre_sim/` directory  
- **PathPlanner Integration**: Auto paths stored in `src/main/deploy/pathplanner/`
- **Starting Position**: Robot simulation starts at (3,3) coordinates
- **Thread Priority**: Robot uses high-priority threading for better loop timing