package main.java.release_info;

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

import main.java.common.JSONReader;

import org.json.JSONArray;


public class GetReleaseInfo {
	
	private HashMap<LocalDateTime, String> releaseNames = null;
	private HashMap<LocalDateTime, String> releaseID = null;
	private List<LocalDateTime> releases = null;
	private JSONReader jr = null;
	
    private static final Logger LOGGER = Logger.getLogger(GetReleaseInfo.class.getName());

	public GetReleaseInfo(JSONReader jr) {

		this.releases = new ArrayList<>();
		this.jr = jr;
		
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
		JSONObject json;
	   
		json = jr.readJsonFromUrl(url);

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
		   
		   if (releases.size() < 6)
			   return;
		   
		   //Name of CSV for output
		   String output = projectName + "versionInfo.csv";
		   
		   try(PrintStream printer = new PrintStream(new File(output))) {
			   
			   
		       printer.println("Index,Version ID,Version Name,Date");

			   for (int i = 0; i < releases.size(); i++) {
				   Integer index = i + 1;
				   printer.println(index.toString() + "," + releaseID.get(releases.get(i)) + "," + releaseNames.get(releases.get(i)) + "," + releases.get(i).toString());

			   }

		   } catch (Exception e) {
	    	   LOGGER.log(Level.SEVERE,"Exception occured ",e);
		   }
	 
	   }

	   public static void main(String[] args) throws IOException, JSONException {
	   
		   JSONReader jr = new JSONReader();
		   
		   //Taking the configuration from config.json file
		   BufferedReader reader = new BufferedReader(new FileReader ("config.json"));
		   String config = jr.readAll(reader);
		   JSONObject jsonConfig = new JSONObject(config);
   
		   String project = jsonConfig.getString("project");
   
		   reader.close();
		   
		   GetReleaseInfo grf = new GetReleaseInfo(jr);

		   String info = "Searching release info for " + project + " ...";
		   
	       LOGGER.info(info);
		   
		   grf.fillReleases(project);
		   grf.orderByDate(project);
		   
		   
	       LOGGER.info("DONE");

	   }

}
