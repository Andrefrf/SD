package sys.storage.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import api.storage.Datanode;
import utils.IP;
import utils.Random;

public class DatanodeServer implements Datanode {

	private static final int INITIAL_SIZE = 32;
	private Map<String, byte[]> blocks = new HashMap<>(INITIAL_SIZE);
	private static final String URI_BASE = "http://" + IP.hostAddress() + ":7778/";

	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException {

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
		System.out.println("BASE: " + URI_BASE);
		System.out.println("SERVER: " + serverURI);
		try {
			MulticastSocket socket = new MulticastSocket(9200);
			socket.joinGroup(group);
			while (true) {
				byte[] buffer = new byte[MAX_DATAGRAM_SIZE];
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				InetAddress received = request.getAddress();
				int port = request.getPort();
				
				socket.receive(request);
				String a = new String(request.getData(), "UTF-8").trim();
				if (a.contains("Datanode")) {
					System.out.println("DONE");
					request = new DatagramPacket(URI_BASE.getBytes(), URI_BASE.getBytes().length, received, port);
					socket.send(request);
				};
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
				File file = new File(id);
				FileOutputStream f = new FileOutputStream(file);
				f.write(data);
				f.close();
			} catch (Exception e) {
				System.out.println("ERRO!");
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
		try {
			File file = new File(block);
			@SuppressWarnings("resource")
			FileInputStream f = new FileInputStream(file);
			data = new byte[(int)file.length()];
			f.read(data);
		} catch (IOException e) {
			System.out.println("ERRO!");
			e.printStackTrace();
		}
		return data;
	}
}
