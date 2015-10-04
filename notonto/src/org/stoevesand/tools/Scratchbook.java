package org.stoevesand.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class Scratchbook {

	public static void main(String[] params) {

		try {
			URL url = new URL("http://www.notonto.de/rest/10/jan/jan1234/lessons");
      URLConnection yc = url.openConnection();
      BufferedReader in = new BufferedReader(
                              new InputStreamReader(
                              yc.getInputStream()));
      String inputLine;
      StringBuffer data = new StringBuffer();

      while ((inputLine = in.readLine()) != null)
      	data.append(inputLine);
      in.close();

      System.out.println("D: " + data.toString());
			
      try {
				JSONObject jo = new JSONObject(data.toString());
				System.out.println("jo: "+jo);
				JSONArray ul = (JSONArray)jo.get("userlessons");
				System.out.println("ul: "+ul);
				JSONObject jo2 = ul.getJSONObject(1);
				System.out.println("jo1: "+jo2 );
				String desc = jo2.getString("description");
				System.out.println("desc: "+desc);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
      
      
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
