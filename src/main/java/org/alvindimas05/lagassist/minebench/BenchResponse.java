package org.alvindimas05.lagassist.minebench;

public class BenchResponse {

	private int singlethread;
	private int multithread;
    private int thread;
    private int cores;
	private boolean ok;

	public BenchResponse(int singlethread, int multithread, int thread, int cores, boolean ok) {
		this.singlethread = singlethread;
		this.multithread = multithread;
        this.thread = thread;
        this.cores = cores;
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

    public int getCores(){
        return cores;
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

    public String getStringifiedTh() {
        if (thread < 0) {
            return "Unknown Score";
        }
        return String.valueOf(thread);
    }

    public void setSinglethread(int singlethread) {
		this.singlethread = singlethread;
	}

}
