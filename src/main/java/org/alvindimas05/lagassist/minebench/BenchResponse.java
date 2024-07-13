package org.alvindimas05.lagassist.minebench;

public class BenchResponse {

	private int singlethread;
	private int multithread;
	
	private boolean ok;

	public BenchResponse(int singlethread, int multithread, boolean ok) {
		this.singlethread = singlethread;
		this.multithread = multithread;
		this.ok = ok;
	}
	
	public boolean getOk() {
		return ok;
	}

	public int getMultithread() {
		return multithread;
	}

	public void setMultithread(int multithread) {
		this.multithread = multithread;
	}

	public int getSinglethread() {
		return singlethread;
	}

	public String getStringifiedSth() {
		if (singlethread < 0) {
			return "Unknown Score";
		}
		return String.valueOf(singlethread);
	}

	public String getStringifiedMth() {
		if (multithread < 0) {
			return "Unknown Score";
		}
		return String.valueOf(multithread);
	}

	public void setSinglethread(int singlethread) {
		this.singlethread = singlethread;
	}

}
