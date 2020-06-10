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

    private static Logger LOGGER;
	
    static {

        System.setProperty("java.util.logging.config.file", "logging.properties");
        LOGGER = Logger.getLogger(SearchInfo.class.getName());
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
		SearchInfo search = new SearchInfo();
		
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
		tickets = search.findTickets(project,attribute);
		LOGGER.info(tickets.size()+" tickets found!");
		
		LOGGER.info("Searching for commits ...");
		commits = search.findCommits(author,project,token);
		LOGGER.info(commits.size()+" commits found!");
		
		LOGGER.info("Searching for committed files ...");
		search.findCommittedFiles(author,project,token,commits);
		LOGGER.info("DONE");
		
		LOGGER.info("Matching commits to tickets ...");
		search.matchCommits(tickets,commits);
		LOGGER.info("DONE");
		
		
   }
	
}
