package sys.comunication;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.URI;
import javax.ws.rs.core.UriBuilder;

public class Finder {
	
	DatagramPacket request;
	static final int MAX_DATAGRAM_SIZE = 65536;
	

	public static URI discovery(String type) throws IOException {
		URI u = null;
		final int port = 9000 ;
		final InetAddress group = InetAddress.getByName("225.9.0.1") ;

		if( ! group.isMulticastAddress()) {
		    System.out.println( "Not a multicast address (use range : 224.0.0.0 -- 239.255.255.255)");
		}

		byte[] data = type.getBytes();
		try(MulticastSocket socket = new MulticastSocket(9200)) {
			socket.joinGroup(group);
			DatagramPacket request = new DatagramPacket( data, data.length, group, port ) ;
			socket.send( request );
		    
		    byte[] buffer = new byte[MAX_DATAGRAM_SIZE] ;
	        request = new DatagramPacket( buffer, buffer.length ) ;
	        socket.receive(request);
	        String uri = new String(request.getData(),"UTF-8").trim();
	        System.out.println(uri);
	        u = UriBuilder.fromUri(uri).build();
		}
		return u;
	}

}
