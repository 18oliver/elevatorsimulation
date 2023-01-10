
public class Passengers {
	private static int ID=0;
	// this will be initialized in the constructor so that it is unique for each
	// set of Passengers - and then increment the static ID
	private int id;	
	// These will come from the csv file, and should be initialized in the 
	// constructor.
	private int time;
	private int numberOfPassengers;
	private int fromFloor;
	private int toFloor;
	private boolean polite = true;
	private int waitTime;
	// These fields will be initialized during run time - boardTime is when the group
	// starts getting on the elevator, timeArrived is when the elevator starts offloading
	// at the desired floor
	private int boardTime;
	private int timeArrived;
	
	// TODO: Write the constructor for this class
	//       Remember to appropriately adjust toFloor and fromFloor 
	//       from American to European numbering...
	/**
	 * Instantiates a new object of type Passenger
	 * @param time int The arrival time of the passenger
	 * @param number int The number of passengers in this group
	 * @param fromFloor int The floor at which the passenger will arrive
	 * @param toFloor int The floor at which the passengers will leave
	 * @param polite boolean Wether or not the passengers are polite
	 * @param waitTime int The amount of time the passengers are willing to wait for the elevator
	 */
	public Passengers(int time, int number, int fromFloor, int toFloor, boolean polite, int waitTime) {
		this.id = ID;
		ID++;
		this.time = time;
		this.numberOfPassengers = number;
		this.fromFloor = fromFloor-1;
		this.toFloor = toFloor-1;
		this.waitTime = waitTime;
		this.polite = polite;
	}

	public int getBoardTime() {
		return boardTime;
	}

	public void setBoardTime(int boardTime) {
		this.boardTime = boardTime;
	}

	public int getTimeArrived() {
		return timeArrived;
	}

	public void setTimeArrived(int timeArrived) {
		this.timeArrived = timeArrived;
	}

	public int getId() {
		return id;
	}

	public int getTime() {
		return time;
	}

	public int getNumberOfPassengers() {
		return numberOfPassengers;
	}

	public int getFromFloor() {
		return fromFloor;
	}

	public int getToFloor() {
		return toFloor;
	}

	public boolean isPolite() {
		return polite;
	}
	
	public void setPolite(boolean t) {
	   polite = t;
	}

	public int getWaitTime() {
		return waitTime;
	}


	// TODO: Write the getters and setters for this method
	
}
