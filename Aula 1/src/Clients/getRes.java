package Clients;

import java.net.URI;
import javax.ws.rs.core.*;
import javax.ws.rs.client.*;
import org.glassfish.jersey.client.*;

public class getRes {
	public static void main(String[] args) {

		ClientConfig config = new ClientConfig();
		Client client = ClientBuilder.newClient(config);

		URI baseURI = UriBuilder.fromUri("http://localhost:9999/v1").build();
		WebTarget target = client.target(baseURI);

		Response response = target.path("/some-path/4adsjj5d1g").request().get();
		{

			if (response.hasEntity()) {
				byte[] data = response.readEntity(byte[].class);
				System.out.println("data resource length: " + data.length);
			} else
				System.err.println(response.getStatus());

		}
	}
}