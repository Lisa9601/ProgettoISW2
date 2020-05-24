package milestones.milestone1.deliverable2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import java.util.Collections;
import java.util.Comparator;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.json.JSONException;
import org.json.JSONObject;

import milestones.milestone1.deliverable1.FindNewFeatures;

import org.json.JSONArray;


public class GetReleaseInfo {
	
	private HashMap<LocalDateTime, String> releaseNames = null;
	private HashMap<LocalDateTime, String> releaseID = null;
	private Integer numVersions = null;
	private List<LocalDateTime> releases = null;
	
    private static final Logger LOGGER = Logger.getLogger(FindNewFeatures.class.getName());

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
		
		return;
      }


	   public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
	      
		   InputStream is = new URL(url).openStream();
	      
		   try {
	         BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
	         String jsonText = readAll(rd);
	         JSONObject json = new JSONObject(jsonText);
	         return json;
	       } finally {
	         is.close();
	       }
	   }
	   
	   
	   private static String readAll(Reader rd) throws IOException {
		   
		   StringBuilder sb = new StringBuilder();
		   int cp;
		   
		   while ((cp = rd.read()) != -1) {
			   sb.append((char) cp);
		   }
		   
		   return sb.toString();
	   }
	   
	   
	   public void fillReleases(String projName) throws JSONException, IOException {
		   //Fills the arraylist with releases dates
		   //Ignores releases with missing dates   
		   String url = "https://issues.apache.org/jira/rest/api/2/project/" + projName;
		   JSONObject json;
		   
			json = readJsonFromUrl(url);

		   JSONArray versions = json.getJSONArray("versions");
		   releaseNames = new HashMap<LocalDateTime, String>();
		   releaseID = new HashMap<LocalDateTime, String> ();
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
		   
		   return;
	   }
	   
	   
	   public void orderByDate(String projectName) {
		   // order releases by date
		   Collections.sort(releases, new Comparator<LocalDateTime>(){
			   //@Override
			   public int compare(LocalDateTime o1, LocalDateTime o2) {
				   return o1.compareTo(o2);
			   }
		   });
		   
		   if (releases.size() < 6)
			   return;
		   
		   PrintStream printer = null;
		   
		   try {
			   
			   //Name of CSV for output
			   String output = projectName + "versionInfo.csv";
			   
			   printer = new PrintStream(new File(output));
		       printer.println("Index,Version ID,Version Name,Date");
		       
			   numVersions = releases.size();
			   for (int i = 0; i < releases.size(); i++) {
				   Integer index = i + 1;
				   printer.println(index.toString() + "," + releaseID.get(releases.get(i)) + "," + releaseNames.get(releases.get(i)) + "," + releases.get(i).toString());

			   }

		   } catch (Exception e) {
			   System.out.println("Error in csv writer");
			   e.printStackTrace();
		   } finally {
			   
			   printer.close();

		   }
	 
		   return;
	   }

	   public static void main(String[] args) throws IOException, JSONException {
	   
		   //Taking the configuration from config.json file
		   BufferedReader reader = new BufferedReader(new FileReader ("config.json"));
		   String config = readAll(reader);
		   JSONObject jsonConfig = new JSONObject(config);
   
		   String project = jsonConfig.getString("project");
   
		   reader.close();
		   
		   GetReleaseInfo grf = new GetReleaseInfo();

	       LOGGER.info("Searching release info for "+project+" ...");
		   
		   grf.fillReleases(project);
		   grf.orderByDate(project);
		   
		   
	       LOGGER.info("DONE");

	   }

}
