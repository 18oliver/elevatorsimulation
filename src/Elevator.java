import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * @author Gabi
 * This class will represent an elevator, and will contain
 * configuration information (capacity, speed, etc) as well
 * as state information - such as stopped, direction, and count
 * of passengers targetting each floor...
 */
public class Elevator {
	// Elevator State Variables
	final static int STOP = 0;
	final static int MVTOFLR = 1;
	final static int OPENDR = 2;
	final static int OFFLD = 3;
	final static int BOARD = 4;
	final static int CLOSEDR = 5;
	final static int MV1FLR = 6;

	// Default configuration parameters - these will be read from a file....
	private int capacity = 15;
	private int ticksPerFloor = 5;
	private int ticksDoorOpenClose = 2;  
	private int passPerTick = 3;
	
	//State Variables
	// track the elevator state
	private int currState;
	private int prevState;
	// track what floor you are on, and where you came from
	private int prevFloor;
	private int currFloor;
	// direction 1 = up, -1 = down
	private int direction;
	// timeInState is reset on state entry, used to determine if state is finished
	// or if floor has changed...
	private int timeInState;
	// used to track where the the door is in OPENDR and CLOSEDR states 
	private int doorState;
	// number of passengers on the elevator
	private int passengers;
	private int boarded;
	// when exiting the STOP ==> MVTOFLR, the floor to moveTo and the direction to go in once you
	// get there...
	private int moveToFloor;
	private int moveToFloorDir;
	
	private ArrayList<Passengers>[] contained;

	// You need to update this constructor to configure the elevator and set any additional state as necessary.
	public Elevator(int numFloors, int capacity, int floorTicks, int doorTicks, int tickPassengers) {
		this.prevState = STOP;
		this.currState = STOP;
		this.timeInState = 0;
		this.currFloor = 0;
		contained = new ArrayList[numFloors];
		this.capacity = capacity;
		ticksPerFloor = floorTicks;
		ticksDoorOpenClose = doorTicks;
		passPerTick = tickPassengers;
		boarded = 0;
		doorState=0;
		
		for(int i = 0; i < contained.length; i++) {
			contained[i] = new ArrayList<Passengers>();
		}
	}
	
	/** update elevator state, update time in state
	 * @param currState new state
	 */
	public void updateCurrState(int currState) {
		this.prevState = this.currState;
		this.currState = currState;
		
		if(this.prevState != this.currState) {
			this.timeInState = 0;
		}
	}
	
	
	/** determine if the elevator is in the process of boarding
	 * @param delay number of delay ticks calculated
	 * @return false if boarding finished, true otherwise
	 */
	public boolean isBoarding(int delay) {
		if (timeInState >= delay) {
			return false;
		}
		
		return true;
	}
	
	/** determine if elevator is between floors
	 * @return true if between floors, false if not moving floors
	 */
	public boolean isMoving() {
		if ((timeInState % ticksPerFloor) != 0) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * increment time in state, update current floor after enough ticks
	 */
	public void moveElevator() {
		timeInState++;
		prevFloor = currFloor;
		
		if ((timeInState % ticksPerFloor) == 0) {
			currFloor = currFloor + direction;
		}
	}
	
	/**
	 * increment time in state, update current floor after enough ticks
	 */
	public void moveToFloor() {
		timeInState++;
		prevFloor = currFloor;
		
		if ((timeInState % ticksPerFloor) == 0) {
			currFloor = currFloor + moveToFloorDir;
		}
	}
	
	/**
	 * decrements doorstate up to a minimum close
	 */
	public void closeDoor() {
		if (doorState != 0) {
			doorState--;
		}
	}
	
	/**
	 * increments doorstate up to a maximum open
	 */
	public void openDoor() {
		if (doorState != ticksDoorOpenClose) {
			doorState++;
		}
	}
	
	/** remove passengers from elevator and update variables
	 * @return all passenger groups that left
	 */
	public ArrayList<Passengers> offload() {
		ArrayList<Passengers> returnArray = new ArrayList<Passengers>();
		timeInState++;
		
		if ((timeInState % passengerTime()) == 0) {
			for(int i = 0; i < contained[currFloor].size(); i++) {
				passengers -= contained[currFloor].get(i).getNumberOfPassengers();
			}
			
			
			if(contained[currFloor] == null) {
			   return null;
			}
         for(int i = 0; i<contained[currFloor].size(); i++) {
            returnArray.add(contained[currFloor].get(i));
         }
			contained[currFloor].clear();
			return returnArray;
		}
		
		return null;
	}
	
	/** add boarding passengers onto elevator and update variables
	 * @param boarding arraylist of passenger goups boarding
	 */
	public void board(ArrayList<Passengers> boarding) {
		timeInState++;
		
		for(int i = 0; i < boarding.size(); i++) {
			contained[boarding.get(i).getToFloor()].add(boarding.get(i));
			//passengers += boarding.get(i).getNumberOfPassengers();
		}
	}
	
	/**
	 * keep track of ticks in stop state
	 */
	public void stop() {
		timeInState++;
	}
	
	/** calculate number of ticks to offload passengers
	 * @return number of ticks to offload passengers
	 */
	public int passengerTime() {
		int leaving = 0;
		for(int i = 0; i < contained[currFloor].size(); i++) {
				leaving += contained[currFloor].get(i).getNumberOfPassengers();
		}
		
		if((leaving % passPerTick) == 0) {
			return leaving / passPerTick;
		}
		
		return (leaving / passPerTick) + 1;
	}
	
	public void setTicksPerFloor(int newValue) {
		ticksPerFloor = newValue;
	}
	
	public void setPassPerTick(int newValue) {
		passPerTick = newValue;
	}
	
	public void setTicksDoorOpenClose(int newValue) {
		ticksDoorOpenClose = newValue;
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	public int getCurrState() {
		return currState;
	}

	public void setCurrState(int currState) {
		this.currState = currState;
	}

	public int getPrevState() {
		return prevState;
	}

	public void setPrevState(int prevState) {
		this.prevState = prevState;
	}

	public int getPrevFloor() {
		return prevFloor;
	}

	public void setPrevFloor(int prevFloor) {
		this.prevFloor = prevFloor;
	}

	public int getCurrFloor() {
		return currFloor;
	}

	public void setCurrFloor(int currFloor) {
		this.currFloor = currFloor;
	}

	public int getDirection() {
		return direction;
	}

	public void setDirection(int direction) {
		this.direction = direction;
	}

	public int getTimeInState() {
		return timeInState;
	}

	public void setTimeInState(int timeInState) {
		this.timeInState = timeInState;
	}

	public int getDoorState() {
		return doorState;
	}

	public void setDoorState(int doorState) {
		this.doorState = doorState;
	}

	public int getPassengers() {
		return passengers;
	}

	public void setPassengers(int passengers) {
		this.passengers = passengers;
	}

	public int getMoveToFloor() {
		return moveToFloor;
	}

	public void setMoveToFloor(int moveToFloor) {
		this.moveToFloor = moveToFloor;
	}

	public int getMoveToFloorDir() {
		return moveToFloorDir;
	}

	public void setMoveToFloorDir(int moveToFloorDir) {
		this.moveToFloorDir = moveToFloorDir;
	}

	public static int getStop() {
		return STOP;
	}

	public static int getMvtoflr() {
		return MVTOFLR;
	}

	public static int getOpendr() {
		return OPENDR;
	}

	public static int getOffld() {
		return OFFLD;
	}

	public static int getBoard() {
		return BOARD;
	}

	public static int getClosedr() {
		return CLOSEDR;
	}

	public static int getMv1flr() {
		return MV1FLR;
	}

	public int getTicksPerFloor() {
		return ticksPerFloor;
	}

	public int getTicksDoorOpenClose() {
		return ticksDoorOpenClose;
	}

	public int getPassPerTick() {
		return passPerTick;
	}
	
	public ArrayList<Passengers>[] getContained() {
		return contained;
	}
	
	
}
