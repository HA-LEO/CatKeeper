package org.apache.zookeeper.server;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;

import javax.sound.midi.SysexMessage;

import org.apache.zookeeper.proto.ReplyHeader;
import org.apache.zookeeper.proto.UpdateTimeout;

public class ZookeeperDynamicTimeout extends Thread{

	/**
	 * @param args
	 */
	private NIOServerCnxn nsc = null;
	private InetAddress clientAddr;
	private int init_ticktime = 200;
	private int init_sessionTimeout;
	private boolean getInit = false;
	private long sessionId = -1;
	private int sessionTimeout = -1;
	private float avg_ping = 0;
	private final static int length = 11;
	private long[] history = new long[length];
	private volatile int p1 = 0;
	private volatile boolean usingHistory = false;
	private float[] result = new float[length-1];
	private int p2 = 0;
	Timer timer;
	
	FileOutputStream out;
	PrintStream p;
	
	public ZookeeperDynamicTimeout(){
		try {
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
	
	public void setNIOServerCnxn(NIOServerCnxn n){
		this.nsc = n;
		this.sessionId = n.sessionId;
		this.clientAddr = n.getRemoteSocketAddress().getAddress();
		//timer = new Timer();
		//timer.schedule(new PING(this.clientAddr.getHostAddress()),5000,this.init_ticktime);
	}
	
	public class PING extends TimerTask{
		private String cmd;
		Runtime run = Runtime.getRuntime();
		Process pro;
		public PING(String addr){
			cmd = "fping -p10 -c2 -t1000 "+ addr;
		}
		
		@Override
		public void run(){
			try {
				pro = run.exec(this.cmd);
			    BufferedReader br = new BufferedReader(new InputStreamReader(pro.getInputStream()));
			    br.readLine();
			    String R = br.readLine();
			    String result;
			    float t = 1000;
			    try{
			    	result = R.substring(R.indexOf("bytes,")+7, R.indexOf("ms")-1);
			    	t = Float.parseFloat(result);
			    }catch (Exception e) {
				}
			    //addToResult(t);
			    p.print(t);
			    p.println();
			    br.close();
			    pro.destroy();
				//printResult();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void setSessionId(long id){
		this.sessionId = id;
	}
	
	private void updateSessionTimeout(int newtimeout){
		sessionTimeout = newtimeout;
		if(this.nsc.getSessionTracker().UpdateSession(sessionId, sessionTimeout) == 0){
			return;
		}
		this.nsc.setSessionTimeout(sessionTimeout);
		//notify client. xid set -100
		this.nsc.sendNewTimeout(new ReplyHeader(-100,0,0), new UpdateTimeout(sessionTimeout), "updateTimeout");
	}
	
	@Override
	public void run(){
		int newtimeout;
		while(true){
			try {
				sleep(5000);
				if(updateResult() == false)
					continue;
				if((newtimeout = calcuateTimeout()) != 0){
					System.out.println("update timeout value! \n");
					updateSessionTimeout(newtimeout);
				}
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * dynamic timeout algorithm
	 * @return new timeout value
	 */
	private int calcuateTimeout(){
		float new_avg_ping = 0;
		avg_ping = sessionTimeout/3;
		for(int i=0; i<length-1; i++){
			new_avg_ping += result[i];
		}
		new_avg_ping = new_avg_ping/(length-1);
		System.out.println("AVG " + new_avg_ping);
		System.out.println("ping interval: " + avg_ping);
		
		
/*		if(new_avg_ping > 2*avg_ping){
			avg_ping = new_avg_ping;
			return sessionTimeout*2;
		}
		if(new_avg_ping < avg_ping/2){
			avg_ping = new_avg_ping;
			return sessionTimeout/2;
		}*/
		return (int) (sessionTimeout+3*new_avg_ping);
	}
	
	private int addToResult(float time){
		p2 = p2%(length-1);
		result[p2] = time;
		p2++;
		return p2;
	}
	private synchronized void printResult(){
		if(result[length-2] == 0.0)
			return;
		for(int i =0; i<length-1; i++)
			System.out.print(result[i]+ " ");
		System.out.println();
	}

	public int addTohistory(long time){
		if(usingHistory == true){
			return -1;
		}
		p1 = p1%length;
		history[p1] = time;
		p1++;
		return p1;
	}
	private boolean updateResult(){
		if(getInit == false){
			if((init_sessionTimeout = nsc.getSessionTimeout()) == 0){
				return false;
			}
			sessionTimeout = init_sessionTimeout;
			this.init_ticktime = this.nsc.getZooKeeperServer().getTickTime();
			getInit = true;
		}
		usingHistory = true;
		int t = p1;
		float tmp;
		for(int i=1; i<length; i++){
			tmp = (float) (history[(t+i)%length]- history[(t+i-1)%length])/1000000;
			if(tmp != sessionTimeout/3 && tmp > 0){
				result[i-1] = (float)(Math.round((tmp - sessionTimeout/3)*1000))/1000;
				System.out.print(result[i-1] + "ms ");
				//log
				p.println(result[i-1]);
			}
		}
		System.out.println();
		usingHistory = false;
		return true;	
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		/*Timer timer = new Timer();
		PING p = new ZookeeperDynamicTimeout().new PING("192.168.10.100");
		timer.schedule(p,1000,1000);*/
		System.out.println(System.nanoTime());
		System.out.println(System.currentTimeMillis());
	}
}
