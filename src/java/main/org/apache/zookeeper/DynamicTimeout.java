package org.apache.zookeeper;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.zookeeper.proto.ReplyHeader;
import org.apache.zookeeper.proto.UpdateTimeout;

public class DynamicTimeout {
	private int sessionTimeout = -1;
	private ClientCnxn client;
	private final static int length = 10;
	private long[] history = new long[length];
	private volatile int p1 = 0;

	FileOutputStream out;
	PrintStream p;
	
	public DynamicTimeout(ClientCnxn client, int timeout){
		this.sessionTimeout = timeout;
		this.client = client;
		try{
			out = new FileOutputStream("/home/HBlog.d");
			p = new PrintStream(out);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	/**
	 * dynamic timeout algorithm
	 * @return new timeout value
	 */
	private int calcuateTimeout(){
		float new_avg_ping = 0, new_min_ping = 0, new_max_ping = 0;
		float ping_interval = (float) (sessionTimeout/3.0);
		new_avg_ping = avgOfResult();
		new_min_ping = minOfResult();
		new_max_ping = maxOfResult();
		System.out.println("AVG " + new_avg_ping);
		System.out.println("MIN " + new_min_ping);
		System.out.println("MAX " + new_max_ping);
		System.out.println("ping interval: " + ping_interval);
		
		if(Math.abs(new_avg_ping) > ping_interval*0.05 ||
				Math.abs(new_min_ping) > ping_interval*0.1 ||
				Math.abs(new_max_ping) > ping_interval*0.07)
		{
			return (int) (sessionTimeout + 10*(new_avg_ping+new_max_ping)/2);
		}
		return 0;
	}
	
	private float avgOfResult(){
		float t=0;
		for(int i=0; i<history.length; i++){
			t += history[i];
		}
		return t/history.length;
	}
	
	private float minOfResult(){
		float min = sessionTimeout*1000;
		for(int i=0; i<history.length; i++){
			if(history[i]<min)
				min = history[i];
		}
		return min;
	}
	
	private float maxOfResult(){
		float max = -sessionTimeout*1000;
		for(int i=0; i<history.length; i++){
			if(history[i]>max)
				max = history[i];
		}
		return max;
	}
	
	private synchronized void printResult(){
		if(history[length-1] == 0.0)
			return;
		for(int i =0; i<length-1; i++)
			System.out.print(history[i]+ " ");
		System.out.println();
	}

	public int addTohistory(long time){
		if(p1 == history.length){
			//trigger Dynamic timeout algorithm
			int nt = 0;
			if((nt = calcuateTimeout()) != 0){
				System.out.println("updata timeout: " + nt);
				updateSessionTimeout(nt);
			}
		}
		p1 = p1%length;
		history[p1] = time;
		System.out.println("delay: " + time + " ms");
		p1++;
		return p1;
	}
	
	private void updateSessionTimeout(int newtimeout){
		sessionTimeout = newtimeout;
		client.updateTimeout(newtimeout);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
