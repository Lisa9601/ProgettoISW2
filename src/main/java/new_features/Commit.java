package main.java.new_features;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Commit {

	private String message;
	private LocalDateTime date;
	private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); 
	
	public Commit(String message, String date){
		
		this.message = message;
		this.date =  LocalDateTime.parse(date,formatter);
		
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
