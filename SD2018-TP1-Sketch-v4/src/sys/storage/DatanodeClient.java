package sys.storage;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.client.ClientConfig;

import api.storage.Datanode;
import utils.IP;
import utils.Random;

/*
 * Fake Datanode client.
 * 
 * Rather than invoking the Datanode via REST, executes
 * operations locally, in memory.
 * 
 */
public class DatanodeClient implements Datanode {
	private static final int INITIAL_SIZE = 32;
	private Map<String, byte[]> blocks = new HashMap<>(INITIAL_SIZE);

	ClientConfig config = new ClientConfig();
	javax.ws.rs.client.Client client = ClientBuilder.newClient(config);
	URI baseURI = null;
	WebTarget target = client.target(baseURI);

	public DatanodeClient(URI dataURI) {
		baseURI = dataURI;
	}

	@Override
	public String createBlock(byte[] data) {
		Response response = target.path(Datanode.PATH + "/")// acede a "classe/separados" some_path
				.request().post(Entity.entity(data, MediaType.APPLICATION_OCTET_STREAM));// ta na interface
		try {
			for (int i = 0; i < 5; i++) {
				if (response.hasEntity()) {
					blocks.put(Random.key64(), data);
					String id = response.readEntity(String.class);
					System.out.println("data resource id: " + id);
					return id;
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

	@Override
	public void deleteBlock(String block) {
		Response response = target.path(Datanode.PATH + "/" + block) // pq � parametro na interface
				.request().delete();
		try {
			for (int i = 0; i < 5; i++) {
				if (response.getStatusInfo().equals(Status.NO_CONTENT)) {
					System.out.println("deleted data resource...");
				} else
					System.err.println(response.getStatus());
			}
		} catch (Exception e) {
			try {
				Thread.sleep(20000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
	}

	@Override
	public byte[] readBlock(String block) {
		Response response = target.path(Datanode.PATH + "/" + block) // pq � parametro na interface
				.request().get();
		try {
			for (int i = 0; i < 5; i++) {
				if (response.hasEntity()) {
					byte[] data = response.readEntity(byte[].class);
					return data;
				}
			}
		} catch (Exception e) {
			try {
				Thread.sleep(20000);
			} catch (InterruptedException e1) {

				e1.printStackTrace();
			}
		}
		return null;
	}
}
