package sys.storage.server;

import java.awt.geom.IllegalPathStateException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import api.storage.Datanode;
import utils.IP;
import utils.Random;

public class DatanodeServer implements Datanode {

	private static Logger logger = Logger.getLogger(Datanode.class.toString());
	private static final int INITIAL_SIZE = 32;
	private Map<String, byte[]> blocks = new HashMap<>(INITIAL_SIZE);
	private static final String URI_BASE = "http://" + IP.hostAddress() + ":8500/";

	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException {

		System.setProperty("java.net.preferIPv4Stack", "true");
		ResourceConfig config = new ResourceConfig();
		config.register(new DatanodeServer());

		int MAX_DATAGRAM_SIZE = 65536;
		InetAddress group = InetAddress.getByName("225.9.0.1");
		URI serverURI = URI.create(URI_BASE);
		JdkHttpServerFactory.createHttpServer(serverURI, config);
		
		if (!group.isMulticastAddress()) {
			System.out.println("Not a multicast address (use range : 224.0.0.0 -- 239.255.255.255)");
			System.exit(1);
		}

		System.err.println("Server ready....");
		System.out.println(serverURI);
		try {
			MulticastSocket socket = new MulticastSocket(9000);
			socket.joinGroup(group);
			while (true) {
				byte[] buffer = new byte[MAX_DATAGRAM_SIZE];
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);

				socket.receive(request);
				if (!request.getData().equals(PATH)) {
					continue;
				}
				request = new DatagramPacket(URI_BASE.getBytes(), URI_BASE.getBytes().length, request.getAddress(),
						request.getPort());
				socket.send(request);
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	@Override
	public synchronized String createBlock(byte[] data) {
		String id = Random.key64();
		if (blocks.putIfAbsent(id, data) != null) {
			throw new WebApplicationException(Status.CONFLICT);
		} else {
			String url = "http://" + IP.hostAddress() + ":8500/" + "datanode/" + id;
			try {
				FileOutputStream file = new FileOutputStream(id);
				file.write(data);
				file.flush();
				file.close();
			} catch (Exception e) {
			}
			return url;
		}
	}

	@Override
	public synchronized void deleteBlock(String block) {
		if (blocks.containsKey(block)) {
			blocks.remove(block);
			throw new WebApplicationException(Status.NO_CONTENT);
		} else {
			throw new WebApplicationException(Status.NOT_FOUND);
		}
	}

	@Override
	public byte[] readBlock(String block) {

		byte[] data = null;
		byte[] g = null;

		try {
			FileInputStream f = new FileInputStream(block);
			f.read(g);
			while (g != null) {
				data = g;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;
		
		
//		 byte[] data = blocks.get(block);
//		 System.out.println(block);
//		 if (data != null) {
//		 return data;
//		 } else {
//		 throw new WebApplicationException(Status.NOT_FOUND);
//		 }
	}
}
