package main.java.entities;


public class CommittedFile {

	private String name;		//file name
	private int size;			//loc
	private int locAdded;		//loc added
	private int locTouched;		//loc touched
	
	public CommittedFile(String name, int size, int locAdded, int locTouched) {
		this.name = name;
		this.size = size;
		this.locAdded = locAdded;
		this.locTouched = locTouched;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getLocAdded() {
		return locAdded;
	}

	public void setLocAdded(int locAdded) {
		this.locAdded = locAdded;
	}

	public int getLocTouched() {
		return locTouched;
	}

	public void setLocTouched(int locTouched) {
		this.locTouched = locTouched;
	}
	
}
