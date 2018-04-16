package sys.Multicast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.HashSet;

public class Multicast {

	private HashSet<URI> received;
	private static final int MAX_DATAGRAM_SIZE = 65536;
	final int port;
	final InetAddress group;

	public Multicast() throws UnknownHostException {
		port = 9200;
		group = InetAddress.getByName("225.9.0.1");

		if (!group.isMulticastAddress()) {
			System.out.println("Not a multicast address (use range : 224.0.0.0 -- 239.255.255.255)");
			System.exit(1);
		}
	}
	
	public HashSet<URI> send(String type) {
		received = new HashSet<>();
		byte[] data = type.getBytes();
		try (MulticastSocket socket = new MulticastSocket()) {

			DatagramPacket request = new DatagramPacket(data, data.length, group, port);
			while (received.isEmpty()) {

				socket.send(request);
				socket.setSoTimeout(4000);
				data = new byte[MAX_DATAGRAM_SIZE];
				request = new DatagramPacket(data,data.length);
				try {

					for (;;) {

						request = new DatagramPacket(data,data.length);
						socket.receive(request);
						String rec = new String(request.getData(), 0, request.getLength());
						System.out.println(type);
						received.add(URI.create(rec));

					}
				} catch (Exception e) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e1) {
						socket.close();

						e1.printStackTrace();
					}
				}
			}

			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return received;
	}
}
