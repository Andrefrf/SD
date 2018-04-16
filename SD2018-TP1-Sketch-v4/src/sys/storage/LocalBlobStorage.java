package sys.storage;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import api.storage.*;
import sys.Multicast.Multicast;
import sys.storage.io.*;

public class LocalBlobStorage implements BlobStorage {
	private static final int BLOCK_SIZE = 512;

	static NamenodeClient namenode;
	static ConcurrentHashMap<URI, Datanode> datanodes;

	public LocalBlobStorage() throws IOException, InterruptedException {
		namenode = null;

		datanodes = new ConcurrentHashMap<>();

		Multicast multi = new Multicast();

		URI nameURI = null;

		while (namenode == null) {
			Set<URI> uri = multi.send("Namenode");
			
			if(uri.size()==0) {
				continue;
			}
			nameURI = uri.iterator().next();
			
			namenode = new NamenodeClient(nameURI);
		}
		Set<URI> uri = multi.send("Datanode");
		for (URI dataURI : uri) {
			datanodes.put(dataURI, new DatanodeClient(dataURI));
		}
	}

	@Override
	public List<String> listBlobs(String prefix) {
		return namenode.list(prefix);
	}

	@SuppressWarnings("unlikely-arg-type")
	@Override
	public void deleteBlobs(String prefix) {
		namenode.list(prefix).forEach(blob -> {
			namenode.read(blob).forEach(block -> {
				datanodes.get(blob).deleteBlock(block);
			});
		});
		namenode.delete(prefix);
	}

	@Override
	public BlobReader readBlob(String name) {
		HashMap<String, Datanode> d = null;
		return new BufferedBlobReader(name, namenode, d);
	}

	@Override
	public BlobWriter blobWriter(String name) {
		Datanode[] d = datanodes.values().toArray(new Datanode[datanodes.keySet().size()]);
		return new BufferedBlobWriter(name, namenode, d, BLOCK_SIZE);
	}
}
