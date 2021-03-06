package main.java;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import main.java.entities.Commit;
import main.java.entities.CommittedFile;
import main.java.entities.Release;
import main.java.entities.Ticket;

import org.json.JSONArray;

public class SearchInfo {
	
	
    private static Logger logger;
    private static final String DATEFORMAT = "yyyy-MM-dd"; 
	
    
    static {

        System.setProperty("java.util.logging.config.file", "logging.properties");
        logger = Logger.getLogger(SearchInfo.class.getName());
    }
    
    
    //Searches for all the tickets of type (attribute) specified which have been resolved/closed 
    public List<Ticket> findTickets(String project, String attribute) throws JSONException, IOException{
    	Integer j = 0;
    	Integer i = 0;
    	Integer k = 0;
    	Integer total = 1;
    	
    	DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATEFORMAT); 
    	
    	List<Ticket> tickets = new ArrayList<>();	//Creates a new list of tickets
		List<String> fixed = null;
		List<String> affected = null;
		JSONArray array = null;
		
    	JSONReader jr = new JSONReader();
	   
    	do {
    		//Only gets a max of 1000 at a time, so must do this multiple times if >1000
    		j = i + 1000;
         
    		String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22"
    				+ project + "%22AND%22issueType%22=%22"+attribute+"%22AND(%22status%22=%22closed%22OR"
    				+ "%22status%22=%22resolved%22)AND%22resolution%22=%22fixed%22&fields=key,resolutiondate,versions,fixVersions,created&startAt="
    				+ i.toString() + "&maxResults=" + j.toString();
         
    		JSONObject json = jr.readJsonFromUrl(url);
    		
    		JSONArray issues = json.getJSONArray("issues");
    		total = json.getInt("total");
         
    		for (; i < total && i < j; i++) {
        	 
    			JSONObject fields = issues.getJSONObject(i%1000).getJSONObject("fields");
    			
    			String key = issues.getJSONObject(i%1000).get("key").toString();
    			String date = fields.get("created").toString();
    			LocalDate formattedDate = LocalDate.parse(date.substring(0,10),formatter);
    			
    			//Fixed and affected versions
    			fixed = new ArrayList<>();			
    			array = fields.getJSONArray("fixVersions");
 
    			for(k=0; k < array.length(); k++) {
    				fixed.add(array.getJSONObject(k).get("name").toString());
    			}
    			
    			affected = new ArrayList<>();
    			array = fields.getJSONArray("versions");
    			
    			for(k=0; k<array.length(); k++) {
    				//Checks if the version is already in the fixed versions list
    				if(!fixed.contains(array.getJSONObject(k).get("name").toString())) {
        				affected.add(array.getJSONObject(k).get("name").toString());	
    				}
    			}
    			
    			Ticket t = new Ticket(key,formattedDate,fixed,affected);
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
	   
	   DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATEFORMAT); 
	   
	   while(true) {
		   //Only 100 commits per page are shown
		   String url = "https://api.github.com/repos/"+projectAuthor+"/"+project+"/commits?&per_page=100&page="+page;
	       
		   try{
	    	   comm = jr.readJsonArrayFromUrl(url,token);
	       }catch(Exception e) {
	    	   logger.severe(e.toString());
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
			   
			   LocalDate formattedDate = LocalDate.parse(date.substring(0,10),formatter);
			   
			   c = new Commit(sha,message,formattedDate,author);
			   
			   commits.add(c);	//Adds the new commit to the list
		            
		   }

		   page++;	//Going to the next page
		   		 
	   }  

	   Collections.sort(commits, (Commit o1, Commit o2) -> o1.getDate().compareTo(o2.getDate()));
	   
	   return commits;
	   
   }
   
   
   //Searches for all realeases of the specified project
   public List<Release> findReleases(String project) throws JSONException, IOException{
		
	   List<Release> releases = new ArrayList<>();
	   
	   DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATEFORMAT);
		
	   String url = "https://issues.apache.org/jira/rest/api/2/project/" + project;
	   JSONReader jr = new JSONReader();
	   
	   JSONObject json = jr.readJsonFromUrl(url);
	   JSONArray versions = json.getJSONArray("versions");
		
	   String date = null;
	   LocalDate formattedDate = null;
	   String name = null;
	   String id = null;
	   Release r = null;
		
	   for (int i = 0; i < versions.length(); i++ ) {

		   if(versions.getJSONObject(i).has("releaseDate")) {
			   if (versions.getJSONObject(i).has("name"))
				   name = versions.getJSONObject(i).get("name").toString();
			   if (versions.getJSONObject(i).has("id"))
				   id = versions.getJSONObject(i).get("id").toString();
				
			   date = versions.getJSONObject(i).get("releaseDate").toString();
			   formattedDate = LocalDate.parse(date,formatter);
			   
			   r = new Release(formattedDate,name,id);
				
			   releases.add(r);
		   }
	   }
		
	   Collections.sort(releases, (Release o1, Release o2) -> o1.getReleaseDate().compareTo(o2.getReleaseDate()));
		
	   return releases;
   }
   
   
   //Searches info of the committed files for a commit
   public List<CommittedFile> findCommittedFiles(String author, String project, String token, Commit commit) throws UnsupportedEncodingException {
	   
	   List<CommittedFile> fileList = null;
	   JSONObject comm = null; 
	   
	   String sha = commit.getSha();	//The commit identifier
	   
	   String url = "https://api.github.com/repos/"+author+"/"+project+"/commits/"+sha;
	   
	   JSONReader jr = new JSONReader();
	   
	   try{
    	   comm = jr.readJsonFromUrl(url,token);
       }catch(Exception e) {
    	   logger.severe(e.toString());
    	   return fileList;
       }
	   
	   JSONArray files = comm.getJSONArray("files");
	   fileList = new ArrayList<>();
	   CommittedFile newFile = null;
	   String filename = null;
	   int additions = 0;
	   int changes = 0;
	   int size = 0;
	   String content = null;
	   
	   for(int j=0; j<files.length(); j++) {
		   
		   filename = files.getJSONObject(j).get("filename").toString();
		   
		   //Only java files
		   if(filename.contains(".java")) {
			   
			   additions = files.getJSONObject(j).getInt("additions");
			   changes = files.getJSONObject(j).getInt("changes"); 
			   
			   url = files.getJSONObject(j).get("contents_url").toString();
			   
			   //Searches the content of the file
			   
			   try{
		    	   comm = jr.readJsonFromUrl(url,token);
		       }catch(Exception e) {
		    	   logger.severe(e.toString());
		    	   continue;
		       }
			   
			   content = comm.get("content").toString();
			   
			   byte[] byteArray = Base64.getMimeDecoder().decode(content);
			   content = new String(byteArray, StandardCharsets.UTF_8) + "\n";
			   
			   size = getLOC(content);
			   
			   newFile = new CommittedFile(filename,size,additions,changes);
			   
			   fileList.add(newFile);

		   }
		   
	   }
	   
	   return fileList; 
   }
   
   
   //Returns the number of LOC in a file
   public int getLOC(String content) {
   		
	   int loc = 0;
	   boolean multi = false;
   	
	   String[] lines = content.split("\n");	//splits the file content in lines
	   loc = lines.length;
   	
	   String line = null;
   	
	   for(int i=0; i<lines.length; i++) {
   	
		   line = lines[i];
   		
		   if(multi){
			   loc--;
			   
			   if(line.contains("*/")) {
				   multi = false;
			   }
		   }
		   else if(line.contains("//")) {   			
			   loc--;
		   }
		   else if(line.contains("/*")) {
			   loc--;
			   multi = true;
		   }
   			
	   }
   	
	   return loc;
   }
   
   
   //Matches commits to tickets
   public void matchCommits(List<Ticket> tickets, List<Commit> commits) {
	   String message = null;
	   int i;
	   
	   for(i=0;i<commits.size();i++) {
		   
		   message = commits.get(i).getMessage();
		   
		   for(int j=0;j<tickets.size();j++) {
			   
			   //Searches for the ticket's id in the commit's message
			   if(message.contains(tickets.get(j).getId()+":") || message.contains(tickets.get(j).getId()+"]") || message.contains(tickets.get(j).getId()+" ") || message.contains(" "+tickets.get(j).getId())) {
				   
				   tickets.get(j).setFixCommit(commits.get(i));				   
				   	break;
			   }
			   
		   }	   
		   
	   }   
	   
	   //Sorts the tickets
	   Collections.sort(tickets, (Ticket o1, Ticket o2) -> sortTickets(o1,o2));
	   
   }
   
  
   //Sorts the tickets
   public int sortTickets(Ticket o1, Ticket o2) {
	   if(o1.getResolutionDate() == null) {
		   return (o2.getResolutionDate() == null) ? 0 : 1;
	   }
	   else if (o2.getResolutionDate() == null) {
		   return -1;
	   }
	   else {
		   return o1.getResolutionDate().compareTo(o2.getResolutionDate());	
	   }
   }
   
}
