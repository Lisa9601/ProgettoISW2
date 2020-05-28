package main.java.common;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Commit {

	private String sha;
	private String message;
	private LocalDateTime date;
	private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); 
	
	public Commit(String sha, String message, String date){
		
		this.sha = sha;
		this.message = message;
		this.date =  LocalDateTime.parse(date,formatter);
		
	}

	public String getSha() {
		return sha;
	}

	public void setSha(String sha) {
		this.sha = sha;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public LocalDateTime getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date =  LocalDateTime.parse(date,formatter);
	}
	
}
