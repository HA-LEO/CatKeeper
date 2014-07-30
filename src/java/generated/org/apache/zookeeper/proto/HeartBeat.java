package org.apache.zookeeper.proto;

import java.io.IOException;

import org.apache.jute.InputArchive;
import org.apache.jute.OutputArchive;
import org.apache.jute.Record;

public class HeartBeat implements Record {
	public long seq;
	
	public HeartBeat(){
		
	}
	
	public HeartBeat(long to){
		this.seq = to;
	}
	
	@Override
	public void serialize(OutputArchive archive, String tag) throws IOException {
		// TODO Auto-generated method stub
		archive.startRecord(this,tag);
		archive.writeLong(seq,"newTimeout");
		archive.endRecord(this,tag);
	}

	@Override
	public void deserialize(InputArchive archive, String tag)
			throws IOException {
		// TODO Auto-generated method stub
		archive.startRecord(tag);
		seq=archive.readLong("newTimeout");
	    archive.endRecord(tag);
	}
}
