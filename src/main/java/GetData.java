package main.java;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.ArrayList;
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

     	   printer.println(t.getId() +","+ date +","+ t.getNumCommits());
        }
        
        printer.close();
 	   
    }
    
    
    //Returns the number of the release if it's in the list, or -1
    public int findRelease(List<Release> releases, String name) {
    	int id = -1;	//id of the release
    	
    	for(int i=0; i< releases.size(); i++) {
    		if(name.compareTo(releases.get(i).getName()) == 0) {
    			id = i;
    			break;
    		}
    	}
    	
    	return id;
    }
    
    
    //Creates a new csv file with all the data on the project
    public void createDataset(String author, String project, String attribute, String token) throws JSONException, IOException {
		
    	int i = 0;
    	int j = 0;
    	int k = 0;
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

		List<Record> records = new ArrayList<>();
		Record r = null;
		
		List<HashMap<String,Record>> maps = new ArrayList<>();
		
		for(i=0; i<releaseNum; i++) {
			
			maxDate = releases.get(i).getReleaseDate();
			maps.add(new HashMap<String,Record>());
			
			while(commits.size() > 0) {
				
				commit = commits.get(0);	//Takes the first commit
				
				if(commit.getDate().compareTo(maxDate) > 0) {
					break;	//If the commit exceeds the maximum date it exits the cycle
				}
				
				counter++; 
				
				logger.info("Searching files for commit "+counter+" ...");
				
				fileList = search.findCommittedFiles(author,project,token,commit);
				commit.setFiles(fileList);	//sets the list of files for that commit
				
				for(j=0;j<fileList.size();j++) {
					
					file = fileList.get(j);
					
					r = maps.get(i).get(file.getName());
					
					//If it's a new file it's added to the list and the map
					if(r == null) {
						
						r = new Record(i+1,file.getName());
						
						maps.get(i).put(file.getName(),r);
						records.add(r);
						
					}
						
					r.setSize(file.getSize());
					r.addLocTouched(file.getLocTouched());
					r.addLocAdded(file.getLocAdded());
					r.addAuthor(commit.getAuthor());
					r.addRevision();
					r.addChgSetSize(fileList.size()-1);
						
					
				}
				
				commits.remove(0);
			}	
		
		}
	
		logger.info("Creating dataset ...");
		
		List<String> versions = null;
		int id;
		
		for(i=0; i<tickets.size(); i++) {

			commit = tickets.get(i).getFixCommit();
			versions = tickets.get(i).getAffected();
			
			if(commit == null) {
				continue;	//If there's no commit associated with the ticket it goes to the next one
			}
			else if(versions == null) {
				//use proportion 
			}
			else {
				
				for(j=0; j<versions.size(); j++) {
					
					id = findRelease(releases,versions.get(j));
					fileList = commit.getFiles();
					//Checks if the version is between the ones beeing considered
					if(id !=-1 && fileList != null) { // id != -1 
						
						for(k=0; k<fileList.size(); k++) {
							
							r = maps.get(id).get(fileList.get(k).getName());
							
							if(r != null) {
								
								r.setBuggy("Si");
								
							}
							
						}
						
					}
					
				}
				
			}
			
		}
		
		writeRecords(project,records);
		
		logger.info("DONE");
		
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
    	
    	printer.println("Release,File,Size,LocTouched,LocAdded,MaxLocAdded,AvgLocAdded,Nauth,Nfix,Nr,ChgSetSize,Buggy");
	   		
    	for (int i = 0; i < records.size(); i++) {
	   		r = records.get(i);
	   		printer.println(r.getRelease()+","+r.getFile()+","+r.getSize()+","+r.getLocTouched()+","+r.getLocAdded()+","+
	   		r.getMaxLocAdded()+","+r.getAvgLocAdded()+","+r.getNauth()+","+r.getNfix()+","+r.getNr()+","+r.getChgSetSize()+
	   		","+r.getBuggy());
    	}
    	
    	printer.close();
    	
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
