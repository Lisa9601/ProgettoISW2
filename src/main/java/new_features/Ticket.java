package main.java.new_features;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Ticket {

	private String id;	
	private List<Commit> commits;
	private LocalDateTime resolutionDate = null;
	
	
	public Ticket(String id) {

		this.id = id;
		this.commits = new ArrayList<>();
		
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
	
	public LocalDateTime getResolutionDate() {
	
		if(resolutionDate == null) {
			
			LocalDateTime temp = null;

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
	
}
