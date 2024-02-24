// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import frc.robot.commands.AutomationCommands;
import frc.robot.subsystems.arm.Arm;
import frc.robot.subsystems.arm.Arm.ArmControlState;
import frc.robot.subsystems.arm.commands.GoToAmp;
import frc.robot.subsystems.arm.commands.GoToGround;
import frc.robot.subsystems.arm.commands.GoToSpeaker;
import frc.robot.subsystems.arm.commands.GoToTravel;
import frc.robot.subsystems.drivetrain.Drivetrain;
import frc.robot.subsystems.drivetrain.commands.TeleOpCommand;
import frc.robot.subsystems.intake.Intake;
import frc.robot.subsystems.intake.commands.IntakeFromGround;
import frc.robot.subsystems.intake.commands.OuttakeToAmp;
import frc.robot.subsystems.intake.commands.SetShooterSpeed;
import frc.robot.subsystems.intake.commands.SetSuckerIntakeSpeed;
import frc.robot.util.DriverController;
import frc.robot.util.DriverController.Mode;
import frc.robot.subsystems.leds.Led;
import frc.robot.subsystems.vision.Vision_old;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.WaitCommand;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;


public class RobotContainer {
  private final AutoPicker autoPicker; 

  // drivetrain of the robot
  private final Drivetrain drivetrain = Drivetrain.getInstance();

  // intake of the bot
  private final Intake intake = Intake.getInstance(); 

  private final Vision_old vision = Vision_old.getInstance();

  // leds!
  private final Led led = Led.getInstance(); 
  // private final AddressableLEDSim sim = new AddressableLEDSim(led.getLED()); 

  // limelight subsystem of robot
  // private final BackVision backVision = BackVision.getInstance();
  // private final FrontVision frontVision = FrontVision.getInstance(); 

  // hang mechanism of robot
  // private final Hang hang = Hang.getInstance();
  
  // arm of the robot
  private final Arm arm = Arm.getInstance();
  
  // private SendableChooser<LEDPattern> patterns = new SendableChooser<>();

  // controller for the driver
  private final DriverController driverController =
      new DriverController(0);

  private final CommandXboxController manipulatorController = new CommandXboxController(1); 

  /** The container for the robot. Contains subsystems, OI devices, and commands. */
  public RobotContainer() {
    autoPicker = new AutoPicker(drivetrain); 
    // Configure the trigger bindings
    configureBindings();

    // driverController.setChassisSpeedsSupplier(drivetrain::getChassisSpeeds); // comment in simulation
    // default command for drivetrain is to calculate speeds from controller and drive the robot
    drivetrain.setDefaultCommand(new TeleOpCommand(drivetrain, driverController));
  }

  private void configureBindings() {
    // --- Driver ---

    // driverController.a().onTrue(new InstantCommand(() -> drivetrain.zeroHeading()));

    driverController.rightTrigger()
      .onTrue(new InstantCommand(() -> driverController.setSlowMode(Mode.SLOW)))
      .onFalse(new InstantCommand(() -> driverController.setSlowMode(Mode.NORMAL))); 


   //  move to setpoints
  //  driverController.y()
  //  .whileTrue(AutomationCommands.pathFindToAmpAndScore(arm, intake)); // move to amp & score

  //  driverController.b()
  //  .whileTrue(AutomationCommands.pathFindToSpeakerAndScore(arm, intake)); // move to speaker & score

  //  driverController.x()
  //  .whileTrue(AutomationCommands.pathFindToGamePiece(driverController)); // auto navigate to note

    driverController.leftBumper()
    .whileTrue(AutomationCommands.generalizedHangCommand(driverController)); 

    // --- Intake --- 

    // outtake
    manipulatorController.leftBumper().and(() -> arm.getArmControlState() == ArmControlState.AMP)
      .whileTrue(new OuttakeToAmp(intake).finallyDo(() -> {
        intake.stopSuck(); 
        intake.stopShoot();
      }));

      // TODO: see which one is better
      // -- hold a button that revs up and outtakes
    // manipulatorController.leftBumper().and(() -> arm.getArmControlState() == ArmControlState.SPEAKER)
    //   .whileTrue(new OuttakeToSpeaker(intake));

      // -- hold a button to rev up, outtakes after release
      manipulatorController.leftBumper().and(() -> arm.getArmControlState() == ArmControlState.SPEAKER)
      .whileTrue(SetShooterSpeed.revAndIndex(intake, 1))
      .onFalse(
        new SetSuckerIntakeSpeed(intake, 1)
        .andThen(new WaitCommand(0.5))
        .andThen(new SetShooterSpeed(intake, 0))
        .andThen(new SetSuckerIntakeSpeed(intake, 0))
      );
      
     // intake
     manipulatorController.leftBumper().and(() -> arm.getArmControlState() == ArmControlState.GROUND)
      .whileTrue(new IntakeFromGround(intake));

      // intake from ground
   
      //  manipulatorController.leftTrigger()
      // .whileTrue(AutomationCommands.generalizedReleaseCommand(driverController)); 

      driverController.rightTrigger()
      .whileTrue(AutomationCommands.autoIntakeCommand()); // intake from ground auto
      driverController.leftTrigger()
      .whileTrue(AutomationCommands.generalizedReleaseCommand(driverController));

    // arm setpoints
    manipulatorController.a().onTrue(new GoToGround(arm));
    manipulatorController.b().onTrue(new GoToTravel(arm));
    manipulatorController.x().onTrue(new GoToSpeaker(arm));
    manipulatorController.y().onTrue(new GoToAmp(arm));


    // --- Hang ---

    // Hang Up when DPAD UP
    // manipulatorController.povUp()
    //   .onTrue(new SetHangSpeed(hang, Constants.HangConstants.kHangSpeed)); 

    // //Hang Down when DPAD DOWN
    // manipulatorController.povDown()
    //   .onTrue(new SetHangSpeed(hang, -Constants.HangConstants.kHangSpeed)); 

    // // Stop hang when neither is pressed
    // manipulatorController.povDown().negate().and(manipulatorController.povUp().negate())
    // .onTrue(new SetHangSpeed(hang, 0)); 



    // TESTING
    // manipulatorController.a().onTrue(new SetVelocity(arm, 0.1)).onFalse(new SetVelocity(arm, 0)); 
    
    // manipulatorController.b().onTrue(new SetVelocity(arm, -0.1)).onFalse(new SetVelocity(arm, 0));
    // manipulatorController.x().onTrue(new SetSuckerIntakeSpeed(intake, -0.5)).onFalse(new SetSuckerIntakeSpeed(intake, 0)); 
    // manipulatorController.y().onTrue(SetShooterSpeed.revAndIndex(intake, 1)).onFalse(new SetShooterSpeed(intake, 0));  

  }
  

  // send any data as needed to the dashboard
  public void doSendables() {
    SmartDashboard.putData("Autonomous", autoPicker.getChooser());
    // SmartDashboard.putBoolean("gyro connected", drivetrain.gyro.isConnected()); 
  }

  // gives the currently picked auto as the chosen auto for the match
  public Command getAutonomousCommand() {
      // return null; 
    return autoPicker.getAuto();
    // return tunerCommand;

  }
}
