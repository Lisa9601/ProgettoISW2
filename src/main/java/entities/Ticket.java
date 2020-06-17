package main.java.entities;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Ticket {

	private String id;							//ticket's id
	private LocalDate date = null;				//creation date
	private LocalDate resolutionDate = null;	//date of the fix
	private List<String> fixed = null;			//fixed versions
	private List<String> affected = null;		//affected versions
	private Commit fixCommit = null; 			//commit of the fix
	private int numCommits;						//number of commits for the ticket

	
	public Ticket(String id, LocalDate date, List<String> fixed, List<String> affected) {

		this.id = id;
		this.date = date;
		this.fixed = fixed;
		this.affected = affected;
		this.numCommits = 0;
		
	}

	
	public String getId() {
		return id;
	}

	
	public void setId(String id) {
		this.id = id;
	}
	
	
	public LocalDate getDate() {
		return date;
	}

	
	public void setDate(LocalDate date) {
		this.date = date;
	}
	
	
	public void setResolutionDate(LocalDate resolutionDate) {
		this.resolutionDate = resolutionDate;		
	}

	
	public LocalDate getResolutionDate() {
		return this.resolutionDate;
	}

	
	public List<String> getFixed() {
		return fixed;
	}


	public void setFixed(List<String> fixed) {
		this.fixed = fixed;
	}

	
	//Removes a version from the list
	public void removeFixed(int i) {
		this.fixed.remove(i);
	}
	

	public List<String> getAffected() {
		return affected;
	}

	
	public void setAffected(List<String> affected) {
		this.affected = affected;
	}

	
	//Adds another affected version to the list
	public void addAffected(String affected) {
		if(this.affected == null) {
			this.affected = new ArrayList<>();
		}
		else if(!this.affected.contains(affected)) {
			this.affected.add(affected);
		}

	}
	
	
	//Removes the affected version from the list
	public void removeAffected(int i) {
		this.affected.remove(i);
	}
	
	
	public Commit getFixCommit() {
		return fixCommit;
	}

	
	public void setFixCommit(Commit fixCommit) {
		
		this.numCommits++;	//Counts the new commit
		
		LocalDate newDate = fixCommit.getDate();
		
		//If there's no resolutionDate or this one is greater then the one we have the value is changed
		if( this.resolutionDate == null || this.resolutionDate.compareTo(newDate) < 0) {
			setResolutionDate(newDate);
			this.fixCommit = fixCommit;
		}
	
	}

	
	public int getNumCommits() {
		return numCommits;
	}
	
	
	public void setNumCommits(int commits) {
		this.numCommits = commits;
	}
	
}
