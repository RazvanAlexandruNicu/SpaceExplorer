import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;
/**
 * Class that implements the channel used by headquarters and space explorers to communicate.
 */
public class CommunicationChannel {

	ArrayBlockingQueue<Message> HQ2Explorer = new ArrayBlockingQueue<Message>(2000);
	ArrayBlockingQueue<Message> Explorer2HQ = new ArrayBlockingQueue<Message>(2000);
	static ReentrantLock communicationChannel_lock = new ReentrantLock();
	/**
	 * Creates a {@code CommunicationChannel} object.
	 */
	public CommunicationChannel() {
	}

	/**
	 * Puts a message on the space explorer channel (i.e., where space explorers write to and 
	 * headquarters read from).
	 * 
	 * @param message
	 *            message to be put on the channel
	 */
	public void putMessageSpaceExplorerChannel(Message message) {
		try {
			Explorer2HQ.add(message);
		}
		catch(Exception e) {
		}
	}

	/**
	 * Gets a message from the space explorer channel (i.e., where space explorers write to and
	 * headquarters read from).
	 * 
	 * @return message from the space explorer channel
	 */
	public Message  getMessageSpaceExplorerChannel()
	{
		Message new_message = null;
		try {
			new_message = Explorer2HQ.take();
		}
		catch(InterruptedException e)
		{
		}
		return new_message;
	}
	/**
	 * Puts a message on the headquarters channel (i.e., where headquarters write to and 
	 * space explorers read from).
	 * 
	 * @param message
	 *            message to be put on the channel
	 */
	public void putMessageHeadQuarterChannel(Message message) {

		try {
			// I drop the 'end' messages to make sure i always
			// have in list pairs (parent,child) - even number of elems
			if(message.getData().compareTo("END") != 0) {
				communicationChannel_lock.lock();
				// if the number of elements in the channel
				// is even, I enter the parent first and lock
				if(HQ2Explorer.size() % 2 == 0) {

					HQ2Explorer.add(message);
				}
				else {
					// else, I enter the child and unlock.
					HQ2Explorer.add(message);
					communicationChannel_lock.unlock();
				}
			}
		} catch(Exception e) {
		}
	}

	/**
	 * Gets a message from the headquarters channel (i.e., where headquarters write to and
	 * space explorer read from).
	 * 
	 * @return message from the header quarter channel
	 */
	public Message getMessageHeadQuarterChannel() {
		try {
			return HQ2Explorer.take();
		}
		catch(InterruptedException e)
		{
		}
		return null;
	}
}
