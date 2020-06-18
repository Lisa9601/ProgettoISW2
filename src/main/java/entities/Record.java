package main.java.entities;

import java.util.ArrayList;
import java.util.List;

public class Record {

	private Integer release;		//release number
	private String file;			//file name
	private int size;				//loc
	private int locTouched;			//loc touched
	private int locAdded;			//loc added
	private int maxLocAdded;		//maximum loc added
	private List<String> authors;	//list of authors of the file
	private int nfix;				//number of fix
	private int nr;					//number of revisions
	private int chgSetSize;			//change set size
	private String buggy;			//bugginess
	
	
	public Record(int release, String file) {

		this.release = release;
		this.file = file;
		this.size = 0;
		this.locTouched = 0;
		this.locAdded = 0;
		this.maxLocAdded = 0;
		this.authors = new ArrayList<>();
		this.nfix = 0;
		this.nr = 0;
		this.chgSetSize = 0;
		this.buggy = "No";
	}
	

	public Integer getRelease() {
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
	
	
	//Adds loc to the number of loc touched
	public void addLocTouched(int loc) {
		this.locTouched += loc;
	}


	public int getLocAdded() {
		return locAdded;
	}


	public void setLocAdded(int locAdded) {
		this.locAdded = locAdded;
	}

	
	//Adds loc to the number of loc added
	public void addLocAdded(int loc) {
		this.locAdded += loc;
		
		//Updates the value of the max loc added
		if(this.maxLocAdded < loc) {
			this.maxLocAdded = loc;
		}
		
	}
	

	public int getMaxLocAdded() {
		return maxLocAdded;
	}


	public void setMaxLocAdded(int maxLocAdded) {
		this.maxLocAdded = maxLocAdded;
	}


	//Returns the average number of loc added
	public float getAvgLocAdded() {
		
		float avg;
		
		if(this.locAdded == 0) {
			avg = 0; 
		}
		else {
			avg = (float)this.locAdded/this.nr;
		}
		return avg;
	}
	

	public List<String> getAuthors() {
		return authors;
	}


	public void setAuthors(List<String> authors) {
		this.authors = authors;
	}

	
	//Adds an author to the list
	public void addAuthor(String author) {

		if(!this.authors.contains(author)) {
			this.authors.add(author);
		}
		
	}
	

	//Returns the number of authors for that file
	public int getNauth() {
		return authors.size();
	}
	

	public int getNfix() {
		return nfix;
	}


	public void setNfix(int nfix) {
		this.nfix = nfix;
	}
	
	
	//Adds a fix
	public void addFix() {
		this.nfix++;
	}


	public int getNr() {
		return nr;
	}


	public void setNr(int nr) {
		this.nr = nr;
	}
	
	
	//Adds a revision
	public void addRevision() {
		this.nr++;
	}


	public int getChgSetSize() {
		return chgSetSize;
	}


	public void setChgSetSize(int chgSetSize) {
		this.chgSetSize = chgSetSize;
	}
	
	
	//Adds to the change set size the new number of files committed together
	public void addChgSetSize(int num) {
		this.chgSetSize += num;
	}


	public String getBuggy() {
		return buggy;
	}


	public void setBuggy(String buggy) {
		this.buggy = buggy;
	}
	
	
	
}
