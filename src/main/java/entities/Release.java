package main.java.entities;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Release {

	private LocalDate releaseDate;
	private String name;
	private String id;
	
	private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	
	public Release(String releaseDate, String name, String id) {
		
		this.releaseDate = LocalDate.parse(releaseDate,formatter);
		this.name = name;
		this.id = id;
	
	}
	
	public LocalDate getReleaseDate() {
		return releaseDate;
	}
	public void setReleaseDate(String releaseDate) {
		this.releaseDate = LocalDate.parse(releaseDate,formatter);
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
}
