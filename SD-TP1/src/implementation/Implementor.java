package implementation;

import java.util.List;

public class Implementor implements api.storage.Datanode, api.storage.Namenode {

	@Override
	public List<String> list(String prefix) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> read(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void create(String name, List<String> blocks) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(String name, List<String> blocks) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete(String prefix) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String createBlock(byte[] data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteBlock(String block) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public byte[] readBlock(String block) {
		// TODO Auto-generated method stub
		return null;
	}
	
	//implementar o api.mapreduce sendo primeiro os datanodes e name nodes

}
