package main.java.entities;


public class CommittedFile {

	private String name;
	private int size;
	private int locAdded;
	private int locTouched;
	private String content;
	
	public CommittedFile(String name, int size, int locAdded, int locTouched, String content) {
		this.name = name;
		this.size = size;
		this.locAdded = locAdded;
		this.locTouched = locTouched;
		this.content = content;
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

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
}
