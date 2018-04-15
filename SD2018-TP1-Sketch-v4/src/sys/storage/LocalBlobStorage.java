package sys.storage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import api.storage.*;
import sys.comunication.Finder;
import sys.storage.io.*;

public class LocalBlobStorage implements BlobStorage {
	private static final int BLOCK_SIZE = 512;

	static NamenodeClient namenode;
	static ConcurrentHashMap<URI, Datanode> datanodes;

	public LocalBlobStorage() {

		namenode = null;
		datanodes = new ConcurrentHashMap<>();

		int i = 0;
		while (namenode == null) {

			URI nameURI = null;
			try {
				// nameURI = Finder.discovery("Namenode");
				nameURI = descoberta("Namenode");
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("Interations: " + i);
			namenode = new NamenodeClient(nameURI);
		}

		Thread t = new Thread(finder);
		t.start();

	}

	private URI descoberta(String string) throws IOException {

		final int port = 9000;
		final InetAddress group = InetAddress.getByName("225.9.0.1");
		URI uri = null;
		if (!group.isMulticastAddress()) {
			System.out.println("Not a multicast address (use range : 224.0.0.0 -- 239.255.255.255)");
		}

		byte[] data = "hello?".getBytes();
		try (MulticastSocket socket = new MulticastSocket()) {
			DatagramPacket request = new DatagramPacket(data, data.length, group, port);
			socket.send(request);
			
			byte[]buf = new byte[65536];
			request = new DatagramPacket(buf, buf.length);
			socket.receive(request);
			
			String result = new String(request.getData(),0,request.getLength());
			System.out.println(result);
			uri = URI.create(result);
		}
		catch(Exception e) {
			e.getStackTrace();
		}
		return uri;
	}

	Runnable finder = new Runnable() {

		public void run() {
			try {
				URI dataURI = Finder.discovery("Datanode");
				datanodes.put(dataURI, new DatanodeClient(dataURI));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	};

	@Override
	public List<String> listBlobs(String prefix) {
		return namenode.list(prefix);
	}

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
