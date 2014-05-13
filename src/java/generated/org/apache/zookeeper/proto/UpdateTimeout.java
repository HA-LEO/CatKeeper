package org.apache.zookeeper.proto;

import java.io.IOException;

import org.apache.jute.InputArchive;
import org.apache.jute.OutputArchive;
import org.apache.jute.Record;

public class UpdateTimeout implements Record {
	int id;
	long newTimeout;
	
	public UpdateTimeout(){
		
	}
	
	public UpdateTimeout(int id, long to){
		this.id = id;
		this.newTimeout = to;
	}
	
	public int getXid(){
		return id;
	}
	@Override
	public void serialize(OutputArchive archive, String tag) throws IOException {
		// TODO Auto-generated method stub
	
	}

	@Override
	public void deserialize(InputArchive archive, String tag)
			throws IOException {
		// TODO Auto-generated method stub

	}

}
