package sys.storage.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.trie.PatriciaTrie;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import api.storage.Namenode;
import utils.IP;


public class NamenodeServer implements Namenode {

	protected Trie<String, List<String>> names = new PatriciaTrie<>();//guardar o (blob-respetivos dataNodes) 
	private static final String URI_BASE = "http://" + IP.hostAddress() + ":7777/";
	
	@SuppressWarnings("unlikely-arg-type")
	public static void main(String[] args) throws IOException {

		ResourceConfig config = new ResourceConfig();
		config.register(new NamenodeServer());

		final int MAX_DATAGRAM_SIZE = 65536;
		final InetAddress group = InetAddress.getByName("228.100.0.1");
		URI serverURI = UriBuilder.fromUri(URI_BASE).build();
		JdkHttpServerFactory.createHttpServer(serverURI, config);
		String serverPath = serverURI.getPath();
		
		System.err.println("Server ready....");

		if (!group.isMulticastAddress()) {
			System.out.println("Not a multicast address (use range : 224.0.0.0 -- 239.255.255.255)");
			System.exit(1);
		}

		try (MulticastSocket socket = new MulticastSocket(9000)) {
			socket.joinGroup(group);
			while (true) {
				byte[] buffer = new byte[MAX_DATAGRAM_SIZE];
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				
				int port = request.getPort();
				if(!request.getData().equals("Namenode".getBytes())) {
					continue;
				}
				request = new DatagramPacket(URI_BASE.getBytes(), URI_BASE.getBytes().length,
						request.getAddress(), request.getPort());
				socket.send(request);
			}
		}
	}

	@Override
	public List<String> list(String prefix) {
		return new ArrayList<>(names.prefixMap( prefix ).keySet());
	}

	@Override
	public synchronized void create(String name,  List<String> blocks) {
		if( names.putIfAbsent(name, new ArrayList<>(blocks)) != null )
			throw new WebApplicationException(Status.CONFLICT);
	}

	@Override
	public synchronized void delete(String prefix) {
		List<String> keys = new ArrayList<>(names.prefixMap( prefix ).keySet());
		if( ! keys.isEmpty() ) {
			names.keySet().removeAll( keys );
			throw new WebApplicationException(Status.NO_CONTENT);
		}
		else {
			throw new WebApplicationException(Status.NOT_FOUND);
		}
	}

	@Override
	public synchronized void update(String name, List<String> blocks) {
		if( names.putIfAbsent( name, new ArrayList<>(blocks)) == null ) {
			throw new WebApplicationException(Status.NOT_FOUND);
		}
	}

	@Override
	public List<String> read(String name) {
		List<String> blocks = names.get( name );
		if( blocks == null )
			throw new WebApplicationException(Status.NOT_FOUND);
		return blocks;
	}
}
