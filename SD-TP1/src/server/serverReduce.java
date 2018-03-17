package server;



import java.net.URI;
import java.net.URL;

import org.glassfish.jersey.jdkhttp.JdkHttpHandlerContainer;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import implementation.ImplementorData;

public class serverReduce {
	
	public static void main(String[] args) {
		
		String URI_BASE = "http://0.0.0.0:9999/v1/";
		
		ResourceConfig config = new ResourceConfig();
		config.register(new ImplementorData());
		
		JdkHttpServerFactory.createHttpServer( URI.create(URI_BASE), config);
		
		System.out.println("Server is ready...");
	}

}
