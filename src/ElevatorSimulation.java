
import java.util.ArrayList;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

// TODO: Auto-generated Javadoc
public class ElevatorSimulation extends Application {
	private ElevatorSimController controller;
	private final int NUM_FLOORS;
	private final int NUM_ELEVATORS;
	private int currFloor;
	private int passengers;
	private int time;
	private boolean running = true;
	private String direction = "";

	private final int STOP = Elevator.STOP;
	private final int MVTOFLR = Elevator.MVTOFLR;
	private final int OPENDR = Elevator.OPENDR;
	private final int OFFLD = Elevator.OFFLD;
	private final int BOARD = Elevator.BOARD;
	private final int CLOSEDR = Elevator.CLOSEDR;
	private final int MV1FLR = Elevator.MV1FLR;

	private BorderPane main = new BorderPane();
	private Label directionLabel;
	private Label timeLabel;
   private Label passengsLabel;
	private Insets insets = new Insets(10,40,10,10);
	
   private VBox floorPane = new VBox(30);
   private VBox buttonPane = new VBox(30);
   private VBox statePane = new VBox(30);
   
   private Label[] stateArr;
   private int currState = STOP;
   private HBox[] floorButtons;
   private Text[] buttons;
   private HBox topLabels = new HBox();
   
   private HBox optionsPane = new HBox(5);
   private Button stepsim = new Button("StepSim");
   private Button stepn = new Button("Step N");
   private TextField n = new TextField();
   private Button run = new Button("Run");
   private Button log = new Button("Log");
   
	/**
	 * Instantiates a new elevator simulation.
	 */
	public ElevatorSimulation() {
		controller = new ElevatorSimController(this);	
		NUM_FLOORS = controller.getNumFloors();
		NUM_ELEVATORS = controller.getNumElevators();
		currFloor = controller.getCurrentFloor();
		passengers = 0;
		time = 0;
		stateArr = new Label[NUM_FLOORS];
	   floorButtons = new HBox[NUM_FLOORS];
	   buttons = new Text[2*NUM_FLOORS];
	      
	}

	/**
	 * Start.
	 *
	 * @param primaryStage the primary stage
	 * @throws Exception the exception
	 */
	@Override
	public void start(Stage primaryStage) throws Exception {
		// You need to design the GUI. Note that the test name should
		// appear in the Title of the window!!
	   primaryStage.setTitle("Elevator Simulation - "+ controller.getTestName());
      updateLabels();
      topLabels.setSpacing(40);
		main.setTop(topLabels);
		main.setMargin(topLabels, insets);

      floorPane.getChildren().add(new Label("Floor"));
      for(int i = 6; i>=1; i--) 
         floorPane.getChildren().add(new Label(String.valueOf(i)));
      
      buttonPane.getChildren().add(new Label("Button Pressed"));
      buttons[0] = new Text("Down:");
      floorButtons[0] = new HBox();
      floorButtons[0].getChildren().add(buttons[0]);
      buttonPane.getChildren().add(floorButtons[0]);
      for(int i = 1; i<floorButtons.length-1; i++) {
         floorButtons[i] = new HBox(40);
         buttons[i*2] = new Text("Up:");
         buttons[(i*2)+1] = new Text("Down:");
         floorButtons[i].getChildren().add(buttons[i*2]);
         floorButtons[i].getChildren().add(buttons[(i*2)+1]);
         buttonPane.getChildren().add(floorButtons[i]);
      }
      buttons[2*(NUM_FLOORS-1)] = new Text("Up:");
      floorButtons[NUM_FLOORS-1] = new HBox();
      floorButtons[NUM_FLOORS-1].getChildren().add(buttons[10]);
      buttonPane.getChildren().add(floorButtons[5]);
      
      updateFloor(currFloor,currState);

      stepsim.setOnAction(e -> {
         if(running) {
            controller.stepSim();
            updateLabels();
         }
      });
      stepn.setOnAction(e -> {
         int ticks = 0;
         try {
            ticks = Integer.parseInt(n.getText());
            for(int i = 0; i<ticks; i++) {
               if(running) {
                  controller.stepSim();
                  updateLabels();
               }
            }
         }
         catch(Exception e1) {   
         }
      });
      run.setOnAction(e -> {
         while(running) {
            controller.stepSim();
            updateLabels();
         }
      });
      log.setOnAction(e -> {
         controller.enableLogging();
         log.setStyle("-fx-background-color: #ff0000; ");
      });
      
      optionsPane.getChildren().add(stepsim);
      optionsPane.getChildren().add(stepn);
      optionsPane.getChildren().add(n);
      optionsPane.getChildren().add(run);
      optionsPane.getChildren().add(log);
      
      main.setCenter(floorPane);
      main.setMargin(floorPane, insets);
      main.setRight(buttonPane);
      main.setMargin(buttonPane, insets);
      main.setLeft(statePane);
      main.setMargin(statePane, insets);
      main.setBottom(optionsPane);
      
      Scene scene = new Scene(main, 550, 500); 
      primaryStage.setScene(scene);
      primaryStage.show();
	}
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main (String[] args) {
		Application.launch(args);
	}
	
	/**
	 * Updates the floor and state
	 * @param floorNum The floor number it should be updated to (0-5)
	 * @param state The name of the state it should be updated to
	 */
	public void updateFloor(int floorNum, int state) {
	   String currState;
	   switch(state) {
	   case STOP:
	      currState = "Stopped";
	      break;
	   case MVTOFLR:
	      currState = "Moving to floor";
	      break;
	   case OPENDR:
	      currState = "Opening door";
	      break;
	   case OFFLD:
	      currState = "Offloading";
	      break;
	   case BOARD:
	      currState = "Boarding";
	      break;
	   case CLOSEDR:
	      currState = "Closing door";
	      break;
	   case MV1FLR:
	      currState = "Moving 1 floor";
	      break;
	   default:
	      currState = "Improper integer inputted for updateFloor";
	   }
	   currFloor = floorNum+1;
	   statePane = new VBox(30);

      statePane.getChildren().add(new Label("State                   ."));
      for(int i = 0; i<stateArr.length; i++) {
         stateArr[i] = new Label();
      }
      stateArr[NUM_FLOORS-currFloor] = new Label(currState);
      for(int i = 0; i<stateArr.length; i++) 
         statePane.getChildren().add(stateArr[i]);
      main.setLeft(statePane);
      main.setMargin(statePane, insets);
	}
	
	/**
	 * Updates number of passengers who are waiting to go up/down, on each floor
	 * @param up Array of how many people are waiting to go up, for each floor
	 * @param down Array of how many people are waiting to go down, for each floor
	 */
	public void floorButton(int[] up, int[] down) {
      buttonPane.getChildren().clear();
      buttonPane.getChildren().add(new Label("Button Pressed"));
	   for(int i = 0; i<up.length; i++) {
	      int n = (NUM_FLOORS-1)-i;
	      Text textUp = new Text("Up:");
	      Text textDown = new Text("Down:");
	      if(up[i] > 0) 
	         textUp = new Text("Up:" + up[i]);
	      if(down[i]>0)
	         textDown = new Text("Down:" + down[i]);
	      
	      if(n == (NUM_FLOORS-1)) {
	         buttons[10] = textUp;
	         floorButtons[5] = new HBox(40);
	         floorButtons[5].getChildren().add(buttons[10]);
	      }
	      else if(n == 0) {
	         buttons[0] = textDown;
	         floorButtons[0] = new HBox(40);
	         floorButtons[0].getChildren().add(buttons[0]);
	      }
	      else {
	         buttons[(n*2)] = textUp;
	         buttons[(n*2)+1] = textDown;
	         floorButtons[n] = new HBox(40);
	         floorButtons[n].getChildren().add(buttons[n*2]);
	         floorButtons[n].getChildren().add(buttons[n*2+1]);
	      }
	   }
      for(int i = 0; i<floorButtons.length; i++) {
         buttonPane.getChildren().add(floorButtons[i]);
      }
	}
	
	/**
	 * Updates the Time and Passengers Labels
	 */
	private void updateLabels() {
      timeLabel = new Label("Time: " + time);
      passengsLabel = new Label("Passengers on Elevator: " + passengers);
      directionLabel = new Label("Current Direction of Elevator: " + direction);
      topLabels.getChildren().clear();
      topLabels.getChildren().add(passengsLabel);
      topLabels.getChildren().add(timeLabel);
      topLabels.getChildren().add(directionLabel);
      main.setTop(topLabels);
	}
	
	/**
	 * Updates label on time
	 * @param t Time to be updated to
	 */
	public void updateTime(int t) {
	   time = t;
	   updateLabels();
	}
	
	/**
	 * Updates the label on number of passengers on the elevator
	 * @param passeng Number of passengers to update it to
	 */
	public void updatePassengerCount(int passeng) {
	   passengers = passeng;
	   updateLabels();
	}
	
	/**
	 * Stops simulation from running
	 */
	public void endSimulation() {
	   running = false;
	}
	
	/**
	 * Updates label for direction
	 * @param dir 1 = Up, -1 = Down
	 */
	public void updateDirection(int dir) {
	   if(dir == 1)
	      direction = "Up";
	   else if(dir == -1)
	      direction = "Down";
	   else
	      direction = "";
	   updateLabels();
	}
}
