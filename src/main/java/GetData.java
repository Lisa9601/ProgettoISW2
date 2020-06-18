package main.java;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import main.java.entities.Commit;
import main.java.entities.CommittedFile;
import main.java.entities.Record;
import main.java.entities.Release;
import main.java.entities.Ticket;

public class GetData {

    private static Logger logger;
    
	private float p;			//proportional number of versions
	private int numBugs;		//1% moving window
	private int count;			//number of tickets
	private int numAffected;	//sum of the affected versions
	private int countToken;		//number of tokens
    
	
	static {

        System.setProperty("java.util.logging.config.file", "logging.properties");
        logger = Logger.getLogger(SearchInfo.class.getName());
    }
	
    
    public GetData() {
		this.p = 0;
		this.numBugs = 0;
		this.count = 0;
		this.numAffected = 0;
		this.countToken = 0;
	}
  
    
    //Returns the new token to use
    public String getToken(List<String> tokens) {
    	
    	this.countToken = (this.countToken+1)%tokens.size();
    	return tokens.get(this.countToken);
    	
    }
    
    
    //Searches a release by name
    public int findReleaseNum(List<Release> releases, String name) {
    	int num = -1;	//If the release is not found returns -1
    	
    	for(int i=0; i< releases.size(); i++) {
    		if(name.compareTo(releases.get(i).getName()) == 0) {
    			num = i;
    			break;
    		}
    	}
    	
    	return num;
    }

    
    //Searches a release by Date
    public int findReleaseNum(List<Release> releases, LocalDate date) {
    	int num = -1;	//If the release is not found returns -1
    	
    	for(int i=0; i< releases.size(); i++) {
    		if(date.compareTo(releases.get(i).getReleaseDate()) < 0) {
    			num = i;
    			break;
    		}
    	}
    	
    	return num;
    }
    
    
    //Computes the proportional number of versions used in proportion
    public void computeP(int versions) {
    	
    	this.numAffected += versions;
    	this.count++;
    	
    	if(this.count >= this.numBugs) {
    		this.p = (float)this.numAffected/this.count;
        	this.numAffected = 0;
        	this.count = 0;
    	}
    	
    }
    
    
    //Uses proportion on a ticket to get the affected versions
    public void proportion(Ticket ticket, List<Release> releases) {
    	
    	int fixedV;
    	int openV;
    	int injV;
    	
    	fixedV = findReleaseNum(releases,ticket.getFixed().get(0)) + 1;
    	openV = findReleaseNum(releases,ticket.getDate()) + 1;
    	
    	
    	//If the fixed version isn't found it's set equal to the opening version
    	if(fixedV == 0) {
    		fixedV = openV;
    	}
    	
    	//If the opening version is greater the the fixed version they are inverted
    	if(openV > fixedV) {
    		int temp = fixedV;
    		fixedV = openV;
    		openV = temp;
    	}
    	
    	injV = Math.round(fixedV -(fixedV - openV)*this.p);
    	
    	//Checks if the value is negative
    	if(injV <= 0) {
    		injV = 1;
    	}
    	
    	//Adds all the new affected versions to the ticket
    	for(int i=injV-1; i<fixedV; i++) {
    		ticket.addAffected(releases.get(i).getName());
    	}
    	
    }
    
    
    //Checks if the ticket needs proportion or not
    public void prepareTicket(Ticket ticket, List<Release> releases) {
    	
		//If there's more than one fixed version, we'll consider the most recent one
		List<String> fixed = ticket.getFixed();
		
		while(fixed.size() > 1) {
			if(findReleaseNum(releases,fixed.get(0))>= findReleaseNum(releases,fixed.get(1))) {
				ticket.addAffected(fixed.get(1));	//Adds it to the affected versions
				ticket.removeFixed(1);
			}
			else {
				ticket.addAffected(fixed.get(0));
				ticket.removeFixed(0);
			}
		}
    		
    	
    	List<String> affected = ticket.getAffected();
    	
    	for(int i=0; i<affected.size();i++) {
    		
    		if(findReleaseNum(releases,affected.get(i)) > findReleaseNum(releases,ticket.getFixed().get(0))) {
    			//If the affected version is greater than the fixed version we discard the data 
    			ticket.setAffected(new ArrayList<String>());
    			break;
    		}
    		
    	}
    	
    	if(ticket.getAffected().size() == 0) {
    		proportion(ticket,releases);	//If there's no affected versions use proportion
    	}
    	
    	computeP(ticket.getAffected().size());
    	
    }
    
    
    //Updates all records releated to the files committed in the commit 
    public void updateRecords(Commit commit, List<CommittedFile> fileList, List<HashMap<String,Record>> maps, List<Record> records, int id) {
    	CommittedFile file = null;
    	Record r = null;
		
		for(int i=0; i<fileList.size(); i++) {
			
			file = fileList.get(i);
			
			r = maps.get(id).get(file.getName());
			
			//If it's a new file it's added to the list and the map
			if(r == null) {
				
				r = new Record(id+1,file.getName());
				
				maps.get(id).put(file.getName(),r);
				records.add(r);
				
			}
				
			r.setSize(file.getSize());
			r.addLocTouched(file.getLocTouched());
			r.addLocAdded(file.getLocAdded());
			r.addAuthor(commit.getAuthor());
			r.addRevision();
			r.addChgSetSize(fileList.size()-1);	
			
		}
    	
    }
    
    
    //Updates the bugginess of the files 
    public void updateBugginess(Ticket t, List<Release> releases, List<HashMap<String,Record>> maps, List<Record> records,
    		String author, String project, List<String> tokens) throws UnsupportedEncodingException {
    	
    	int id;
    	List<CommittedFile> fileList = null;
    	List<String> versions = t.getAffected();
    	Commit commit = t.getFixCommit();
    	
    	if(commit == null || t.getFixed().size() == 0) {
    		return;	//If the ticket has no fix commit or no fixed versions it's not considered
    	}
    	
    	prepareTicket(t,releases);
    	
		for(int j=0; j<versions.size(); j++) {
			
			id = findReleaseNum(releases,versions.get(j));	//Checks if the release is in 'releases'

			if(id !=-1 && id < releases.size()) {  

				fileList = getCommitFiles(commit,author,project,tokens);
				
				updateVersion(id,fileList,maps,records,false);
				
			}
				
		}
		
		//Fixed version
		id = findReleaseNum(releases,t.getFixed().get(0));
		
		if(id!= -1 && id < releases.size()) {
			
			fileList = getCommitFiles(commit,author,project,tokens);

			
			updateVersion(id,fileList,maps,records,true);
			
		}
		
    	
    	
    }
    
    
    //Updates the records with the bugginess and the number of fix
    public void updateVersion(int id, List<CommittedFile> fileList, List<HashMap<String,Record>> maps, List<Record> records, boolean fix) {
    	Record r = null;
    	CommittedFile file = null;
		
		for(int i=0; i<fileList.size(); i++) {
			
			file = fileList.get(i);
			r = maps.get(id).get(file.getName());
			
			if(r == null) {
				r = new Record(id+1,file.getName());
				
				maps.get(id).put(file.getName(),r);
				records.add(r);
			}
			
			r.setBuggy("Yes");
			
			if(fix) {
				r.addFix();
			}
			
		}
		
    }
    
    
    public List<CommittedFile> getCommitFiles(Commit commit, String author, String project, List<String> tokens) throws UnsupportedEncodingException {
		List<CommittedFile> files = commit.getFiles();
    	
    	if(files == null) {
        	SearchInfo search = new SearchInfo();
			files = search.findCommittedFiles(author, project, getToken(tokens), commit);
			commit.setFiles(files);	//Sets the files for the commit
		}
    	
    	return files;
    }
    
    
    //Creates a new csv file with all the data on the project
    public void createDataset(String author, String project, String attribute, List<String> tokens) throws JSONException, IOException {
		
    	int i = 0;
		int counter = 0;	//Used to keep track of the commits beeing processed
		
		LocalDate maxDate = null;
		Commit commit = null;
		String info = null;
		
		List<Ticket> tickets = null;		//List of tickets from the project	
		List<Commit> commits = null;		//List of all the commits of the project
		List<Release> releases = null;		//List of all the releases of the project
		
		this.countToken = -1;
		this.p = 0;
		this.numAffected = 0;
		this.count = 0;
    	
		// SEARCHING INFO ON THE PROJECT
		
		SearchInfo search = new SearchInfo();
		
		logger.info("Searching for releases ...");
		releases = search.findReleases(project);
		logger.info(releases.size()+" releases found!");
		
		writeReleases(project,releases);		//Creates a csv file with release info
		
		logger.info("Searching for commits ...");
		commits = search.findCommits(author,project,getToken(tokens));
		logger.info(commits.size()+" commits found!");
		
		writeCommits(project,commits); 		//Creates a csv file with commit info
		
		logger.info("Searching for tickets ...");
		tickets = search.findTickets(project,attribute);
		logger.info(tickets.size()+" tickets found!");
		
		//Sets the number of tickets to consider for the moving window to 1% of the total
		this.numBugs = Math.round(tickets.size()/100);
		
		logger.info("Matching commits to tickets ...");
		search.matchCommits(tickets,commits);
		
		writeTickets(project,tickets);		//Creates a csv file with ticket info
		
		
		// ASSOCIATING COMMITS AND TICKETS TO RELEASES
		
		int releaseNum = releases.size()/2;		//We consider only half of the releases
		releases = releases.subList(0,releaseNum);		
		
		List<Record> records = new ArrayList<>();
		List<HashMap<String,Record>> maps = new ArrayList<>();
		List<CommittedFile> fileList = null;
		
		for(i=0; i<releases.size(); i++) {
			
			maxDate = releases.get(i).getReleaseDate();
			maps.add(new HashMap<String,Record>());	//Creates a new hashmap for the release
			
			while(commits.size() > 0 && commits.get(0).getDate().compareTo(maxDate) < 0) {
				
				commit = commits.get(0);	//Takes the first commit	
				
				counter++; 
				info = "Searching files for commit " + counter + " release "+ (i+1); 
				logger.info(info);
				
				fileList = getCommitFiles(commit,author,project,tokens);
				
				updateRecords(commit, fileList, maps, records, i);
				
				commits.remove(0);
			}	
		
		}
		
		
		counter = 0;
		
		for(i=0; i<tickets.size(); i++) {
			
			counter++;
			info = "Working on ticket " + counter ; 
			logger.info(info);
			
			updateBugginess(tickets.get(i), releases, maps, records, author, project, tokens);
			
		}
		
		logger.info("Creating dataset ...");
		
		//Sorts the records
		Collections.sort(records, (Record o1, Record o2) -> o1.getRelease().compareTo(o2.getRelease()));
		
		writeRecords(project,records);
		
		logger.info("DONE");
		
    }
    
    
    //Writes the tickets info in a csv file
    public void writeTickets(String project, List<Ticket> tickets) throws FileNotFoundException {
 	   
    	LocalDate d = null;
        Ticket t = null;
        String date = null;
        
 	   	String output = project + "tickets.csv";
 	   	PrintStream printer = new PrintStream(new File(output));
        
 	   	printer.println("Id;Date;Num Commits");
        
        for(int i=0;i<tickets.size();i++) {
        	t = tickets.get(i);
     	   	d = t.getResolutionDate();
     	   
     	   	if(d == null) {
     	   		date = "null";
     	   	}
     	   	else {
     	   		date = d.getMonthValue() +"/"+ d.getYear();
     	   	}

     	   	printer.println(t.getId() +";"+ date +";"+ t.getNumCommits());
        }
        
        printer.close();
 	   
    }
    
    
    //Writes the commits info in a csv file
    public void writeCommits(String project, List<Commit> commits) throws FileNotFoundException {
        
    	LocalDate cd = null;
        Commit c = null;
 	   	String output = project + "commits.csv";
 	   
 	   	PrintStream printer = new PrintStream(new File(output));
       
 	   printer.println("Sha;Date");
 	   	
        for(int i=0;i<commits.size();i++) {
     	   	c = commits.get(i);
     	   	cd = c.getDate();

     	   	printer.println(c.getSha()+";"+cd.getMonthValue() +"/"+ cd.getYear());
        }
        
        printer.close();
 	   
    }
    
    
    //Writes the releases info in a csv file
    public void writeReleases(String project, List<Release> releases) throws FileNotFoundException {
 		
 	   Release r = null;
 	   String output = project + "versionInfo.csv";
 		   
 	   PrintStream printer = new PrintStream(new File(output));
 			   
 	   printer.println("Index;Version ID;Version Name;Date");
 	
 	   for (int i = 0; i < releases.size(); i++) {
 		   Integer index = i + 1;
 		   r = releases.get(i);
 		   printer.println(index.toString() + ";" + r.getId() + ";" + r.getName() + ";" + r.getReleaseDate());
 	
 	   }
 	
 	   printer.close();
 		
    }
    
    
    //Writes the dataset in a csv file
    public void writeRecords(String project, List<Record> records) throws FileNotFoundException {
    
    	Record r = null;
    	String output = project + "dataset.csv";
    	
    	PrintStream printer = new PrintStream(new File(output));
    	
    	printer.println("Release;File;Size;LocTouched;LocAdded;MaxLocAdded;AvgLocAdded;Nauth;Nfix;Nr;ChgSetSize;Buggy");
	   		
    	for (int i = 0; i < records.size(); i++) {
	   		r = records.get(i);
	   		printer.println(r.getRelease()+";"+r.getFile()+";"+r.getSize()+";"+r.getLocTouched()+";"+r.getLocAdded()+";"+
	   		r.getMaxLocAdded()+";"+String.format("%.2f", r.getAvgLocAdded())+";"+r.getNauth()+";"+r.getNfix()+";"+r.getNr()+";"+r.getChgSetSize()+
	   		";"+r.getBuggy());
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
		
		JSONArray tokenArray = jsonConfig.getJSONArray("token");
		
		List<String> tokens = new ArrayList<>();
		
		for(int i=0; i< tokenArray.length(); i++) {
			tokens.add(tokenArray.get(i).toString());
		}
	   
		reader.close();
	   
		//Creating new dataset for the project
		GetData gd = new GetData();
		
		gd.createDataset(author,project,attribute,tokens);	
		
   }
	
}
