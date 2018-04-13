package sys.storage.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import api.storage.Datanode;
import utils.IP;
import utils.Random;


public class DatanodeServer implements Datanode {

	private static final int INITIAL_SIZE = 32;
	private Map<String, byte[]> blocks = new HashMap<>(INITIAL_SIZE);
	private static final String URI_BASE = "http://"+IP.hostAddress()+":9999/";
	
	public static void main(String[] args) throws IOException {
		
		System.setProperty("java.net.preferIPv4Stack", "true");
		ResourceConfig config = new ResourceConfig();
		config.register(new DatanodeServer());

		System.err.println("Server ready....");
		
		int MAX_DATAGRAM_SIZE = 65536;
		InetAddress group = InetAddress.getByName("225.9.0.1");
		URI serverURI = UriBuilder.fromUri(group.getHostName()).build();
		JdkHttpServerFactory.createHttpServer(serverURI, config);

		if (!group.isMulticastAddress()) {
			System.out.println("Not a multicast address (use range : 224.0.0.0 -- 239.255.255.255)");
			System.exit(1);
		}

		System.err.println("Server ready....");
		
		try (MulticastSocket socket = new MulticastSocket(9100)) {
			socket.joinGroup(group);
			while (true) {
				byte[] buffer = new byte[MAX_DATAGRAM_SIZE];
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				socket.receive(request);
				if(!request.getData().equals(PATH)){
					continue;
				}
				
			}
		}
	}

	@Override
	public String createBlock(byte[] data) {
		String id = Random.key64();
		blocks.put(id, data);
		return URI_BASE + "datanote/" + id;
	}

	@Override
	public void deleteBlock(String block) {
		blocks.remove(block);
	}

	@Override
	public byte[] readBlock(String block) {
		byte[] data = blocks.get(block);
		if (data != null)
			return data;
		else
			throw new WebApplicationException(Status.NOT_FOUND);
	}
}
