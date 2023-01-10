// ListIterater can be used to look at the contents of the floor queues for 
// debug/display purposes...
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

// TODO: Auto-generated Javadoc
/**
 * The Class Floor.
 */
public class Floor {
	// queues to track the up requests and down requests on each floor
	final int UP = 1;
	final int DOWN = -1;
	final int maximumSize;

	private GenericQueue<Passengers> down;
	private GenericQueue<Passengers> up;

	public Floor(int qSize) {
		down = new GenericQueue<Passengers>(qSize);
		up = new GenericQueue<Passengers>(qSize);
		maximumSize = qSize;
	}

	// TODO: Write the helper methods for this class. 
	//       The building will need to be able to manipulate the
	//       up and down queues for each floor.... 
	//       This includes accessing all of the lower level queue
	//       methods as well as possibly accessing the contents of each
	//       queue

	/**
	 * Adds the element to the direction based queue if possible
	 *
	 * @param direction the int based direction (1 UP) (-1 DOWN)
	 * @param pass the Passenger to add
	 * @return true, if successful
	 * @throws IllegalStateException the illegal state exception
	 */
	public boolean addToFloor(int direction, Passengers pass) throws IllegalStateException{
		if(direction==UP) {
			if(up.size()==maximumSize) {
				throw new IllegalStateException("Add failed - UP Queue is full");
			}
			up.add(pass);
			return true;
		}

		if(down.size()==maximumSize) {
			throw new IllegalStateException("Add failed - UP Queue is full");
		}
		down.add(pass);
		return true;
	}

	/**
	 * Removes the element at the head of the queue based on direction, if possible
	 *
	 * @param direction the int based direction (1 UP) (-1 DOWN)
	 * @return the Passenger
	 * @throws NoSuchElementException the no such element exception
	 */
	public Passengers remove(int direction) throws NoSuchElementException {
		if(direction==UP) {
			if(up.isEmpty()) 
				throw new NoSuchElementException("No Element to be removed - UP Queue is empty");
			return up.remove();
		}
		if(down.isEmpty()) 
			throw new NoSuchElementException("No Element to be removed - DOWN Queue is empty");
		return down.remove();
	}	

	/**
	 * Element returns the Passenger at the head of the queue without 
	 * removing it - if possible
	 *
	 *	 
	 * @param direction the int based direction (1 UP) (-1 DOWN)
	 * @return the Passenger
	 * @throws NoSuchElementException the no such element exception
	 */
	public Passengers element(int direction) throws NoSuchElementException {	
		if(direction==UP) {
			if (up.isEmpty())
				throw new NoSuchElementException("No Element to be removed - UP Queue is empty");
			return(up.element());
		}
		if (down.isEmpty())
			throw new NoSuchElementException("No Element to be removed - DOWN Queue is empty");
		return(down.element());	
	}

	/**
	 * Offer. - same functionality as add, but returns false if add is not possible
	 *
	 * @param direction the int based direction (1 UP) (-1 DOWN)
	 * @param pass the Passenger being offered
	 * @return true, if successful
	 */
	public boolean offer (int direction, Passengers pass) {
		if(direction==UP) {
			return up.offer(pass);
		}
		return down.offer(pass);
	}

	/**
	 * Poll. Same functionality as remove(), but returns null if direction based queue is empty
	 *
	 * @param direction the int based direction (1 UP) (-1 DOWN)
	 * @return the Passenger
	 */
	public Passengers poll(int direction) {
		//retrieves the value of the first element of the queue dependent on direction and removes it from the queue. If the queue is empty returns null
		if(direction==UP) {
			return up.poll();
		}
		return down.poll();
	}	

	/**
	 * Peek. Same functionality as element(), but returns  null if
	 * direction based queue is empty.
	 *
	 * @param direction the int based direction (1 UP) (-1 DOWN)
	 * @return the e
	 */
	public Passengers peek(int direction) {
		//retrieves the value of the first element of the queue dependent on direction without removing it from the queue. If the queue is empty returns null
		if(direction==UP) {
			return up.peek();
		}
		return down.peek();
	}

	/**
	 * Checks if direction based queue is empty.
	 *	 
	 * @param direction the int based direction (1 UP) (-1 DOWN)
	 * @return true, if is empty
	 */
	public boolean isEmpty(int direction){
		//Returns a boolean according to whether or not a queue is empty. Differentiates between queues based on imputed direction(UP or DOWN)
		if(direction==UP) {
			return up.isEmpty();
		}
		return down.isEmpty();
	}

	/**
	 * Size. Returns the number of entries currently in the queue
	 * @param direction the int based direction (1 UP) (-1 DOWN)
	 * @return the int
	 */
	public int size(int direction){
		if(direction==UP) {
			return up.size();
		}
		return down.size();
	}

	/**
	 * Returns the Number of passengers in the associated floor queue
	 * @param direction
	 * @return int Number of passengers in given direction queue
	 */
	public int getQueueNumPass(int direction) {

		int sum =0;

		if (direction == 1) {
			ListIterator<Passengers> list = up.getListIterator();

			if (list != null) 
				while (list.hasNext())
					sum += list.next().getNumberOfPassengers();

		} else if (direction ==-1) {
			ListIterator<Passengers> list = down.getListIterator();

			if (list != null) 
				while (list.hasNext())
					sum += list.next().getNumberOfPassengers();

		}
		return sum;
	}
}

