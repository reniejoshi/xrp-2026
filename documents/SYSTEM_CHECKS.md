# System Checks - Comprehensive Guide

## Overview

System Checks are automated diagnostic routines that verify the health of robot mechanisms during pre-match testing. They measure motor current draw and check for hardware faults to ensure all components are functioning within expected parameters.

**Purpose**:
- Detect failing motors before matches
- Verify wiring and mechanical connections
- Catch brownout-prone mechanisms
- Provide quick go/no-go status for each subsystem

---

## Architecture

### Core Classes

System Checks are built on two classes in `AbstractSubsystem.java`:

| Class | Purpose |
|-------|---------|
| `SystemCheckMotor` | Wraps a TalonFX motor with nominal current bounds and fault detection |
| `SystemCheckRoutine` | Combines a test command with motors to monitor |

### Registration Flow

```
Subsystem Constructor
    │
    ├── Create test command (exercise the mechanism)
    │
    ├── Define SystemCheckMotor for each motor to monitor
    │
    └── Call registerSystemCheckRoutine(name, command, motors...)
            │
            └── Stored in static map for access in Test Mode
```

---

## How It Works

### 1. SystemCheckMotor

Each motor to be tested is wrapped in a `SystemCheckMotor`:

```java
new SystemCheckMotor(
    "Motor Name",        // Descriptive name for logging
    talonFXMotor,        // The TalonFX hardware object
    Amps.of(2.0),        // Minimum expected current (if below, motor may not be working)
    Amps.of(25.0)        // Maximum expected current (if above, motor may be stalled)
)
```

**What it tracks**:
- Accumulates supply current readings during the test
- Calculates average current after test completes
- Checks if average is within `[minNominal, maxNominal]` range

### 2. SystemCheckRoutine

Combines a test command with the motors to monitor:

```java
registerSystemCheckRoutine(
    "Shooter System Check",       // Routine name
    () -> testCommand,            // Supplier<Command> that exercises the mechanism
    shooterMotor1,                // SystemCheckMotor...
    shooterMotor2
);
```

**Execution flow**:
1. Clear all accumulated current readings
2. Start the test command AND begin measuring currents (raced together)
3. When test command ends, calculate average current for each motor
4. Log results and set SmartDashboard status

---

## Implementation Example

### Step 1: Define Nominal Current Bounds (Constants)

```java
public class ShooterConstants {
    // Current draw when running at test speed
    public static final Current RIGHT_MIN_NOMINAL_CURRENT = Amps.of(5.0);
    public static final Current RIGHT_MAX_NOMINAL_CURRENT = Amps.of(30.0);
    
    public static final Current LEFT_MIN_NOMINAL_CURRENT = Amps.of(5.0);
    public static final Current LEFT_MAX_NOMINAL_CURRENT = Amps.of(30.0);
    
    public static final Current HOOD_MIN_NOMINAL_CURRENT = Amps.of(1.0);
    public static final Current HOOD_MAX_NOMINAL_CURRENT = Amps.of(10.0);
}
```

**Tuning tips**:
- Run the mechanism normally and observe current draw in logs
- Set minimum ~50% below normal operating current
- Set maximum ~50% above normal operating current
- Account for battery voltage variations

### Step 2: Create Test Command

The test command should exercise the mechanism through its full range of motion:

```java
Supplier<Command> testCommand = () ->
    // Spin up shooter wheels
    Commands.runOnce(() -> setAngularVelocity(MAX_WHEEL_VELOCITY), this)
        .andThen(Commands.waitSeconds(5))  // Let it reach steady state
        .andThen(Commands.runOnce(this::stopShooterWheel, this))
    
        // Move hood through positions
        .andThen(Commands.runOnce(() -> setHoodAngle(MAX_HOOD_ANGLE), this))
        .andThen(Commands.waitSeconds(1))
        .andThen(Commands.runOnce(() -> setHoodAngle(MID_HOOD_ANGLE), this))
        .andThen(Commands.waitSeconds(1))
        .andThen(Commands.runOnce(() -> setHoodAngle(MIN_HOOD_ANGLE), this))
        .andThen(Commands.waitSeconds(1))
        .andThen(Commands.runOnce(() -> setHoodAngle(HOOD_ZERO_ANGLE), this));
```

**Best practices**:
- Include `waitSeconds()` to allow current to stabilize
- Test at realistic operating speeds/positions
- End in a safe position

### Step 3: Register the Routine

```java
// In subsystem constructor
registerSystemCheckRoutine(
    "Shooter System Check",
    testCommand,
    
    new SystemCheckMotor(
        "Right Shooter Motor",
        rightShooterMotor,
        RIGHT_MIN_NOMINAL_CURRENT,
        RIGHT_MAX_NOMINAL_CURRENT
    ),
    new SystemCheckMotor(
        "Left Shooter Motor",
        leftShooterMotor,
        LEFT_MIN_NOMINAL_CURRENT,
        LEFT_MAX_NOMINAL_CURRENT
    ),
    new SystemCheckMotor(
        "Hood Motor",
        hoodMotor,
        HOOD_MIN_NOMINAL_CURRENT,
        HOOD_MAX_NOMINAL_CURRENT
    )
);
```

---

## Running System Checks

### In Test Mode

System checks are automatically registered to SmartDashboard in `OI.enterTestMode()`:

```java
AbstractSubsystem.getSystemCheckRoutines().forEach((name, routine) -> {
    SmartDashboard.putData(name + " SystemCheck", routine.getCommand());
});
```

**To run**:
1. Enable Test Mode on the Driver Station
2. Open SmartDashboard or Shuffleboard
3. Find the button labeled `{RoutineName} SystemCheck`
4. Click to run the check

### Results

After the routine completes:

**Console output** (via tinylog):
```
INFO  Shooter System Check - Right Shooter Motor average current: 12.5A
INFO  Shooter System Check - Left Shooter Motor average current: 11.8A
ERROR Shooter System Check - Hood Motor is out of nominal range! Nominal is 1.0A to 10.0A
```

**SmartDashboard**:
- `{RoutineName} Good` → `true` if all motors passed, `false` if any failed

---

## Fault Detection

`SystemCheckMotor.notFaulted()` checks for TalonFX hardware faults:

| Fault | Meaning |
|-------|---------|
| `Hardware` | Internal motor controller hardware fault |
| `DeviceTemp` | Motor controller over temperature |
| `ProcTemp` | Processor over temperature |
| `UnlicensedFeatureInUse` | Using Pro features without license |
| `RemoteSensorDataInvalid` | CANcoder or other remote sensor not responding |
| `BootDuringEnable` | Motor rebooted while robot was enabled |
| `OverSupplyV` | Supply voltage exceeded safe limits |
| `RemoteSensorPosOverflow` | Remote sensor position overflow |

**If any fault is detected**, it is logged as an error and the motor fails the check.

---

## Chassis Example

The chassis registers checks for all 8 swerve motors:

```java
registerSystemCheckRoutine("Chassis System Check", 
    () -> driveInSquareCommand,  // Command to move the robot
    
    // Drive motors (4)
    new SystemCheckMotor("Front Left Drive", 
        drivetrain.getModule(0).getDriveMotor(),
        CHASSIS_DRIVE_MIN_NOMINAL_CURRENT,
        CHASSIS_DRIVE_MAX_NOMINAL_CURRENT),
    // ... 3 more drive motors
    
    // Steer motors (4)
    new SystemCheckMotor("Front Left Steer",
        drivetrain.getModule(0).getSteerMotor(),
        CHASSIS_STEER_MIN_NOMINAL_CURRENT,
        CHASSIS_STEER_MAX_NOMINAL_CURRENT)
    // ... 3 more steer motors
);
```

---

## Troubleshooting

### Motor Shows Below Minimum Current

| Possible Cause | Solution |
|----------------|----------|
| Motor disconnected | Check CAN wiring |
| Motor not actually running | Verify command is applied |
| Mechanism jammed/stuck | Manually check movement |
| Test duration too short | Increase `waitSeconds()` |

### Motor Shows Above Maximum Current

| Possible Cause | Solution |
|----------------|----------|
| Motor stalled | Check for obstructions |
| Gear ratio too high | Verify mechanical design |
| Binding/friction | Lubricate or realign |
| Current limits not configured | Set motor current limits |

### Faults Detected

| Fault | Action |
|-------|--------|
| Hardware | Replace motor controller |
| DeviceTemp | Improve cooling, reduce load |
| BootDuringEnable | Check CAN wiring, reduce bus load |
| OverSupplyV | Check battery connections |

---

## Summary

| Step | Action |
|------|--------|
| 1 | Define nominal current bounds in Constants |
| 2 | Create a test command that exercises the mechanism |
| 3 | Register with `registerSystemCheckRoutine()` |
| 4 | Run in Test Mode via SmartDashboard |
| 5 | Check console and SmartDashboard for results |

System checks provide a quick, automated way to verify motor health before matches. Run them during practice and pit stops to catch problems early!

