package Clients;

import java.net.URI;

import javax.sound.midi.SysexMessage;
import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.client.ClientConfig;

public class changeRes {
	public static void main(String[] args) {

		ClientConfig config = new ClientConfig();
		Client client = ClientBuilder.newClient(config);

		URI baseURI = UriBuilder.fromUri("http://localhost:9999/v1").build();
		WebTarget target = client.target(baseURI);

		Response response = target.path("/some-path/4bndgo52kg").request().get();
		Response newSpot = target.path("/some-path/").request()
				.post(Entity.entity(new byte[1024], MediaType.APPLICATION_OCTET_STREAM));
		
		if (newSpot.hasEntity()) {
			String id = newSpot.readEntity(String.class);
			System.out.println("data resourse id: " + id);
		} else {
			System.err.println(newSpot.getStatus() + "\n------- \n");
			System.err.println(newSpot.getStatus());
		}
	}
	// muito parecido com o create
}
