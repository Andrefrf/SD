package implementation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import api.storage.Namenode;

public class ImplementorName implements Namenode{
	
	Map<String, List<String>> storage; 

	
	public ImplementorName() {
		storage = new HashMap<String, List<String>>();
	}
	
	@Override
	public List<String> list(String prefix) {
		List<String> blocks = new ArrayList<>();
		for(String a: storage.keySet()) {
			if(a.substring(0, prefix.length()-1).equals(prefix)) {
				blocks.add(a);
			}
		}
		return blocks;
	}

	@Override
	public List<String> read(String name) {
		if(!storage.containsKey(name)) {
			throw new WebApplicationException(Status.NOT_FOUND);
		} else {
			return storage.get(name);	
		}
	}

	@Override
	public void create(String name, List<String> blocks) {
		
	}

	@Override
	public void update(String name, List<String> blocks) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete(String prefix) {
		// TODO Auto-generated method stub
		
	}

}
