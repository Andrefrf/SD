package server;
import java.net.*;
import org.glassfish.jersey.server.*;

import implementation.DataResources;

import org.glassfish.jersey.jdkhttp.*;

public class RESTServer {

	public static void main(String[] args) {
		
		String URI_BASE = "http://0.0.0.0:9999/v1/";

		ResourceConfig config = new ResourceConfig();
		config.register( new DataResources() );

		JdkHttpServerFactory.createHttpServer( URI.create(URI_BASE), config);

		System.err.println("Server ready....");
	}
}
