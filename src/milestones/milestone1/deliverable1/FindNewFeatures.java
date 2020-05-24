package milestones.milestone1.deliverable1;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

public class FindNewFeatures {
	
	private String author = null;			//Name of the author of the project
	private String project = null;			//Name of the project to analyze
	private String token = null;			//token for github authorization
	
    private static final Logger LOGGER = Logger.getLogger(FindNewFeatures.class.getName());
    
    
    public FindNewFeatures(String author, String project, String token) {
    	
    	this.author = author;
    	this.project = project;
    	this.token = token;
    }
    
    private static String readAll(Reader rd) throws IOException {
    	StringBuilder sb = new StringBuilder();
    	int cp;
    	while ((cp = rd.read()) != -1) {
    		sb.append((char) cp);
    	}
    	return sb.toString();
    }

   
   public static JSONArray readJsonArrayFromUrl(String url, String token) throws IOException, JSONException {
	   URL url2 = new URL(url);
	   HttpURLConnection urlConnection = (HttpURLConnection)  url2.openConnection();
	   
	   //Setting the requirements to access the github api
	   urlConnection.setRequestProperty("Accept", "application/vnd.github.cloak-preview");
	   urlConnection.setRequestProperty("Authorization", "token "+token);

	   BufferedReader rd = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), StandardCharsets.UTF_8));
	   String jsonText = readAll(rd);
	   JSONArray json = new JSONArray(jsonText);
	  
	   urlConnection.disconnect();
	   
	   return json;
      
   }
   
   

   public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
	      InputStream is = new URL(url).openStream();
	      
	      BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
	      String jsonText = readAll(rd);
	      JSONObject json = new JSONObject(jsonText);
	      
	      is.close();
	      
	      return json;
	      
	}
   
   
   //Searches for all the tickets of type 'New Feature' which have been resolved/closed 
   public List<Ticket> findTickets() throws JSONException, IOException{

	   
	   Integer j = 0;
	   Integer i = 0;
	   Integer total = 1;
	   List<Ticket> tickets = new ArrayList<>();
	   
	      do {
	         //Only gets a max of 1000 at a time, so must do this multiple times if >1000
	         j = i + 1000;
	         
	         String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22"
	                + this.project + "%22AND%22issueType%22=%22New%20Feature%22AND(%22status%22=%22closed%22OR"
	                + "%22status%22=%22resolved%22)AND%22resolution%22=%22fixed%22&fields=key,resolutiondate,versions,created&startAt="
	                + i.toString() + "&maxResults=" + j.toString();
	         
	         JSONObject json = readJsonFromUrl(url);
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
   public List<Commit> findCommits() throws JSONException, IOException{
	   
	   int page = 1;
	   JSONArray comm = null;
	   List<Commit> commits = new ArrayList<>();
	   
	   while(true) {
		   String url = "https://api.github.com/repos/"+this.author+"/"+this.project+"/commits?&per_page=100&page="+page;
	       
		   try{
	    	   comm = readJsonArrayFromUrl(url,this.token);
	       }catch(Exception e) {
	    	   LOGGER.log(Level.SEVERE,"Exception occur ",e);
	    	   return commits;
	       }
	       
		  
	       Integer total = comm.length();
	       int i;
	       
	       if(total == 0) {
	    	   break;
	       }
	  
		   for (i=0; i < total; i++) {
		        	
			   JSONObject commit = comm.getJSONObject(i).getJSONObject("commit");
			   
			   String message = commit.get("message").toString();
			   String date = commit.getJSONObject("committer").get("date").toString();
			   
			   String formattedDate = date.substring(0,10)+" "+date.substring(11,19);
			   
			   Commit c = new Commit(message,formattedDate);
			   
			   commits.add(c);	//Adds the new commit to the list
		            
		   }

		   page++;	//Going to the next page
		   		 
	   }  

	   return commits;
	   
   }
   
   
   //Associating commits to tickets
   public static void sortCommits(List<Ticket> tickets, List<Commit> commits) {
	   
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
   
   
//--------------------------------------------------------------------------------------------------------------------------------
  
   public static void main(String[] args) throws JSONException, IOException {

		List<Ticket> tickets = null;	//List of tickets from the project
		List<Commit> commits = null;	//List of all the commits of the project
		
		PrintStream printer = null;
		
	   //Taking the configuration from config.json file
	   BufferedReader reader = new BufferedReader(new FileReader ("config.json"));
	   String config = readAll(reader);
	   JSONObject jsonConfig = new JSONObject(config);
	   
	   String author = jsonConfig.getString("author");
	   String project = jsonConfig.getString("project");
	   String token = jsonConfig.getString("token");
	   
	   reader.close();
	   
	   FindNewFeatures fd = new FindNewFeatures(author,project,token);
	   
	   LOGGER.info("Searching for tickets ...");
	   tickets = fd.findTickets();
	   LOGGER.info(tickets.size()+" tickets found!");

	   LOGGER.info("Searching for commits ...");
	   commits = fd.findCommits();
	   LOGGER.info(commits.size()+" commits found!");
	   
	   //Creating a new csv file with all the commits
	   printer = new PrintStream(new File("commits.csv"));
       printer.println("Date");

       LocalDateTime cd = null;
       Commit c = null;
       
       for(int i=0;i<commits.size();i++) {
    	   c = commits.get(i);
    	   cd = c.getDate();

    	   printer.println(cd.getDayOfMonth() + "/"+ cd.getMonthValue() +"/"+ cd.getYear());
       }
       
       printer.close();
	   
	   sortCommits(tickets,commits);
	   
	   //Creating a new csv file with all the tickets with at least one commit
	   printer = new PrintStream(new File("tickets.csv"));
       printer.println("Id,Date,Commits");

       LocalDateTime d = null;
       Ticket t = null;
       
       for(int i=0;i<tickets.size();i++) {
    	   t = tickets.get(i);
    	   d = t.getResolutionDate();
    	   
    	   if(d == null) {
    		   continue;
    	   }

    	   printer.println(t.getId() +","+ d.getDayOfMonth() + "/"+ d.getMonthValue() +"/"+ d.getYear() +","+ t.getCommitsNumber());
       }
       
       printer.close();
       
       LOGGER.info("DONE");
   }
	   
}
