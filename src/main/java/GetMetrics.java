package main.java;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import main.java.entities.Commit;
import main.java.entities.Ticket;

public class GetMetrics {

    private static Logger logger;
	
    static {

        System.setProperty("java.util.logging.config.file", "logging.properties");
        logger = Logger.getLogger(SearchInfo.class.getName());
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
		
		logger.info("Searching for tickets ...");
		tickets = search.findTickets(project,attribute);
		logger.info(tickets.size()+" tickets found!");
		
		logger.info("Searching for commits ...");
		commits = search.findCommits(author,project,token);
		logger.info(commits.size()+" commits found!");
		
		logger.info("Searching for committed files ...");
		search.findCommittedFiles(author,project,token,commits);
		logger.info("DONE");
		
		logger.info("Matching commits to tickets ...");
		search.matchCommits(tickets,commits);
		logger.info("DONE");
		
		
   }
	
}
