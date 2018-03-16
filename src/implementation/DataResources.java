package implementation;

import java.util.*;
import java.util.concurrent.*;

import javax.ws.rs.*;
import javax.ws.rs.core.Response.*;

public class DataResources implements api.DataResources {
    
    private static final int ISIZE = 128;
    private final Map<String, byte[]> storage;

    public DataResources() {
        this.storage = new ConcurrentHashMap<>(ISIZE);
    }
    
    public String store(byte[] data) {
        String id = Long.toString( System.nanoTime(), 32);
        if( storage.putIfAbsent(id, data) != null)
            throw new WebApplicationException( Status.CONFLICT );
        else
            return id;
    }
    
    @Override
	public byte[] download(String id) {
		
    	if(!storage.containsKey(id)) {
			throw new WebApplicationException( Status.NOT_FOUND);
		}
    	
		return storage.get(id);
	}

	@Override
	public byte[] change(String id, byte[] data) {
		if(!storage.containsKey(id)) {
			storage.put(id, data);
		}
		throw new WebApplicationException( Status.NOT_FOUND);
	}

	@Override
	public void delete(String id) {
		if(storage.containsKey(id)) {
			storage.remove(id);
		}
		throw new WebApplicationException(Status.NOT_FOUND);
	}
    
    
	
}