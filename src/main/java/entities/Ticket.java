package main.java.entities;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Ticket {

	private String id;	
	private List<Commit> commits = null;
	private LocalDate resolutionDate = null;
	private List<String> affected = null;
	
	
	public Ticket(String id, List<String> affected) {

		this.id = id;
		this.affected = affected;
		commits = new ArrayList<>();
		
	}

	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}

	public List<Commit> getCommits() {
		return commits;
	}

	public void setCommits(List<Commit> commits) {
		this.commits = commits;
	}
	
	public void addCommit(Commit c) {
		commits.add(c);
	}
	
	public LocalDate getResolutionDate() {
	
		if(resolutionDate == null) {
			
			LocalDate temp = null;

			for(int i=0;i<commits.size();i++) {
				
				temp = commits.get(i).getDate();
				
				if(resolutionDate == null || resolutionDate.compareTo(commits.get(i).getDate()) < 0) {
					resolutionDate = temp;
				}
				
			}
		}

		return resolutionDate;
	}

	
	public int getCommitsNumber() {
		
		return commits.size();
	}

	public List<String> getAffected() {
		return affected;
	}

	public void setAffected(List<String> affected) {
		this.affected = affected;
	}
	
}
