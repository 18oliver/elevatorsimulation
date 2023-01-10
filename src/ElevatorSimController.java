import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import myfileio.MyFileIO;

// TODO: Auto-generated Javadoc
public class ElevatorSimController {
	private ElevatorSimulation gui;
	private Building building;
	private MyFileIO fio;

	private final int NUM_FLOORS;
	private final int NUM_ELEVATORS;
	private int numFloors;
	private int numElevators;
	private int capacity;
	private int floorTicks;
	private int doorTicks;
	private int tickPassengers;
	private String testfile;
	private String logfile;
	private int maxTime;
	
	private int stepCnt = 0;
	private boolean endSim = false;
		
	/**
	 * Instantiates a new elevator sim controller. 
	 * Reads the configuration file to configure the building and
	 * the elevator characteristics and also select the test
	 * to run. Reads the passenger data for the test to run to
	 * initialize the passenger queue in building...
	 *
	 * @param gui the gui
	 */
	public ElevatorSimController(ElevatorSimulation gui) {
		this.gui = gui;
		fio = new MyFileIO();
		// IMPORTANT: DO NOT CHANGE THE NEXT LINE!!!
		configSimulation("ElevatorSimConfig.csv");
		NUM_FLOORS = numFloors;
		NUM_ELEVATORS = numElevators;
		logfile = testfile.replaceAll(".csv", ".log");
		building = new Building(NUM_FLOORS,NUM_ELEVATORS,capacity,floorTicks,doorTicks,tickPassengers,logfile);
		
		// YOU still need to configure the elevators in the building here....
	}
	
	//TODO: Write methods to update the GUI display
	//      Needs to cover the Elevator state, Elevator passengers
	//      and queues for each floor, as well as the current time
	
	/**
	 * Config simulation. Reads the filename, and parses the
	 * parameters.
	 *
	 * @param filename the filename
	 */
	private void configSimulation(String filename) {
		File configFile = fio.getFileHandle(filename);
		try ( BufferedReader br = fio.openBufferedReader(configFile)) {
			String line;
			while ((line = br.readLine())!= null) {
				String[] values = line.split(",");
				if (values[0].equals("numFloors")) {
					System.out.println(values[0] + ": " + values[1]);
					numFloors = Integer.parseInt(values[1]);
				} else if (values[0].equals("numElevators")) {
					numElevators = Integer.parseInt(values[1]);
				} else if (values[0].equals("passCSV")) {
					System.out.println(values[0] + ": " + values[1]);
					testfile = values[1];
				} else if (values[0].equals("capacity")) {
					System.out.println(values[0] + ": " + values[1]);
					capacity = Integer.parseInt(values[1]);
				} else if (values[0].equals("floorTicks")) {
					System.out.println(values[0] + ": " + values[1]);
					floorTicks = Integer.parseInt(values[1]);
				} else if (values[0].equals("doorTicks")) {
					System.out.println(values[0] + ": " + values[1]);
					doorTicks = Integer.parseInt(values[1]);
				} else if (values[0].equals("tickPassengers")) {
					System.out.println(values[0] + ": " + values[1]);
					tickPassengers = Integer.parseInt(values[1]);
				}
			}
			fio.closeFile(br);
		} catch (IOException e) { 
			System.err.println("Error in reading file: "+filename);
			e.printStackTrace();
		}
	}
	
	/**
	 * Initialize passenger data. Reads the supplied filename,
	 * and for each passenger group, identifies the pertinent information
	 * and adds it to the passengers queue in Building...
	 *
	 * @param filename the filename
	 */
	private void initializePassengerData(String filename) {
		int time=0, numPass=0,fromFloor=0, toFloor=0;
		boolean polite = true;
		int wait = 1000;
		boolean firstLine = true;
		File inputData = fio.getFileHandle(filename);
		try (BufferedReader br = fio.openBufferedReader(inputData)) {
			String line;
			while ((line = br.readLine())!= null) {
				if (firstLine) {
					firstLine = false;
					continue;
				}
				String[] values = line.split(",");
				for (int i = 0; i < values.length; i++) {
					switch (i) {
						case 0 : time      = Integer.parseInt(values[i]); break;
						case 1 : numPass   = Integer.parseInt(values[i]); break;
						case 2 : fromFloor   = Integer.parseInt(values[i]); break;
						case 3 : toFloor  = Integer.parseInt(values[i]); break;
						case 4 : polite = "TRUE".equalsIgnoreCase(values[i]); break;
						case 5 : wait      = Integer.parseInt(values[i]); break;
					}
					
					maxTime = time;
				}
				//  YOU need to write this code in Building.java
				if(stepCnt == time) {
					building.addPassengersToQueue(time,numPass,fromFloor,toFloor,polite,wait);	
				}
			}
			fio.closeFile(br);
		} catch (IOException e) { 
			System.err.println("Error in reading file: "+filename);
			e.printStackTrace();
		}
	}	
	
	/**
	 * Enable logging. A pass-through from the GUI to building
	 */
	public void enableLogging() {
		building.enableLogging();
	}
	
	// TODO: Write any other helper methods that you may need to access data from the building...
	
	
	/** determine if there are more passengers that will appear
	 * @return false if no more, true if there are still passengers
	 */
	public boolean notAllPassengers() {
		if(stepCnt > maxTime) {
			return false;
		}
		
		return true;
	}
	
 	/**
	 * Step sim. See the comments below for the functionality you
	 * must implement......
	 */
	public void stepSim() {
 		// DO NOT MOVE THIS - YOU MUST INCREMENT TIME FIRST!
		stepCnt++;
		
		// TODO: Write the rest of this method
		// If simulation is not completed (not all passengers have been processed
		// or elevator(s) are not all in STOP state), then
		// 		1) check for arrival of any new passengers
		// 		2) update the elevator
		// 		3) update the GUI 
		//  else 
		//    	1) update the GUI
		//		2) close the logs
		//		3) process the passenger results
		//		4) send endSimulation to the GUI to stop ticks.
		
		initializePassengerData(testfile);

		if(!building.allElevatorsStoped() || notAllPassengers() || !(building.getElevators()[0].getCurrState()==0 && building.getElevators()[0].getPrevState()==0)) {
			building.updateElevator(stepCnt);
			
			if(gui != null) {
				gui.updateTime(stepCnt);
				gui.updatePassengerCount(building.getElevators()[0].getPassengers());
				gui.floorButton(building.arrayOfFloors(1), building.arrayOfFloors(-1));
				gui.updateFloor(building.getElevators()[0].getCurrFloor(), building.getElevators()[0].getCurrState());
				gui.updateDirection(building.getElevators()[0].getDirection());
			}
			
		} else {
			
			if(gui != null) {
				gui.updateFloor(building.getElevators()[0].getCurrFloor(), building.getElevators()[0].getCurrState());
				gui.updatePassengerCount(building.getElevators()[0].getPassengers());
				gui.endSimulation();
			}
			building.processPassengerData();
			building.closeLogs(stepCnt);		
		}	
		
	}
	
	protected Building getBuilding() {
		return building;
	}
	
	public int getNumFloors() {
		return NUM_FLOORS;
	}
	
	public int getNumElevators() {
		return NUM_ELEVATORS;
	}
	
	public int getCurrentFloor() {
		return 0;
	}
	
	public String getTestName() {
		return testfile;
	}

}
