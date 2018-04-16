package sys.storage;

import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.trie.PatriciaTrie;
import org.glassfish.jersey.client.ClientConfig;

import api.storage.Namenode;

public class NamenodeClient implements Namenode {
	protected static InetAddress serverAddress;

	Trie<String, List<String>> names = new PatriciaTrie<>();
	ClientConfig config = new ClientConfig();
	Client client = ClientBuilder.newClient(config);
	URI baseURI = null;
	WebTarget target = null;

	public NamenodeClient(URI uri) {
		baseURI = uri;
		target = client.target(baseURI);
	}

	@Override
	public List<String> list(String prefix) {
		return new ArrayList<>(names.prefixMap(prefix).keySet());
	}

	@Override
	public void create(String name, List<String> blocks) {
		Response response = target.path(Namenode.PATH + name).request()
				.post(Entity.entity(blocks, MediaType.APPLICATION_OCTET_STREAM));
		try {
			for (int i = 0; i < 5; i++) {
				if (response.hasEntity()) {
					
					String id = response.readEntity(String.class);
					System.out.println("data resource id: " + id);
					
				} else
					System.err.println(response.getStatus());
			}

		} catch (Exception e) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
	}

	@Override
	public void delete(String prefix) {
		Response response = target.path(Namenode.PATH + "/list/").queryParam("prefix", prefix).request().delete();
		try {
			for (int i = 0; i < 5; i++) {

				if (response.hasEntity()) {
					
					String id = response.readEntity(String.class);
					System.out.println(id);
					
				} else
					System.err.println(response.getStatus());
			}
		} catch (Exception e) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
	}

	@Override
	public void update(String name, List<String> blocks) {
		Response response = target.path(Namenode.PATH + name).request()
				.put(Entity.entity(blocks, MediaType.APPLICATION_OCTET_STREAM));
		try {
			for (int i = 0; i < 5; i++) {

				if (response.hasEntity()) {
					
					String id = response.readEntity(String.class);
					System.out.println(id);
					
				} else
					System.err.println(response.getStatus());
			}
		} catch (Exception e) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
	}

	@Override
	public List<String> read(String name) {
		Response response = target.path(Namenode.PATH + name).request().get();
		try {
			for (int i = 0; i < 5; i++) {

				if (response.hasEntity()) {
					
					List<String> b = response.readEntity(new GenericType<List<String>>() {
					});
					System.out.println("list: " + b);
					return b;
					
				} else
					System.err.println(response.getStatus());
			}
		} catch (Exception e) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
		return null;
	}
}
