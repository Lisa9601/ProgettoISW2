package main.java;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import main.java.entities.Commit;
import main.java.entities.Ticket;

public class GetMetrics {

	private String author = null;			//Name of the author of the project
	private String project = null;			//Name of the project to analyze
	private String attribute = null;		//Name of the attribute to search in the tickets
	private String token = null;			//Token for github authorization
	
    private static final Logger LOGGER = Logger.getLogger(FindAttribute.class.getName());
	
	public GetMetrics(String author, String project, String attribute, String token) {
		this.author = author;
		this.project = project;
		this.attribute = attribute;
		this.token = token;
	}
	
	
	
//-------------------------------------------------------------------------------------------------------------------------------------------------------	
	
	public static void main(String[] args) throws JSONException, IOException {
	 
		JSONReader jr = new JSONReader();
		
		List<Ticket> tickets = null;	//List of tickets from the project	
		List<Commit> commits = null;	//List of all the commits of the project
	   
	   
		//Taking the configuration from config.json file
		BufferedReader reader = new BufferedReader(new FileReader ("config.json"));
		String config = jr.readAll(reader);
		JSONObject jsonConfig = new JSONObject(config);
	   
		String author = jsonConfig.getString("author");
		String project = jsonConfig.getString("project");
		String attribute = jsonConfig.getString("attribute");
		String token = jsonConfig.getString("token");
	   
		reader.close();
	   
		//Searching for tickets and commits	
		FindAttribute fa = new FindAttribute(author,project,attribute,token);
		
		String url = "https://api.github.com/repos/"+ author+"/"+project+"/tags";
   
		JSONArray comm = null;
		
		try{
			comm = jr.readJsonArrayFromUrl(url,token);
		}catch(Exception e) {
			LOGGER.log(Level.SEVERE,"Exception occur ",e);
			return;
		}
		
		System.out.println(comm);
		
		
		LOGGER.info("Searching for tickets ...");
		tickets = fa.findTickets();
		LOGGER.info(tickets.size()+" tickets found!");
		
		LOGGER.info("Searching for commits ...");
		commits = fa.findCommits();
		LOGGER.info(commits.size()+" commits found!");
		
		LOGGER.info("Searching for committed files ...");
		fa.findCommittedFiles(commits);
		LOGGER.info("DONE");
		
		LOGGER.info("Matching commits to tickets ...");
		fa.matchCommits(tickets,commits);
		LOGGER.info("DONE");
		
		
   }
	
}
