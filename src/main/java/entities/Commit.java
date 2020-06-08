package main.java.entities;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Commit {

	private String sha;
	private String message;
	private LocalDate date;
	private String author;
	private List<CommittedFile> files = null;
	private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd"); 
	
	public Commit(String sha, String message, String date, String author){
		
		this.sha = sha;
		this.message = message;
		this.date =  LocalDate.parse(date,formatter);
		this.author = author;
		
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

	public LocalDate getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date =  LocalDate.parse(date,formatter);
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public List<CommittedFile> getFiles() {
		return files;
	}

	public void setFiles(List<CommittedFile> files) {
		this.files = files;
	}
	
}
