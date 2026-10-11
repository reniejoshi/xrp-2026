# SysId (System Identification) Documentation

## Overview
SysId is WPILib's system identification tool for characterizing the dynamic behavior of robot mechanisms. It provides automated routines to determine the feedforward constants (kS, kV, kA) and other parameters needed for accurate motion control. SysId is essential for tuning drivetrain, arm, elevator, and other mechanism controllers.

## Version Information
- **Tool Version**: Included with WPILib 2025.3.2
- **Access Method**: Gradle task `./gradlew SysId`
- **Integration**: Works with Phoenix 6, WPILib, and other motor controllers
- **Data Format**: WPILOG files for analysis

## Core Concepts

### System Identification Theory
SysId characterizes mechanisms using the equation:
**V = kS × sgn(velocity) + kV × velocity + kA × acceleration**

Where:
- **kS** (Static gain): Voltage to overcome static friction
- **kV** (Velocity gain): Voltage per unit of steady-state velocity  
- **kA** (Acceleration gain): Voltage per unit of acceleration
- **V**: Applied voltage to mechanism

### Mechanism Types
SysId supports characterization of:
- **Drivetrain**: Differential drive systems
- **Arm**: Rotating arm mechanisms (shoulder joints)
- **Elevator**: Linear vertical mechanisms
- **Simple Motor**: Basic rotating mechanisms

## SysId Tool Interface

### Running SysId
```bash
# Launch SysId tool from project directory
./gradlew SysId
```

### Project Configuration
1. **Create New Project**: Select mechanism type and configure robot parameters
2. **Team Number**: Set your FRC team number for NetworkTables connection
3. **Mechanism Selection**: Choose drivetrain, arm, elevator, or simple motor
4. **Units**: Configure position and velocity units (meters, rotations, etc.)

### Test Configuration
```json
// SysId project settings example
{
  "mechanism": "Drivetrain",
  "units": {
    "position": "Meters",
    "velocity": "Meters per Second"
  },
  "wheelDiameter": 0.1524,
  "gearing": 8.45,
  "gyro": true
}
```

## Robot Code Integration

### Mechanism Interface Implementation
SysId requires implementing a standard interface for each mechanism type:

#### Drivetrain Interface
```java
import edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine;
import edu.wpi.first.units.*;

public class DriveSubsystem extends SubsystemBase {
    private final SysIdRoutine sysIdRoutine;
    
    public DriveSubsystem() {
        // Configure SysId routine
        sysIdRoutine = new SysIdRoutine(
            new SysIdRoutine.Config(),
            new SysIdRoutine.Mechanism(
                (Measure<Voltage> volts) -> {
                    leftMotor.setVoltage(volts.in(Volts));
                    rightMotor.setVoltage(volts.in(Volts));
                },
                log -> {
                    log.motor("drive-left")
                        .voltage(m_appliedVoltage.mut_replace(
                            leftMotor.get() * RobotController.getBatteryVoltage(), Volts))
                        .linearPosition(m_distance.mut_replace(leftEncoder.getDistance(), Meters))
                        .linearVelocity(m_velocity.mut_replace(leftEncoder.getRate(), MetersPerSecond));
                    
                    log.motor("drive-right")
                        .voltage(m_appliedVoltage.mut_replace(
                            rightMotor.get() * RobotController.getBatteryVoltage(), Volts))
                        .linearPosition(m_distance.mut_replace(rightEncoder.getDistance(), Meters))
                        .linearVelocity(m_velocity.mut_replace(rightEncoder.getRate(), MetersPerSecond));
                },
                this
            )
        );
    }
    
    // Expose SysId commands for autonomous
    public Command sysIdQuasistatic(SysIdRoutine.Direction direction) {
        return sysIdRoutine.quasistatic(direction);
    }
    
    public Command sysIdDynamic(SysIdRoutine.Direction direction) {
        return sysIdRoutine.dynamic(direction);
    }
}
```

#### Arm Mechanism Interface
```java
public class ArmSubsystem extends SubsystemBase {
    private final SysIdRoutine sysIdRoutine;
    
    public ArmSubsystem() {
        sysIdRoutine = new SysIdRoutine(
            new SysIdRoutine.Config(),
            new SysIdRoutine.Mechanism(
                (Measure<Voltage> volts) -> {
                    armMotor.setVoltage(volts.in(Volts));
                },
                log -> {
                    log.motor("arm")
                        .voltage(m_appliedVoltage.mut_replace(
                            armMotor.get() * RobotController.getBatteryVoltage(), Volts))
                        .angularPosition(m_angle.mut_replace(
                            armEncoder.getPosition(), Radians))
                        .angularVelocity(m_velocity.mut_replace(
                            armEncoder.getVelocity(), RadiansPerSecond));
                },
                this
            )
        );
    }
}
```

### Test Routine Commands
```java
public class SysIdCommands {
    public static void configureSysIdCommands(RobotContainer robotContainer) {
        DriveSubsystem drive = robotContainer.getDriveSubsystem();
        
        // Bind SysId tests to controller
        new JoystickButton(controller, 1).whileTrue(
            drive.sysIdQuasistatic(SysIdRoutine.Direction.kForward)
                .withName("Drive Quasistatic Forward")
        );
        
        new JoystickButton(controller, 2).whileTrue(
            drive.sysIdQuasistatic(SysIdRoutine.Direction.kReverse)
                .withName("Drive Quasistatic Reverse")  
        );
        
        new JoystickButton(controller, 3).whileTrue(
            drive.sysIdDynamic(SysIdRoutine.Direction.kForward)
                .withName("Drive Dynamic Forward")
        );
        
        new JoystickButton(controller, 4).whileTrue(
            drive.sysIdDynamic(SysIdRoutine.Direction.kReverse)
                .withName("Drive Dynamic Reverse")
        );
    }
}
```

## Test Execution Procedure

### Pre-Test Setup
1. **Ensure Safety**: Clear area around robot, verify emergency stop functionality
2. **Battery**: Use fully charged battery (>12.5V recommended)  
3. **Calibration**: Zero encoders and verify sensor readings
4. **Space**: Ensure adequate space for mechanism movement

### Test Sequence
1. **Connect**: Connect SysId tool to robot via NetworkTables
2. **Select Tests**: Choose appropriate test routines (quasistatic and dynamic)
3. **Run Tests**: Execute each test direction (forward/reverse)
4. **Data Collection**: SysId automatically logs test data

### Quasistatic Test
- **Purpose**: Measure kS and kV constants
- **Method**: Slowly ramp voltage while measuring steady-state velocity
- **Duration**: Typically 10-15 seconds per direction
- **Characteristics**: Smooth acceleration, reaches steady state

### Dynamic Test  
- **Purpose**: Measure kA constant and validate kS/kV
- **Method**: Apply step voltage inputs while measuring acceleration
- **Duration**: Typically 3-5 seconds per direction
- **Characteristics**: Rapid acceleration changes

## Data Analysis

### Importing Data
1. **Load Data**: Import WPILOG files from robot or simulation
2. **Select Dataset**: Choose appropriate test data for analysis
3. **Units**: Verify position and velocity units match robot configuration
4. **Filtering**: Apply data filters if needed for noisy signals

### Analysis Results
SysId provides:
- **Feedforward Constants**: kS, kV, kA values
- **Feedback Gains**: Recommended kP for PID control
- **Goodness of Fit**: R² values indicating data quality
- **Graphs**: Voltage vs. velocity and acceleration plots

### Result Validation
```java
// Example feedforward constants from SysId analysis
public class DriveConstants {
    // SysId Results
    public static final double kS = 0.667; // Volts - static friction
    public static final double kV = 2.44;  // Volt-seconds/meter - velocity
    public static final double kA = 0.27;  // Volt-seconds²/meter - acceleration
    
    // Recommended PID gains
    public static final double kP = 3.5;   // Position gain
    public static final double kI = 0.0;   // Integral gain (usually 0)
    public static final double kD = 0.0;   // Derivative gain (usually 0)
}
```

## Implementation of Results

### Feedforward Controller
```java
import edu.wpi.first.math.controller.SimpleMotorFeedforward;

public class DriveSubsystem extends SubsystemBase {
    // Use SysId results for feedforward
    private final SimpleMotorFeedforward leftFeedforward = 
        new SimpleMotorFeedforward(
            DriveConstants.kS, 
            DriveConstants.kV, 
            DriveConstants.kA
        );
    
    public void setVelocity(double leftVelocity, double rightVelocity) {
        // Calculate feedforward voltages
        double leftFF = leftFeedforward.calculate(leftVelocity);
        double rightFF = rightFeedforward.calculate(rightVelocity);
        
        // Combine with PID feedback
        double leftPID = leftPIDController.calculate(getLeftVelocity(), leftVelocity);
        double rightPID = rightPIDController.calculate(getRightVelocity(), rightVelocity);
        
        // Apply total voltage
        leftMotor.setVoltage(leftFF + leftPID);
        rightMotor.setVoltage(rightFF + rightPID);
    }
}
```

### Phoenix 6 Integration
```java
import com.ctre.phoenix6.configs.Slot0Configs;

public class PhoenixSysIdIntegration {
    public static void applySysIdResults(TalonFX motor) {
        Slot0Configs slot0Configs = new Slot0Configs();
        
        // Apply SysId feedforward results
        slot0Configs.kS = DriveConstants.kS;  // Static gain
        slot0Configs.kV = DriveConstants.kV;  // Velocity gain  
        slot0Configs.kA = DriveConstants.kA;  // Acceleration gain
        slot0Configs.kP = DriveConstants.kP;  // Proportional gain
        
        motor.getConfigurator().apply(slot0Configs);
    }
}
```

## Best Practices

### Test Execution
1. **Multiple Tests**: Run several test cycles for consistency
2. **Battery Consistency**: Use similar battery charge levels
3. **Temperature**: Account for motor temperature effects
4. **Surface Conditions**: Test on competition-like surfaces
5. **Load Conditions**: Test with expected robot weight/loading

### Data Quality
1. **Sensor Accuracy**: Verify encoder resolution and calibration
2. **Sample Rate**: Ensure adequate data sampling frequency
3. **Noise Filtering**: Apply appropriate filtering for noisy signals
4. **Outlier Removal**: Remove obvious measurement errors

### Result Application
1. **Validation**: Test feedforward constants in real robot operation
2. **Iteration**: Refine constants based on actual performance
3. **Documentation**: Record test conditions and results
4. **Version Control**: Track constant changes over time

## Common Issues and Troubleshooting

### Poor Data Quality
- **Symptoms**: Low R² values, scattered data points
- **Causes**: Encoder noise, mechanical slop, insufficient test duration
- **Solutions**: Check sensor connections, extend test duration, filter data

### Inconsistent Results
- **Symptoms**: Different constants between test runs
- **Causes**: Battery voltage variation, temperature changes, surface conditions
- **Solutions**: Use consistent test conditions, multiple test averages

### Implementation Problems
- **Symptoms**: Poor tracking with calculated constants
- **Causes**: Unit mismatches, sensor scaling errors, mechanism changes
- **Solutions**: Verify units, check sensor calibration, re-run tests

## Integration with Other Tools

### AdvantageScope Analysis
```java
// Log SysId data for AdvantageScope analysis
public void logSysIdData() {
    Logger.recordOutput("SysId/Applied Voltage", appliedVoltage);
    Logger.recordOutput("SysId/Position", currentPosition);  
    Logger.recordOutput("SysId/Velocity", currentVelocity);
    Logger.recordOutput("SysId/Timestamp", Timer.getFPGATimestamp());
}
```

### Simulation Validation
```java
// Use SysId constants in simulation
public class DriveSimulation {
    private final LinearSystem<N2, N2, N2> plant;
    
    public DriveSimulation() {
        // Create plant model using SysId constants
        plant = LinearSystemId.identifyDrivetrainSystem(
            DriveConstants.kV,  // kV linear
            DriveConstants.kA,  // kA linear  
            DriveConstants.kV * trackWidth, // kV angular
            DriveConstants.kA * trackWidth  // kA angular
        );
    }
}
```

## Usage in This Codebase

This project uses a centralized registration system for SysId routines via `AbstractSubsystem`.

### Registration Pattern

Each subsystem registers its SysId routines in its constructor:

```java
// In ShooterSubsystem constructor
registerSysIdRoutine("Shooter Wheel", createShooterSysid());
registerSysIdRoutine("Hood Motor", createHoodSysid());

// In ChassisSubsystem constructor
registerSysIdRoutine("Chassis Drive", createDriveSysID());
registerSysIdRoutine("Chassis Steer", createSteerSysID());
registerSysIdRoutine("Chassis Rotation", createRotationSysID());
```

### Creating a SysIdRoutine

```java
private SysIdRoutine createShooterSysid() {
    return new SysIdRoutine(
        new SysIdRoutine.Config(
            null,           // Default ramp rate (1 V/s)
            Volts.of(4),    // Step voltage (lower to prevent brownout)
            null,           // Default timeout (10 s)
            (state) -> SignalLogger.writeString("state", state.toString())
        ),
        new SysIdRoutine.Mechanism(
            (volts) -> motor.setControl(voltOut.withOutput(volts.in(Volts))),
            null,           // Using Phoenix SignalLogger instead
            this
        )
    );
}
```

**Config Parameters**:
| Parameter | Default | Purpose |
|-----------|---------|---------|
| Ramp rate | 1 V/s | How quickly voltage increases in quasistatic test |
| Step voltage | 7 V | Voltage applied during dynamic test |
| Timeout | 10 s | Maximum test duration |
| State callback | none | Log test state changes |

### Running SysId in Test Mode

SysId routines are automatically made available in Test Mode (`OI.enterTestMode()`):

1. **Start/Stop Logging**:
   - Right Bumper → Start Phoenix SignalLogger
   - Left Bumper → Stop Phoenix SignalLogger

2. **Select Routine**:
   - Open SmartDashboard/Shuffleboard
   - Find "SysIdRoutine" chooser
   - Select the routine to run (e.g., "Shooter Wheel", "Chassis Drive")

3. **Run Tests** (D-Pad):
   | Button | Test |
   |--------|------|
   | D-Pad Up | Dynamic Forward |
   | D-Pad Down | Dynamic Reverse |
   | D-Pad Right | Quasistatic Forward |
   | D-Pad Left | Quasistatic Reverse |

4. **Complete Procedure**:
   - Run all 4 tests (each direction, both test types)
   - Stop logging with Left Bumper
   - Retrieve logs from roboRIO `/U/logs/` via SSH or USB

### Registered Routines

| Subsystem | Routine Name | Purpose |
|-----------|--------------|---------|
| Chassis | Chassis Drive | Characterize drive motors (kS, kV, kA) |
| Chassis | Chassis Steer | Characterize steer motors |
| Chassis | Chassis Rotation | Characterize rotation dynamics |
| Shooter | Shooter Wheel | Characterize flywheel motors |
| Shooter | Hood Motor | Characterize hood positioning motor |

### Applying Results

After analysis, apply results to Phoenix 6 motor configs:

```java
// In motor configuration
Slot0Configs slot0 = new Slot0Configs();
slot0.kS = 0.25;   // From SysId
slot0.kV = 0.12;   // From SysId
slot0.kA = 0.01;   // From SysId
slot0.kP = 3.0;    // Tuned based on SysId recommendations

motor.getConfigurator().apply(slot0);
```

## Additional Resources
- [WPILib SysId Documentation](https://docs.wpilib.org/en/stable/docs/software/pathplanning/system-identification/)
- [SysId Tool User Guide](https://docs.wpilib.org/en/stable/docs/software/wpilib-tools/system-identification/)
- [Feedforward Control Theory](https://docs.wpilib.org/en/stable/docs/software/advanced-controls/introduction/control-system-basics.html)
- [Phoenix 6 Feedforward Integration](https://v6.docs.ctr-electronics.com/en/stable/docs/api-reference/device-specific/talonfx/closed-loop-control.html)