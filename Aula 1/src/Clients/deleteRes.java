package Clients;

import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.client.ClientConfig;

public class deleteRes {

	public static void main(String[] args) {

		ClientConfig config = new ClientConfig();
		Client client = ClientBuilder.newClient(config);

		URI baseURI = UriBuilder.fromUri("http://localhost:9999/v1").build();
		WebTarget target = client.target(baseURI);

		Response response = target.path("/some-path/28s0pkung0").request().delete();
		{

			if (response.hasEntity()) {
				byte[] data = response.readEntity(byte[].class);
				System.out.println("data resource lenght: " + data.length);
			} else {
				System.err.println(response.getStatus());
			}
		}
	}
}
