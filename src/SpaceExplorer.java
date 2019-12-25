import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.locks.ReentrantLock;
/**
 * Class for a space explorer.
 */
public class SpaceExplorer extends Thread {

	/**
	 * Creates a {@code SpaceExplorer} object.
	 * 
	 * @param hashCount
	 *            number of times that a space explorer repeats the hash operation
	 *            when decoding
	 * @param discovered
	 *            set containing the IDs of the discovered solar systems
	 * @param channel
	 *            communication channel between the space explorers and the
	 *            headquarters
	 */
	static Integer hashCount;
	static Set<Integer> discovered;
	static CommunicationChannel channel;

	public SpaceExplorer(Integer hashCount, Set<Integer> discovered, CommunicationChannel channel) {
		this.hashCount = hashCount;
		this.discovered = discovered;
		this.channel = channel;
	}

	@Override
	public void run() {
		while(true) {
			Message message1 = null, message2 = null;
			// i read 2 messages from the HQ2Se channel
			synchronized (channel) {
				message1 = channel.getMessageHeadQuarterChannel();
				if(message1.getData().compareTo(HeadQuarter.EXIT) == 0) {
					return;
				}
				message2 = channel.getMessageHeadQuarterChannel();
			}
			// if the solar system's hash has not been previously decoded, I compute it
			if(!discovered.contains(message2.getCurrentSolarSystem())) {
				String decoded_string = encryptMultipleTimes(message2.getData(), SpaceExplorer.hashCount);
				Message new_message = new Message(message1.getCurrentSolarSystem(), message2.getCurrentSolarSystem(), decoded_string);
				channel.putMessageSpaceExplorerChannel(new_message);
				SpaceExplorer.discovered.add(message2.getCurrentSolarSystem());
			}
		}

	}
	
	/**
	 * Applies a hash function to a string for a given number of times (i.e.,
	 * decodes a frequency).
	 * 
	 * @param input
	 *            string to he hashed multiple times
	 * @param count
	 *            number of times that the string is hashed
	 * @return hashed string (i.e., decoded frequency)
	 */
	private String encryptMultipleTimes(String input, Integer count) {
		String hashed = input;
		for (int i = 0; i < count; ++i) {
			hashed = encryptThisString(hashed);
		}

		return hashed;
	}

	/**
	 * Applies a hash function to a string (to be used multiple times when decoding
	 * a frequency).
	 * 
	 * @param input
	 *            string to be hashed
	 * @return hashed string
	 */
	private static String encryptThisString(String input) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] messageDigest = md.digest(input.getBytes(StandardCharsets.UTF_8));

			// convert to string
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < messageDigest.length; i++) {
				String hex = Integer.toHexString(0xff & messageDigest[i]);
				if (hex.length() == 1)
					hexString.append('0');
				hexString.append(hex);
			}
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
}
