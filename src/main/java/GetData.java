package main.java;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import main.java.entities.Commit;
import main.java.entities.CommittedFile;
import main.java.entities.Record;
import main.java.entities.Release;
import main.java.entities.Ticket;

public class GetData {

    private static Logger logger;
	
    static {

        System.setProperty("java.util.logging.config.file", "logging.properties");
        logger = Logger.getLogger(SearchInfo.class.getName());
    }
	    	
    
    //Returns the number of LOC in a file
    public int getLOC(CommittedFile file) {
    	
    	int loc = 0;
    	String content = file.getContent();
    	
    	String[] lines = content.split("\n");
    	loc = lines.length;
    	
    	String line = null;
    	
    	for(int i=0; i<lines.length; i++) {
    		
    		line = lines[i];
    		
    		if(line.contains("//")) {   			
    			loc--;
    		}
    		else if(line.contains("/*")) {
    			do {
    				loc--;
    				i++;
    				line = lines[i];
    				
    			}
    			while(!line.contains("*/"));
    			
    		}
    		
    	}
    	
    	return loc;
    }
    
    
    //Writes the tickets info in a csv file
    public void writeTickets(String project, List<Ticket> tickets) throws FileNotFoundException {
 	   
        LocalDate d = null;
        Ticket t = null;
        String date = null;
        
 	   String output = project + "tickets.csv";
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
 	   
 	   String output = project + "commits.csv";
 	   
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
    
    
    //Writes the releases info in a csv file
    public void writeReleases(String project, List<Release> releases) throws FileNotFoundException {
 		
 	   Release r = null;
 	   String output = project + "versionInfo.csv";
 		   
 	   PrintStream printer = new PrintStream(new File(output));
 			   
 	   printer.println("Index,Version ID,Version Name,Date");
 	
 	   for (int i = 0; i < releases.size(); i++) {
 		   Integer index = i + 1;
 		   r = releases.get(i);
 		   printer.println(index.toString() + "," + r.getId() + "," + r.getName() + "," + r.getReleaseDate());
 	
 	   }
 	
 	   printer.close();
 		
    }
    
    
    //Writes dataset in a csv file
    public void writeRecords(String project, List<Record> records) throws FileNotFoundException {
    
    	Record r = null;
    	String output = project + "dataset.csv";
    	
    	PrintStream printer = new PrintStream(new File(output));
    	
    	printer.println("Release,File,Size,LocTouched,LocAdded,MaxLocAdded,AvgLocAdded,Nauth,Nfix,Nr,ChgSetSize");
	   		
    	for (int i = 0; i < records.size(); i++) {
	   		r = records.get(i);
	   		printer.println(r.getRelease()+","+r.getFile()+","+r.getSize()+","+r.getLocTouched()+","+r.getLocAdded()+","+
	   		r.getMaxLocAdded()+","+r.getAvgLocAdded()+","+r.getNauth()+","+r.getNfix()+","+r.getNr()+","+r.getChgSetSize());
    	}
    	
    	printer.close();
    	
    }
    
    
    //Creates a new csv file with all the data on the project
    public void createDataset(String author, String project, String attribute, String token) throws JSONException, IOException {
		
		List<Ticket> tickets = null;	//List of tickets from the project	
		List<Commit> commits = null;	//List of all the commits of the project
		List<Release> releases = null;	//List of all the releases of the project
    	
		//Searching information on the project
		SearchInfo search = new SearchInfo();
		
		logger.info("Searching for releases ...");
		releases = search.findReleases(project);
		logger.info(releases.size()+" releases found!");
		
		writeReleases(project,releases);		//Creates a csv file with release info
		
		logger.info("Searching for commits ...");
		commits = search.findCommits(author,project,token);
		logger.info(commits.size()+" commits found!");
		
		writeCommits(project,commits); 		//Creates a csv file with commit info
		
		logger.info("Searching for tickets ...");
		tickets = search.findTickets(project,attribute);
		logger.info(tickets.size()+" tickets found!");
		
		logger.info("Matching commits to tickets ...");
		search.matchCommits(tickets,commits);
		
		writeTickets(project,tickets);		//Creates a csv file with ticket info
		
		
		//We consider only half of the releases
		int releaseNum = releases.size()/2;
		LocalDate maxDate = null;
		Commit commit = null;
		List<CommittedFile> fileList = null;
		CommittedFile file = null;
		int counter = 0;

		for(int i=0; i<releaseNum; i++) {
			
			maxDate = releases.get(i).getReleaseDate();
			
			while(commits.size() > 0) {
				
				commit = commits.get(0);	//Takes the first commit
				
				if(commit.getDate().compareTo(maxDate) > 0) {
					break;	//If the commit exceeds the maximum date it exits the cycle
				}
				
				counter++; 
				
				logger.info("Searching files for commit "+counter+" ...");
				
				fileList = search.findCommittedFiles(author,project,token,commit);
				
				System.out.println(fileList.get(0).getContent());
				
				System.out.println(getLOC(fileList.get(0)));
				
				commits.remove(0);
			}
			
		
		}
		
		logger.info("DONE");
		
    }
    
    
	
//-------------------------------------------------------------------------------------------------------------------------------------------------------	
	
	public static void main(String[] args) throws JSONException, IOException {
	 
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
	   
		//Creating new dataset for the project
		GetData gd = new GetData();
		
		gd.createDataset(author,project,attribute,token);	
		
   }
	
}
