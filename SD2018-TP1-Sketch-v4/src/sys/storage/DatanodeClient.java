package sys.storage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import api.storage.Datanode;
import utils.Random;

/*
 * Fake Datanode client.
 * 
 * Rather than invoking the Datanode via REST, executes
 * operations locally, in memory.
 * 
 */
public class DatanodeClient implements Datanode {
	private static Logger logger = Logger.getLogger(Datanode.class.toString());

	private static final int INITIAL_SIZE = 32;
	private Map<String, byte[]> blocks = new HashMap<>(INITIAL_SIZE);

	public static void main(String[] args) throws IOException {
		
		final int MAX_DATAGRAM_SIZE = 65536;
		final InetAddress group = InetAddress.getByName(args[0]);
		if (!group.isMulticastAddress()) {
			System.out.println("Not a multicast address (use range : 224.0.0.0 -- 239.255.255.255)");
			System.exit(1);
		}
		try( MulticastSocket socket = new MulticastSocket( 9000 )) {
		    socket.joinGroup( group);
		    while( true ) {
		        byte[] buffer = new byte[MAX_DATAGRAM_SIZE] ;
		        DatagramPacket request = new DatagramPacket( buffer, buffer.length ) ;
		        socket.receive( request );
		        System.out.write( request.getData(), 0, request.getLength() ) ;
		        //prepare and send reply... (unicast)
		    }
		}
		
	}

	@Override
	public String createBlock(byte[] data) {
		String id = Random.key64();
		blocks.put(id, data);
		return id; // tbm tem que retornar uri dele (ip:porta/datanode/id)
	}

	@Override
	public void deleteBlock(String block) {
		if(!blocks.containsKey(block)){
			throw new WebApplicationException(Status.NOT_FOUND);
		}
		else {
			blocks.remove(block);
			throw new WebApplicationException(Status.GONE);// verificar se existe e retornar um status de http	
		}
	}

	@Override
	public byte[] readBlock(String block) {
		byte[] data = blocks.get(block);
		if (data != null)
			return data;
		else
			throw new WebApplicationException(Status.NOT_FOUND);// webservice status not found/content
	}
}
