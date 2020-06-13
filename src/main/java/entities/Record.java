package main.java.entities;

public class Record {

	private int release;
	private String file;
	private int size;
	private int locTouched;
	private int locAdded;
	private int maxLocAdded;
	private int AvgLocAdded;
	private int nauth;
	private int nfix;
	private int nr;
	private int chgSetSize;
	
	
	public Record(int release, String file, int size, int locTouched, int locAdded, int maxLocAdded, int avgLocAdded,
			int nauth, int nfix, int nr, int chgSetSize) {

		this.release = release;
		this.file = file;
		this.size = size;
		this.locTouched = locTouched;
		this.locAdded = locAdded;
		this.maxLocAdded = maxLocAdded;
		AvgLocAdded = avgLocAdded;
		this.nauth = nauth;
		this.nfix = nfix;
		this.nr = nr;
		this.chgSetSize = chgSetSize;
	}
	

	public int getRelease() {
		return release;
	}


	public void setRelease(int release) {
		this.release = release;
	}


	public String getFile() {
		return file;
	}


	public void setFile(String file) {
		this.file = file;
	}


	public int getSize() {
		return size;
	}


	public void setSize(int size) {
		this.size = size;
	}


	public int getLocTouched() {
		return locTouched;
	}


	public void setLocTouched(int locTouched) {
		this.locTouched = locTouched;
	}


	public int getLocAdded() {
		return locAdded;
	}


	public void setLocAdded(int locAdded) {
		this.locAdded = locAdded;
	}


	public int getMaxLocAdded() {
		return maxLocAdded;
	}


	public void setMaxLocAdded(int maxLocAdded) {
		this.maxLocAdded = maxLocAdded;
	}


	public int getAvgLocAdded() {
		return AvgLocAdded;
	}


	public void setAvgLocAdded(int avgLocAdded) {
		AvgLocAdded = avgLocAdded;
	}


	public int getNauth() {
		return nauth;
	}


	public void setNauth(int nauth) {
		this.nauth = nauth;
	}


	public int getNfix() {
		return nfix;
	}


	public void setNfix(int nfix) {
		this.nfix = nfix;
	}


	public int getNr() {
		return nr;
	}


	public void setNr(int nr) {
		this.nr = nr;
	}


	public int getChgSetSize() {
		return chgSetSize;
	}


	public void setChgSetSize(int chgSetSize) {
		this.chgSetSize = chgSetSize;
	}
	
}
