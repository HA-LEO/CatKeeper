package org.apache.zookeeper.proto;

import java.io.IOException;

import org.apache.jute.InputArchive;
import org.apache.jute.OutputArchive;
import org.apache.jute.Record;

public class UpdateTimeout implements Record {
	public int newTimeout;
	
	public UpdateTimeout(){
		
	}
	
	public UpdateTimeout(int to){
		this.newTimeout = to;
	}
	
	@Override
	public void serialize(OutputArchive archive, String tag) throws IOException {
		// TODO Auto-generated method stub
		archive.startRecord(this,tag);
		archive.writeInt(newTimeout,"newTimeout");
		archive.endRecord(this,tag);
	}

	@Override
	public void deserialize(InputArchive archive, String tag)
			throws IOException {
		// TODO Auto-generated method stub
		archive.startRecord(tag);
		newTimeout=archive.readInt("newTimeout");
	    archive.endRecord(tag);
	}

}
