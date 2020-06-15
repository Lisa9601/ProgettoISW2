package main.java.entities;

import java.time.LocalDate;

public class Release {

	private LocalDate releaseDate;		//date of the release
	private String name;				//name of the release
	private String id;					//id of the release
	
	public Release(LocalDate releaseDate, String name, String id) {
		
		this.releaseDate = releaseDate;
		this.name = name;
		this.id = id;
	
	}
	
	public LocalDate getReleaseDate() {
		return releaseDate;
	}
	public void setReleaseDate(LocalDate releaseDate) {
		this.releaseDate = releaseDate;
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
