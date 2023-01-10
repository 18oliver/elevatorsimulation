import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import myfileio.MyFileIO;

// TODO: Auto-generated Javadoc
public class Building {
	private final static Logger LOGGER = Logger.getLogger(Building.class.getName());
	private FileHandler fh;
	private MyFileIO fio;
	private File passDataFile;

	// ArrayLists to store those passengers that arrived or gave up...
	private ArrayList<Passengers> passSuccess;
	private ArrayList<Passengers> gaveUp;

	private final int UP = 1;
	private final int Down = -1;
	
	private boolean firstBoard = true;
	private int numberOfPeople =0;
	private static int delay= 0;
	private static int delayCounter= 0;


	private static int offLoadIterations = 0;
	private static boolean firstTick = true;
	private static int offLoadTime = -1;


	// Elevator State Variables
	private final int NUM_FLOORS;
	private final int NUM_ELEVATORS;
	public Floor[] floors;
	private Elevator[] elevators;
	private int [] delayArr;

	private GenericQueue passengerQueue;
	/**
	 * Instantiates a new building.
	 *
	 * @param numFloors the num floors
	 * @param numElevators the num elevators
	 * @param logfile the logfile
	 */
	public Building(int numFloors, int numElevators,int capacity, int floorTicks,int doorTicks, int tickPassengers,String logfile) {
		NUM_FLOORS = numFloors;
		NUM_ELEVATORS = numElevators;

		floors = new Floor[numFloors];
		elevators = new Elevator[numElevators];

		//Instantiate all floors
		for(int i=0;i<numFloors;i++) {
			floors[i] = new Floor(20);
		}

		//Instantiate all Elevators
		for (int i = 0; i < numElevators; i++) {
			elevators[i]= new Elevator(numFloors, capacity, floorTicks,doorTicks,tickPassengers); 
		}

		delayArr = new int [] {1, 1, 1, 
										  2, 2, 2,
										  3, 3, 3,
										  4, 4, 4,
										  5, 5, 5,
										  6, 6, 6,
										  7, 7, 7};
		
		passengerQueue = new GenericQueue(1000);
		
		passSuccess = new ArrayList<Passengers>();

		// Initialize the LOGGER - DO NOT CHANGE THIS!!!
		System.setProperty("java.util.logging.SimpleFormatter.format","%4$-7s %5$s%n");
		LOGGER.setLevel(Level.OFF);
		try {
			fh = new FileHandler(logfile);
			LOGGER.addHandler(fh);
			SimpleFormatter formatter = new SimpleFormatter();
			fh.setFormatter(formatter);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// passDataFile is where you will write all the results for those passengers who successfully
		// arrived at their destination and those who gave up...
		fio = new MyFileIO();
		passDataFile = fio.getFileHandle(logfile.replaceAll(".log","PassData.csv"));

		// create the floors and the elevator(s)
		// note that YOU will need to create and config each specific elevator...

		gaveUp =new ArrayList<Passengers>();
	}

	/**
	 * Adds Passengers to passenger Queue
	 * @param time  An int of the current time since start
	 * @param numPass An int of the number of Passengers
	 * @param fromFloor An int of the floor the passengers arrived from
	 * @param toFloor An int of the floor the passengers are headed to.
	 * @param wait An int of the amount of time the passengers are willing to wait
	 * @param polite A boolean of whether or not passenger is polite
	 * @return boolean True if was added false if could nto be added
	 */
	public boolean addPassengersToQueue(int time,int numPass, int fromFloor, int toFloor, boolean polite,int wait) {
		Passengers newGroup = new Passengers(time,numPass,fromFloor,toFloor,polite, wait);
		if(fromFloor < toFloor) {
			if(!(floors[fromFloor-1].size(UP) >20)) {
				floors[fromFloor-1].offer(1, newGroup);
				logCalls(time, numPass, fromFloor-1, 1, newGroup.getId());
				return true;
			}
		} else {	
			if(!(floors[fromFloor-1].size(Down) >20)) {
				floors[fromFloor-1].offer(-1, newGroup);
				logCalls(time, numPass, fromFloor-1, -1, newGroup.getId());
				return true;
			}
		}
		return false;
	}

	/**
	 * Prints out the passenger Queue;
	 */
	public void dumpPassQ () {
		ListIterator<Passengers> passengers = passengerQueue.getListIterator();
		if (passengers != null) {
			System.out.println("Passengers Queue:");
			while (passengers.hasNext()) {
				Passengers p = passengers.next();
				System.out.println("   ID="+p.getId()+"  NumPass="+p.getNumberOfPassengers()+
						"   From:"+(p.getFromFloor()+1)+"   To:"+(p.getToFloor()+1)+
						"   Polite="+p.isPolite()+"   Wait="+p.getWaitTime());
			}
		}
	}

	/**
	 * Returns the int representation of the state that will follow 
	 * after the current Stop State with for specified elevator
	 * @param time 
	 * @param el The specified elevator
	 * @return int representaiton of next state
	 */
	public int currStateStop(int time, Elevator el) {
		el.stop();
		if(isBuildingEmpty())
			return 0;
		Floor currFloor = floors[el.getCurrFloor()];
		if(!currFloor.isEmpty(UP)||!currFloor.isEmpty(Down)) {
			if(!floors[el.getCurrFloor()].isEmpty(UP) && floors[el.getCurrFloor()].isEmpty(Down)) {
				el.setDirection(UP);
			}
			else if(!currFloor.isEmpty(Down) && currFloor.isEmpty(UP)) {
				el.setDirection(Down);
			}
			else {
				if(getNumberOfCallsAboveGoingUp(el)>=getNumberOfCallsBelowGoingDown(el)) {
					el.setDirection(UP);
				} else {
					el.setDirection(Down);
				}
			}
			return 2;
		} else {	
			
			int upCall=0;
			int downCall=0;
			for(int j=0;j<NUM_FLOORS;j++) {
				if(!floors[j].isEmpty(UP)) {
					upCall++;
				} else if ((!floors[j].isEmpty(Down))){
					downCall++;
				}
			}
			
			int lowestFloor = lowestFloorCalled();
			int highestFloor= highestFloorCalled();
			
			if(upCall>downCall) {			
				el.setMoveToFloor(lowestFloor);
				el.setMoveToFloorDir(UP);

			} else if (upCall<downCall) {			
				el.setMoveToFloor(highestFloor);
				el.setMoveToFloorDir(Down);

			} else {
				if(Math.abs(el.getCurrFloor() - highestFloor)<Math.abs(el.getCurrFloor() - lowestFloor)) {
					el.setMoveToFloorDir(Down);
					el.setMoveToFloor(highestFloor);

				} else {
					el.setMoveToFloorDir(UP);
					el.setMoveToFloor(lowestFloor);
				}
			}
			setDirection(el);
			return 1;
		}
	}
	/**
	 * Returns the int representation of the state that will follow 
	 * after the current MvToFlr State for the specified elevator.
	 * @param el The specified elevator
	 * @return int representaiton of next state
	 */
	public int currStateMvToFlr(int time, Elevator el) {
		el.moveElevator();

		if((el.getCurrFloor() != el.getMoveToFloor())) {
			return 1;
		} else {
			el.setDirection(el.getMoveToFloorDir());
			return 2;
		}
	}

	/**
	 * Returns the int representation of the state that will follow
	 * after the current OpenDr State for the specified elevator.
	 * @param el The specified elevator
	 * @return int representaiton of next state
	 */
	public int currStateOpenDr(int time, Elevator el) {
		el.openDoor();
		el.setPrevFloor(el.getCurrFloor());
		if(el.getDoorState()!= el.getTicksDoorOpenClose()) {
			return 2;
		} else {
			if(el.getContained()[el.getCurrFloor()].size()!=0) {
				return 3;
			} else {
				return 4;
			}
		}
	}

	/**
	 * Returns the int representation of the state that will follow
	 * after the current OffLd State for the specified elevator.
	 * @param el The specified elevator
	 * @return int representaiton of next state
	 */
	public int currStateOffLd(int time, Elevator el) {
		if(firstTick) {
			if(offLoadTime == -1) 
				offLoadTime = time;
			
			ArrayList <Passengers> offLoad = el.offload();	
			if(offLoad==null) 
				return 3;
			
			for(int i=0;i<offLoad.size();i++) {
				logArrival(offLoadTime, offLoad.get(i).getNumberOfPassengers(), el.getCurrFloor(), offLoad.get(i).getId());
				passSuccess.add(offLoad.get(i));
				offLoad.get(i).setTimeArrived(time);
			}
			offLoadTime = -1;
		}
		
		if(!floors[el.getCurrFloor()].isEmpty(el.getDirection())) {
			return 4;
		} else {
			boolean anyCurrDirectionCalls = anyCurrDirectionCalls(el);
			if(el.getPassengers() != 0 || anyCurrDirectionCalls)
				return 5;
			if(!floors[el.getCurrFloor()].isEmpty((Down)*el.getDirection())) {
				el.setDirection(el.getDirection()*-1);
				return 4;
			}
			return 5;
		}
	}


	public void giveUpCheck(int time) {
		for(int i=0;i<NUM_FLOORS;i++) {
			Passengers upPass = floors[i].peek(UP);
			Passengers downPass = floors[i].peek(Down);
			if(upPass!=null) {
				if(upPass.getTime()+upPass.getWaitTime()<time) {
					logGiveUp(time,  upPass.getNumberOfPassengers(), i, 1, upPass.getId());
					gaveUp.add(floors[i].remove(UP));
				}
			}
			if(downPass!=null) {
				if(downPass.getTime()+downPass.getWaitTime()<time) {
					logGiveUp(time,  downPass.getNumberOfPassengers(), i, -1, downPass.getId());
					gaveUp.add(floors[i].remove(Down));
				}
			}
		}
	}
	/**
	 * Returns the int representation of the state that will follow
	 * after the current Board State for the specified elevator.
	 * @param el The specified elevator
	 * @return int representaiton of next state
	 */
	public int currStateBoard(int time, Elevator el) {
		
//		for(int i=0;i<NUM_FLOORS;i++) {
//			Passengers upPass = floors[i].peek(UP);
//			Passengers downPass = floors[i].peek(Down);
//			if(upPass!=null) {
//				if(upPass.getTime()+upPass.getWaitTime()<time) {
//					logGiveUp(time,  upPass.getNumberOfPassengers(), i, 1, upPass.getId());
//					gaveUp.add(floors[i].remove(UP));
//				}
//			}
//			if(downPass!=null) {
//				if(downPass.getTime()+downPass.getWaitTime()<time) {
//					logGiveUp(time,  downPass.getNumberOfPassengers(), i, -1, downPass.getId());
//					gaveUp.add(floors[i].remove(Down));
//				}
//			}
//		}
		
		
		Floor currFloor = floors[el.getCurrFloor()];

		ArrayList <Passengers> boarding = new ArrayList<Passengers>();
		int direction = el.getDirection();
		int iterations =currFloor.getQueueNumPass(direction);
					
		int projectedNumberOfPeople = el.getPassengers();

		for(int i=0;i<iterations;i++) {
			for(int j=0;j<15;j++)
				giveUpCheck(time);
			iterations =currFloor.getQueueNumPass(direction);
			if(!currFloor.isEmpty(direction)) {	
				projectedNumberOfPeople = el.getPassengers() + currFloor.peek(direction).getNumberOfPassengers();
				
				//Necessary so people who are rude do not try and open the door again
				currFloor.peek(direction).setPolite(true);
				if(projectedNumberOfPeople <= el.getCapacity()) {
					el.setPassengers(el.getPassengers() + currFloor.peek(direction).getNumberOfPassengers());
					currFloor.peek(direction).setBoardTime(time);
					boarding.add(currFloor.remove(direction));
					logBoard(time, boarding.get(i).getNumberOfPassengers(), el.getCurrFloor(), el.getDirection(), boarding.get(i).getId());
					
				} else {
					if(firstBoard) 
						logSkip(time, currFloor.peek(direction).getNumberOfPassengers(), el.getCurrFloor(), el.getDirection(), currFloor.peek(direction).getId());
					firstBoard = false;
				}
				//if(!currFloor.isEmpty(el.getDirection())) 
			}
		}
		
		el.board(boarding);
		
		for(int i=0;i<boarding.size();i++) {
			numberOfPeople += boarding.get(i).getNumberOfPassengers();
		}
		
		if(el.getDirection()==1) {
			int highestFloorToMoveTo=0;
			for(int i=0;i<NUM_FLOORS;i++) {
				if(!el.getContained()[i].isEmpty()) {
					highestFloorToMoveTo=i;
				}
			}
			el.setMoveToFloor(highestFloorToMoveTo);
		} else {
			int lowestFloorToMoveTo=0;
			for(int i=NUM_FLOORS-1;i>=0;i--) {
				if(!el.getContained()[i].isEmpty()) {
					lowestFloorToMoveTo=i;
				}
			}
			el.setMoveToFloor(lowestFloorToMoveTo);
		}
		
		if(numberOfPeople != 0)
			delay = delayArr[numberOfPeople-1];
		
		if(el.isBoarding(delay)) {
			return 4;
		} else {
		   delay = 0;
		   numberOfPeople =0;
		   firstBoard =true;
		   return 5;
		}
	}

	/**8
	 * Returns the int representation of the state that will follow
	 * after the current CloseDr State for the specified elevator.
	 *@param el The specified elevator
	 * @return int representaiton of next state
	 */
	public int currStateCloseDr(int time, Elevator el) {
		el.closeDoor();
		if(!floors[el.getCurrFloor()].isEmpty(el.getDirection()) && !floors[el.getCurrFloor()].peek(el.getDirection()).isPolite()) {
         floors[el.getCurrFloor()].peek(el.getDirection()).setPolite(true);
         return 2;
      }
		if(el.getDoorState()!=0) {
			return 5;
		} else {
			if(el.getPassengers()!=0) {
				return 6;
			} else {
				if(el.getDirection() == 1) {
					for(int i=el.getCurrFloor()+1;i<NUM_FLOORS;i++) {
						if(floors[i].getQueueNumPass(el.getDirection())!=0 || floors[i].getQueueNumPass((Down)*el.getDirection())!=0) {
							return 6;
						} 
					}
					for(int i=el.getCurrFloor()-1;i>=0;i--) {
						if(floors[i].getQueueNumPass(el.getDirection())!=0 || floors[i].getQueueNumPass((Down)*el.getDirection())!=0) {
							el.setDirection((Down)*el.getDirection());
							return 6;
						} 
					}
				}
				else if(el.getDirection() == -1) {
					for(int i=el.getCurrFloor()-1;i>=0;i--) {
						if(floors[i].getQueueNumPass(el.getDirection())!=0 || floors[i].getQueueNumPass((Down)*el.getDirection())!=0) {
							return 6;
						} 
					}
					for(int i=el.getCurrFloor()+1;i<NUM_FLOORS;i++) {
						if(floors[i].getQueueNumPass(el.getDirection())!=0 || floors[i].getQueueNumPass((Down)*el.getDirection())!=0) {
							el.setDirection((Down)*el.getDirection());
							return 6;
						} 
					}
				}
				return 0;	
			}
		}
	}

	/**
	 * Returns the int representation of the state that will follow
	 * after the current Mv1Flr State for the specified elevator.
	 * @param el The specified elevator
	 * @return int representaiton of next state
	 */
	public int currStateMv1Flr(int time, Elevator el) {
		el.moveElevator();

		if(el.isMoving()) {
			return 6;
		}
		Floor currfloor =floors[el.getCurrFloor()];
		if(el.getCurrFloor() == el.getMoveToFloor()){
			return 2;
		} else if(!currfloor.isEmpty(el.getDirection())) {
			return 2;
		} else {
			boolean stopHere = false;
			for(int i = 0; i<NUM_FLOORS; i++) {
				ArrayList<Passengers>[] contained = el.getContained();
				for(int j = 0; j<contained[i].size(); j++) {
					if(contained[i].get(j).getToFloor() == el.getCurrFloor()) {
						stopHere = true;
					}
				}
			}
			if(stopHere) { //If passengers want to get off at the current floor
				return 2;
			} 
			boolean anyCallsCurrDirection = anyCurrDirectionCalls(el);

			if(el.getPassengers() == 0 && !anyCallsCurrDirection && currfloor.getQueueNumPass((Down)*el.getDirection()) !=0 ) {
				el.setDirection(el.getDirection()*-1);
				return 2;
			}
		}
		return 6;

	}



	//_____________________________________________________________________		
	// DO NOT CHANGE ANYTHING BELOW THIS LINE:
	/**
	 * Update elevator - this is called AFTER time has been incremented.
	 * -  Logs any state changes, if the have occurred,
	 * -  Calls appropriate method based upon currState to perform
	 *    any actions and calculate next state...
	 *
	 * @param time the time
	 */
	// YOU WILL NEED TO CODE ANY MISSING METHODS IN THE APPROPRIATE CLASSES...
	public void updateElevator(int time) {
		for (Elevator lift: elevators) {
			if (elevatorStateChanged(lift))
				logElevatorStateChanged(time,lift.getPrevState(),lift.getCurrState(),lift.getPrevFloor(),lift.getCurrFloor());

			switch (lift.getCurrState()) {
			case Elevator.STOP: lift.updateCurrState(currStateStop(time,lift)); break;
			case Elevator.MVTOFLR: lift.updateCurrState(currStateMvToFlr(time,lift)); break;
			case Elevator.OPENDR: lift.updateCurrState(currStateOpenDr(time,lift)); break;
			case Elevator.OFFLD: lift.updateCurrState(currStateOffLd(time,lift)); break;
			case Elevator.BOARD: lift.updateCurrState(currStateBoard(time,lift)); break;
			case Elevator.CLOSEDR: lift.updateCurrState(currStateCloseDr(time,lift)); break;
			case Elevator.MV1FLR: lift.updateCurrState(currStateMv1Flr(time,lift)); break;
			}
		}
	}

	private boolean elevatorStateChanged(Elevator lift) {
		return (lift.getPrevState()!=lift.getCurrState());
	}

	/**
	 * Process passenger data. Do NOT change this - it simply dumps the 
	 * collected passenger data for successful arrivals and give ups. These are
	 * assumed to be ArrayLists...
	 */
	public void processPassengerData() {

		try {
			BufferedWriter out = fio.openBufferedWriter(passDataFile);
			out.write("ID,Number,From,To,WaitToBoard,TotalTime\n");
			for (Passengers p : passSuccess) {
				String str = p.getId()+","+p.getNumberOfPassengers()+","+(p.getFromFloor()+1)+","+(p.getToFloor()+1)+","+
						(p.getBoardTime() - p.getTime())+","+(p.getTimeArrived() - p.getTime())+"\n";
				out.write(str);
			}
			for (Passengers p : gaveUp) {
				String str = p.getId()+","+p.getNumberOfPassengers()+","+(p.getFromFloor()+1)+","+(p.getToFloor()+1)+","+
						p.getWaitTime()+",-1\n";
				out.write(str);
			}
			fio.closeFile(out);
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Enable logging. Prints the initial configuration message.
	 * For testing, logging must be enabled BEFORE the run starts.
	 */
	public void enableLogging() {
		LOGGER.setLevel(Level.INFO);
		for (Elevator el:elevators)
			logElevatorConfig(el.getCapacity(),el.getTicksPerFloor(), el.getTicksDoorOpenClose(), el.getPassPerTick(), el.getCurrState(),el.getCurrFloor());
	}

	/**
	 * Close logs, and pause the timeline in the GUI
	 *
	 * @param time the time
	 */
	public void closeLogs(int time) {
		if (LOGGER.getLevel() == Level.INFO) {
			logEndSimulation(time);
			fh.flush();
			fh.close();
		}
	}

	/**
	 * Prints the state.
	 *
	 * @param state the state
	 * @return the string
	 */
	private String printState(int state) {
		String str = "";

		switch (state) {
		case Elevator.STOP: 		str =  "STOP   "; break;
		case Elevator.MVTOFLR: 		str =  "MVTOFLR"; break;
		case Elevator.OPENDR:   	str =  "OPENDR "; break;
		case Elevator.CLOSEDR:		str =  "CLOSEDR"; break;
		case Elevator.BOARD:		str =  "BOARD  "; break;
		case Elevator.OFFLD:		str =  "OFFLD  "; break;
		case Elevator.MV1FLR:		str =  "MV1FLR "; break;
		default:					str =  "UNDEF  "; break;
		}
		return(str);
	}

	/**
	 * Log elevator config.
	 *
	 * @param capacity the capacity
	 * @param ticksPerFloor the ticks per floor
	 * @param ticksDoorOpenClose the ticks door open close
	 * @param passPerTick the pass per tick
	 * @param state the state
	 * @param floor the floor
	 */
	private void logElevatorConfig(int capacity, int ticksPerFloor, int ticksDoorOpenClose, int passPerTick, int state, int floor) {
		LOGGER.info("CONFIG:   Capacity="+capacity+"   Ticks-Floor="+ticksPerFloor+"   Ticks-Door="+ticksDoorOpenClose+
				"   Ticks-Passengers="+passPerTick+"   CurrState=" + (printState(state))+"   CurrFloor="+(floor+1));
	}

	/**
	 * Log elevator state changed.
	 *
	 * @param time the time
	 * @param prevState the prev state
	 * @param currState the curr state
	 * @param prevFloor the prev floor
	 * @param currFloor the curr floor
	 */
	private void logElevatorStateChanged(int time, int prevState, int currState, int prevFloor, int currFloor) {
		LOGGER.info("Time="+time+"   Prev State: " + printState(prevState) + "   Curr State: "+printState(currState)
		+"   PrevFloor: "+(prevFloor+1) + "   CurrFloor: " + (currFloor+1));
	}

	/**
	 * Log arrival.
	 *
	 * @param time the time
	 * @param numPass the num pass
	 * @param floor the floor
	 * @param id the id
	 */
	private void logArrival(int time, int numPass, int floor,int id) {
		LOGGER.info("Time="+time+"   Arrived="+numPass+" Floor="+ (floor+1)
				+" passID=" + id);						
	}

	/**
	 * Log calls.
	 *
	 * @param time the time
	 * @param numPass the num pass
	 * @param floor the floor
	 * @param dir the dir
	 * @param id the id
	 */
	private void logCalls(int time, int numPass, int floor, int dir, int id) {
		LOGGER.info("Time="+time+"   Called="+numPass+" Floor="+ (floor +1)
				+" Dir="+((dir>0)?"Up":"Down")+"   passID=" + id);
	}

	/**
	 * Log give up.
	 *
	 * @param time the time
	 * @param numPass the num pass
	 * @param floor the floor
	 * @param dir the dir
	 * @param id the id
	 */
	private void logGiveUp(int time, int numPass, int floor, int dir, int id) {
		LOGGER.info("Time="+time+"   GaveUp="+numPass+" Floor="+ (floor+1) 
				+" Dir="+((dir>0)?"Up":"Down")+"   passID=" + id);				
	}

	/**
	 * Log skip.
	 *
	 * @param time the time
	 * @param numPass the num pass
	 * @param floor the floor
	 * @param dir the dir
	 * @param id the id
	 */
	private void logSkip(int time, int numPass, int floor, int dir, int id) {
		LOGGER.info("Time="+time+"   Skip="+numPass+" Floor="+ (floor+1) 
				+" Dir="+((dir>0)?"Up":"Down")+"   passID=" + id);				
	}

	/**
	 * Log board.
	 *
	 * @param time the time
	 * @param numPass the num pass
	 * @param floor the floor
	 * @param dir the dir
	 * @param id the id
	 */
	private void logBoard(int time, int numPass, int floor, int dir, int id) {
		LOGGER.info("Time="+time+"   Board="+numPass+" Floor="+ (floor+1) 
				+" Dir="+((dir>0)?"Up":"Down")+"   passID=" + id);				
	}

	/**
	 * Log end simulation.
	 *
	 * @param time the time
	 */
	private void logEndSimulation(int time) {
		LOGGER.info("Time="+time+"   Detected End of Simulation");
	}


	public Floor[] getFloors() {
		return floors;
	}

	public void setFloors(Floor[] floors) {
		this.floors = floors;
	}

	public Elevator[] getElevators() {
		return elevators;
	}

	public void setElevators(Elevator[] elevators) {
		this.elevators = elevators;
	}

	public GenericQueue getPassengerQueue() {
		return passengerQueue;
	}

	public void setPassengerQueue(GenericQueue passengerQueue) {
		this.passengerQueue = passengerQueue;
	}

	/** 
	 * Boolean representation of whether or not all elevators are stopped
	 * @return boolean True if all elevators are stopped false if else
	 */
	public boolean allElevatorsStoped() {
		for(int i=0;i<elevators.length;i++) {
			if(!(elevators[i].getCurrState()==0)) {
				return false;
			} 
		}
		return true;
	}

	//Number of People up and number of people going down per floor
	/**
	 * An array of the number of passengers waiting at each floor given the respective direction
	 * @param direction The direction of the queue waiting at each floor
	 * @return 	int [] Number of passengers waiting at each floor given the respective direction

	 */
	public int [] arrayOfFloors(int direction) {
		int [] arrayOfFloors = new int [NUM_FLOORS];

		for(int j=0;j<NUM_FLOORS;j++) {
			arrayOfFloors[j] = floors[j].getQueueNumPass(direction);
		}

		return arrayOfFloors;
	}

	/**
	 * Sets the direction of the Elevator given its moveToFloor and Current Floor
	 * @param el The specified elevator 
	 */
	private void setDirection(Elevator el) {
		if(el.getMoveToFloor() > el.getCurrFloor())
			el.setDirection(UP);
		else
			el.setDirection(Down);
	}

	/**
	 * Determines if there are any Calls on the Building
	 * @param el The elevator which is to be compared 
	 * @return boolean True if there are calls in the building false if else
	 */
	private boolean anyCurrDirectionCalls(Elevator el) {
		if(el.getDirection() == 1) {
			for(int i = el.getCurrFloor()+1; i<NUM_FLOORS; i++) {
				if(!floors[i].isEmpty(UP) || !floors[i].isEmpty(Down))
					return true;
			}
		}
		else if(el.getDirection() == -1) {
			for(int i = el.getCurrFloor()-1; i>=0; i--) {
				if(!floors[i].isEmpty(UP) || !floors[i].isEmpty(Down))
					return true;
			}
		}
		return false;
	}

	private boolean isBuildingEmpty() {
		boolean empty =true;

		for(int i=0;i<NUM_FLOORS;i++) {
			if(floors[i].isEmpty(UP) && floors[i].isEmpty(Down)) {
				continue;
			} else {
				empty = false;
			}
		}
		return empty;
	}

	private int getNumberOfCallsAboveGoingUp(Elevator el) {
		int upCallCount = 0;

		for(int i = el.getCurrFloor(); i<NUM_FLOORS; i++) 
			if(!floors[i].isEmpty(UP))
				upCallCount += 1;

		return upCallCount;
	}

	private int getNumberOfCallsBelowGoingDown(Elevator el) {
		int downCallCount = 0;

		for(int i = el.getCurrFloor(); i>=0; i--) 
			if(!floors[i].isEmpty(Down))
				downCallCount += 1;

		return downCallCount;
	}

	private int lowestFloorCalled() {
		int lowestFloor = 0;
		for(int j=0;j<NUM_FLOORS;j++) {
			if(!floors[j].isEmpty(UP)) {
				lowestFloor=j;
				break;
			}
		}
		return lowestFloor;
	}
	
	private int highestFloorCalled() {
		int highestFloor =0;
		for(int j=NUM_FLOORS-1;j>=0;j--) {
			if(!floors[j].isEmpty(Down)) {
				highestFloor=j;
				break;
			}
		}
		return highestFloor;
	}
}
