package implementation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

public class ImplementorData implements api.storage.Datanode{
	
	private final Map<String, byte[]> storage;
	
	public ImplementorData() {
		storage = new ConcurrentHashMap<String, byte[]>();
	}

	@Override
	public String createBlock(byte[] data) {
		String id = Long.toString(System.nanoTime(), 32);
		if(storage.putIfAbsent(id, data) != null ) {
			throw new WebApplicationException(Status.CONFLICT);
		}
		else {
			return id;
		}
	}

	@Override
	public void deleteBlock(String block) {

		if(storage.containsKey(block)) {
			storage.remove(block);
		}
		else {
			throw new WebApplicationException(Status.NOT_FOUND);
		}
	}

	@Override
	public byte[] readBlock(String block) {
		if(!storage.containsKey(block)) {
			throw new WebApplicationException(Status.NOT_FOUND);
		}
		return storage.get(block);
	}

}
