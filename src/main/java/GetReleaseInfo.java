package main.java;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Collections;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.json.JSONException;
import org.json.JSONObject;

import main.java.entities.Release;

import org.json.JSONArray;


public class GetReleaseInfo {
	
	private HashMap<LocalDateTime, String> releaseNames = null;
	private HashMap<LocalDateTime, String> releaseID = null;
	private List<LocalDateTime> releases = null;
	
    private static final Logger LOGGER = Logger.getLogger(GetReleaseInfo.class.getName());

	public GetReleaseInfo() {

		this.releases = new ArrayList<>();
		
	}
	   
	   
	public void addRelease(String strDate, String name, String id) {
		
		LocalDate date = LocalDate.parse(strDate);
		LocalDateTime dateTime = date.atStartOfDay();
		
		if (!releases.contains(dateTime))
			releases.add(dateTime);
		
		releaseNames.put(dateTime, name);
		releaseID.put(dateTime, id);
		
	}
	
	      
	public void fillReleases(String projName) throws JSONException, IOException {
		//Fills the arraylist with releases dates
		//Ignores releases with missing dates   
		String url = "https://issues.apache.org/jira/rest/api/2/project/" + projName;
		JSONReader jr = new JSONReader();
	   
		JSONObject json = jr.readJsonFromUrl(url);

		JSONArray versions = json.getJSONArray("versions");
		releaseNames = new HashMap<>();
		releaseID = new HashMap<> ();
		
		for (int i = 0; i < versions.length(); i++ ) {
			String name = "";
			String id = "";
			if(versions.getJSONObject(i).has("releaseDate")) {
				if (versions.getJSONObject(i).has("name"))
					name = versions.getJSONObject(i).get("name").toString();
				if (versions.getJSONObject(i).has("id"))
					id = versions.getJSONObject(i).get("id").toString();
				addRelease(versions.getJSONObject(i).get("releaseDate").toString(),name,id);
			}
		}
		   	
	}
	   
	   
	public void orderByDate(String projectName) {
		   // order releases by date
		   Collections.sort(releases, (LocalDateTime o1, LocalDateTime o2) -> o1.compareTo(o2));
	 
	   }
	
	
	public List<Release> findReleases(String author, String project, String token) throws JSONException, IOException{
		
		List<Release> releases = new ArrayList<>();
		JSONReader jr = new JSONReader();
		JSONArray comm = null;
		
		fillReleases(project);
		Collections.sort(this.releases, (LocalDateTime o1, LocalDateTime o2) -> o1.compareTo(o2));
		
		//Searches for releases name from the gihub repo
		String url = "https://api.github.com/repos/"+author+"/"+project+"/tags";
	       
		try{
			comm = jr.readJsonArrayFromUrl(url,token);
		}catch(Exception e) {
			LOGGER.log(Level.SEVERE,"Exception occur ",e);
			return releases;
		}
		
		String name = null;
		Release r = null;
		
		for(int i=0; i<comm.length(); i++) {
			
			name = comm.getJSONObject(i).get("name").toString();
			
			for(int j=0; j< this.releases.size(); j++) {
				
				if(name.contains(releaseNames.get(this.releases.get(j)))) {
					
					r = new Release(this.releases.get(j).toString().substring(0,10),releaseNames.get(this.releases.get(j)),releaseID.get(this.releases.get(j)));
					releases.add(r);
					continue;
				}
				
			}
			
		}
		
		return releases;
	}
	
//------------------------------------------------------------------------------------------------------------------------------------------------------	

	   public static void main(String[] args) throws IOException, JSONException {
	   
		   JSONReader jr = new JSONReader();
		   
		   //Taking the configuration from config.json file
		   BufferedReader reader = new BufferedReader(new FileReader ("config.json"));
		   String config = jr.readAll(reader);
		   JSONObject jsonConfig = new JSONObject(config);
   
		   String project = jsonConfig.getString("project");
		   String author = jsonConfig.getString("author");
		   String token = jsonConfig.getString("token");
		   
		   reader.close();
		   
		   GetReleaseInfo grf = new GetReleaseInfo();

		   String info = "Searching release info for " + project + " ...";
		   
	       LOGGER.info(info);
		   
		   List<Release> releases = grf.findReleases(author,project,token);
		   Release r = null;
	       
		   //Name of CSV for output
		   String output = "results/" + project + "versionInfo.csv";
		   
		   try(PrintStream printer = new PrintStream(new File(output))) {
			   
			   
		       printer.println("Index,Version ID,Version Name,Date");

			   for (int i = 0; i < releases.size(); i++) {
				   Integer index = i + 1;
				   r = releases.get(i);
				   printer.println(index.toString() + "," + r.getId() + "," + r.getName() + "," + r.getReleaseDate());

			   }

		   } catch (Exception e) {
	    	   LOGGER.log(Level.SEVERE,"Exception occured ",e);
		   }
		   
		   
	       LOGGER.info("DONE");

	   }

}
