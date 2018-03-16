package Clients;

import java.net.URI;
import javax.ws.rs.core.*;
import javax.ws.rs.client.*;
import org.glassfish.jersey.client.*;

public class createRes {
	public static void main(String[] args) {

		ClientConfig config = new ClientConfig();
		Client client = ClientBuilder.newClient(config);

		URI baseURI = UriBuilder.fromUri("http://localhost:9999/v1").build();
		WebTarget target = client.target(baseURI);

		Response response = target.path("/some-path/").request()
				.post(Entity.entity(new byte[1024], MediaType.APPLICATION_OCTET_STREAM));
		{

			if (response.hasEntity()) {
				String id = response.readEntity(String.class);
				System.out.println("data resource id: " + id);
			} else
				System.err.println(response.getStatus());

		}
	}
}