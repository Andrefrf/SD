package sys.storage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.trie.PatriciaTrie;

import api.storage.Namenode;

/*
 * Fake NamenodeClient client.
 * 
 * Rather than invoking the Namenode via REST, executes
 * operations locally, in memory.
 * 
 * Uses a trie to perform efficient prefix query operations.
 */
public class NamenodeClient implements Namenode {

	private static Logger logger = Logger.getLogger(NamenodeClient.class.toString());

	Trie<String, List<String>> names = new PatriciaTrie<>();

	public static void main(String[] args) throws IOException {
		final int MAX_DATAGRAM_SIZE = 65536;
		final InetAddress group = InetAddress.getByName(args[0]);
		if (!group.isMulticastAddress()) {
			System.out.println("Not a multicast address (use range : 224.0.0.0 -- 239.255.255.255)");
			System.exit(1);
		}

		try (MulticastSocket socket = new MulticastSocket(9000)) {
			socket.joinGroup(group);
			while (true) {
				byte[] buffer = new byte[MAX_DATAGRAM_SIZE];
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				socket.receive(request);
				System.out.write(request.getData(), 0, request.getLength());
				// prepare and send reply... (unicast)
			}
		}
	}

	@Override
	public List<String> list(String prefix) {
		return new ArrayList<>(names.prefixMap(prefix).keySet());// fica assim
	}

	@Override
	public void create(String name, List<String> blocks) {
		if (names.putIfAbsent(name, new ArrayList<>(blocks)) != null)
			throw new WebApplicationException(Status.CONFLICT);// trow new exception
	}

	@Override
	public void delete(String prefix) {
		List<String> keys = new ArrayList<>(names.prefixMap(prefix).keySet());
		if (!keys.isEmpty())
			names.keySet().removeAll(keys);// se n tiver dizer ya
	}

	@Override
	public void update(String name, List<String> blocks) {
		if (names.putIfAbsent(name, new ArrayList<>(blocks)) == null) {
			logger.info("NOT FOUND");
		}
	}

	@Override
	public List<String> read(String name) {
		List<String> blocks = names.get(name);
		if (blocks == null)
			logger.info("NOT FOUND");
		return blocks;
	}
}
