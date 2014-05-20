package org.apache.zookeeper.proto;

import java.io.IOException;

import org.apache.jute.InputArchive;
import org.apache.jute.OutputArchive;
import org.apache.jute.Record;

public class NotifySpecialNode implements Record {
	public String specialNode;
	
	public NotifySpecialNode() {
		// TODO Auto-generated constructor stub
	}
	
	public NotifySpecialNode(String s) {
		// TODO Auto-generated constructor stub
		this.specialNode = s;
	}
	
	
	@Override
	public void serialize(OutputArchive archive, String tag) throws IOException {
		// TODO Auto-generated method stub
		archive.startRecord(this,tag);
		archive.writeString(specialNode,"specialNode");
		archive.endRecord(this,tag);
	}

	@Override
	public void deserialize(InputArchive archive, String tag)
			throws IOException {
		// TODO Auto-generated method stub
		archive.startRecord(tag);
		specialNode=archive.readString("specialNode");
	    archive.endRecord(tag);
	}

}
