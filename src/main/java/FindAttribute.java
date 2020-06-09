package main.java;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import main.java.entities.Commit;
import main.java.entities.CommittedFile;
import main.java.entities.Ticket;

import org.json.JSONArray;

public class FindAttribute {
	

    //private static final Logger LOGGER = Logger.getLogger(FindAttribute.class.getName());
    private static Logger LOGGER;
	
    static {

        System.setProperty("java.util.logging.config.file", "logging.properties");
        LOGGER = Logger.getLogger(FindAttribute.class.getName());
    }
    
    
   //Searches for all the tickets of type specified which have been resolved/closed 
   public List<Ticket> findTickets(String project, String attribute) throws JSONException, IOException{

	   Integer j = 0;
	   Integer i = 0;
	   Integer total = 1;
	   List<Ticket> tickets = new ArrayList<>();	//Creates a new list of tickets
	   JSONReader jr = new JSONReader();
	   
	      do {
	         //Only gets a max of 1000 at a time, so must do this multiple times if >1000
	         j = i + 1000;
	         
	         String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22"
	                + project + "%22AND%22issueType%22=%22"+attribute+"%22AND(%22status%22=%22closed%22OR"
	                + "%22status%22=%22resolved%22)AND%22resolution%22=%22fixed%22&fields=key,resolutiondate,versions,created&startAt="
	                + i.toString() + "&maxResults=" + j.toString();
	         
	         JSONObject json = jr.readJsonFromUrl(url);
	         JSONArray issues = json.getJSONArray("issues");
	         total = json.getInt("total");
	         
	         for (; i < total && i < j; i++) {
	        	 
	        	 String key = issues.getJSONObject(i%1000).get("key").toString();
	        	 
	        	 Ticket t = new Ticket(key);
	        	 tickets.add(t);	//Adds the new ticket to the list
	            
	         }
	         
	      } while (i < total);
	      
	      return tickets;
   }
   
   
   //Searches for all the commits in the github repository of the project
   public List<Commit> findCommits(String projectAuthor, String project, String token) throws JSONException, IOException{
	   
	   int page = 1;
	   JSONArray comm = null;
	   Commit c = null;
	   List<Commit> commits = new ArrayList<>();	//Creates a new list of commits
	   JSONReader jr = new JSONReader();
	   
	   while(true) {
		   //Only 100 commits per page are shown
		   String url = "https://api.github.com/repos/"+projectAuthor+"/"+project+"/commits?&per_page=100&page="+page;
	       
		   try{
	    	   comm = jr.readJsonArrayFromUrl(url,token);
	       }catch(Exception e) {
	    	   LOGGER.log(Level.SEVERE,"Exception occur ",e);
	    	   return commits;
	       }
		  
	       Integer total = comm.length();
	       int i;
	       
	       if(total == 0) {
	    	   break;	//If no commits have been found exits the cycle
	       }
	  
		   for (i=0; i < total; i++) {
		       
			   String sha = comm.getJSONObject(i).get("sha").toString();
			   
			   JSONObject commit = comm.getJSONObject(i).getJSONObject("commit");			   
			   String message = commit.get("message").toString();
			   String date = commit.getJSONObject("committer").get("date").toString();
			   String author = commit.getJSONObject("author").get("name").toString();
			   String formattedDate = date.substring(0,10);
			   
			   c = new Commit(sha,message,formattedDate,author);
			   
			   commits.add(c);	//Adds the new commit to the list
		            
		   }

		   page++;	//Going to the next page
		   		 
	   }  

	   return commits;
	   
   }
   
   
   //Matches commits to tickets
   public void matchCommits(List<Ticket> tickets, List<Commit> commits) {
	   
	   String message = null;
	   
	   for(int i=0;i<commits.size();i++) {
		   
		   message = commits.get(i).getMessage();
		   
		   for(int j=0;j<tickets.size();j++) {
			   
			   if(message.contains(tickets.get(j).getId()+":") || message.contains(tickets.get(j).getId()+"]")) {	//If a ticket is found in the message that commit is added to the list
				   
				   tickets.get(j).addCommit(commits.get(i));
				   break;
			   }
			   
		   }	   
		   
	   }
	   
   } 
   
   
   //Searches info of the committed files for each commit
   public void findCommittedFiles(String author, String project, String token, List<Commit> commits) {
	   
	   JSONReader jr = new JSONReader();
	   String sha = null;
	   String url = null;
	   Commit commit = null;
	   JSONObject comm = null;
	   
	   for(int i=0; i< commits.size(); i++) {
		   
		   commit = commits.get(i);
		   sha = commit.getSha();
		   
		   LOGGER.info("searching files for commit "+i+"/"+commits.size());	//Added to keep track of the number of commits processed
		   
		   url = "https://api.github.com/repos/"+author+"/"+project+"/commits/"+sha;

		   try{
	    	   comm = jr.readJsonFromUrl(url,token);
	       }catch(Exception e) {
	    	   LOGGER.log(Level.SEVERE,"Exception occur ",e);
	    	   break;
	       }
		   
		   JSONArray files = comm.getJSONArray("files");
		   List<CommittedFile> fileList = new ArrayList<>();
		   CommittedFile newFile = null;
		   String filename = null;
		   int additions = 0;
		   int changes = 0;
		   int size = 0;
		   String content = null;
		   
		   for(int j=0; j<files.length(); j++) {
			   
			   filename = files.getJSONObject(j).get("filename").toString();
			   additions = files.getJSONObject(j).getInt("additions");
			   changes = files.getJSONObject(j).getInt("changes");
			   
			   url = files.getJSONObject(j).get("contents_url").toString();
			   
			   try{
		    	   comm = jr.readJsonFromUrl(url,token);
		       }catch(Exception e) {
		    	   LOGGER.log(Level.SEVERE,"Exception occur ",e);
		    	   break;
		       }
			   
			   size = comm.getInt("size");
			   content = comm.get("content").toString();
			   
			   newFile = new CommittedFile(filename,size,additions,changes,content);
			   
			   fileList.add(newFile);
		   }
		   
		   commit.setFiles(fileList);
		   
	   }
	   
   }
   
   
   //Writes the tickets info in a csv file
   public void writeTickets(String project, List<Ticket> tickets) throws FileNotFoundException {
	   
       LocalDate d = null;
       Ticket t = null;
       String date = null;
       
	   String output = "results/" + project + "tickets.csv";
	   PrintStream printer = new PrintStream(new File(output));
       
       printer.println("Id,Date,Num Commits");
       
       for(int i=0;i<tickets.size();i++) {
    	   t = tickets.get(i);
    	   d = t.getResolutionDate();
    	   
    	   if(d == null) {
    		   date = "null";
    	   }
    	   else {
    		   date = d.getMonthValue() +"/"+ d.getYear();
    	   }

    	   printer.println(t.getId() +","+ date +","+ t.getCommitsNumber());
       }
       
       printer.close();
	   
   }
   
   
   //Writes the commits info in a csv file
   public void writeCommits(String project, List<Commit> commits) throws FileNotFoundException {
	   
	   String output = "results/" + project + "commits.csv";
	   
	   PrintStream printer = new PrintStream(new File(output));

       LocalDate cd = null;
       Commit c = null;
       
       for(int i=0;i<commits.size();i++) {
    	   c = commits.get(i);
    	   cd = c.getDate();

    	   printer.println(cd.getMonthValue() +"/"+ cd.getYear());
       }
       
       printer.close();
	   
   }
   
//--------------------------------------------------------------------------------------------------------------------------------
  
   public static void main(String[] args) throws JSONException, IOException {

		List<Ticket> tickets = null;	//List of tickets from the project
		List<Commit> commits = null;	//List of all the commits of the project
		JSONReader jr = new JSONReader();
		
	   //Taking the configuration from config.json file
	   BufferedReader reader = new BufferedReader(new FileReader ("config.json"));
	   String config = jr.readAll(reader);
	   JSONObject jsonConfig = new JSONObject(config);
	   
	   String author = jsonConfig.getString("author");
	   String project = jsonConfig.getString("project");
	   String attribute = jsonConfig.getString("attribute");
	   String token = jsonConfig.getString("token");
	   
	   reader.close();
	   
	   FindAttribute fa = new FindAttribute();
	   
	   LOGGER.info("Searching for tickets ...");
	   tickets = fa.findTickets(project,attribute);
	   LOGGER.info(tickets.size()+" tickets found!");

	   LOGGER.info("Searching for commits ...");
	   commits = fa.findCommits(author,project,token);
	   LOGGER.info(commits.size()+" commits found!");
	   
	   //Creating a new csv file with all the commits
	   fa.writeCommits(project,commits);
	   
	   fa.matchCommits(tickets,commits);
	   
	   //Creating a new csv file with all the tickets with at least one commit
       fa.writeTickets(project,tickets);
	   
       LOGGER.info("DONE");
   }
	   
}
