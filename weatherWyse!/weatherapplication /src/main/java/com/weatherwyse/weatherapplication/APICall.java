package com.weatherwyse.weatherapplication;

import java.util.Scanner;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.LocalTime;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;


public class APICall {
	
	
	
	public static void timelineRequestHttpClient() throws Exception {
		//set up the end point
		Scanner sc=new Scanner(System.in);
		String user = System.getProperty("user.name");//gets username from computer
		LocalTime.now();
		System.out.println("Hello, "+user+"! Please enter the location you would like to see the forecast for (Please enter in either city & country, or in zip code format): ");
		String location = sc.next();
		String unitGroup;
		boolean Continue = true;
		
		if(isInUnitedStates(location)) {
			unitGroup = "us";
		}
		else {
			unitGroup = "metric";
		}
		
		
		System.out.println("");
		String apiEndPoint="https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/";
		String startDate=null;
		String endDate=null;
	
		String apiKey="MXDH2Y7G3AQ7EQQDTWC2JU4KC";
	
		StringBuilder requestBuilder=new StringBuilder(apiEndPoint);
		requestBuilder.append(URLEncoder.encode(location, StandardCharsets.UTF_8.toString()));
		
		if (startDate!=null && !startDate.isEmpty()) {
			requestBuilder.append("/").append(startDate);
			if (endDate!=null && !endDate.isEmpty()) {
				requestBuilder.append("/").append(endDate);
			}
		}
		
		URIBuilder builder = new URIBuilder(requestBuilder.toString());
		
		builder.setParameter("unitGroup", unitGroup)
			.setParameter("key", apiKey);

		
		
		HttpGet get = new HttpGet(builder.build());
		
		CloseableHttpClient httpclient = HttpClients.createDefault();
		
		CloseableHttpResponse response = httpclient.execute(get);    
		
		String rawResult=null;
		try {
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				System.out.printf("Bad response status code:%d%n", response.getStatusLine().getStatusCode());
				return;
			}
			
			HttpEntity entity = response.getEntity();
		    if (entity != null) {
		    	rawResult=EntityUtils.toString(entity, Charset.forName("utf-8"));
		    }
		    
		    
		} finally {
			response.close();
		}
		
		parseTimelineJson(rawResult);
		
	}
	private static void parseTimelineJson(String rawResult) {
		
		if (rawResult==null || rawResult.isEmpty()) {
			System.out.printf("No raw data%n");
			return;
		}
		
		JSONObject timelineResponse = new JSONObject(rawResult);
		
		ZoneId zoneId=ZoneId.of(timelineResponse.getString("timezone"));
		
		System.out.printf("Weather data for: %s%n", timelineResponse.getString("resolvedAddress"));
		
		JSONArray values=timelineResponse.getJSONArray("days");
		
		System.out.printf("Date\tMaxTemp\tMinTemp\tPrecip\tSource%n");
		for (int i = 0; i < values.length(); i++) {
			JSONObject dayValue = values.getJSONObject(i);
            
            ZonedDateTime datetime=ZonedDateTime.ofInstant(Instant.ofEpochSecond(dayValue.getLong("datetimeEpoch")), zoneId);
            
            double maxtemp=dayValue.getDouble("tempmax");
            double mintemp=dayValue.getDouble("tempmin");
            double pop=dayValue.getDouble("precip");
            String source=dayValue.getString("source");
            System.out.printf("%s\t%.1f\t%.1f\t%.1f\t%s%n", datetime.format(DateTimeFormatter.ISO_LOCAL_DATE), maxtemp, mintemp, pop,source );
        }
	}
	
	
	public static void main(String[] args)  throws Exception {
		APICall.timelineRequestHttpClient();
	}
	
	// checks if the country after "," is the US or not.
	public static boolean isInUnitedStates(String location) {
		String parts[] = location.split(",");//splits the string to see the country.
		
		if(parts.length>1) {
			String country=parts[1].trim();
			return country.equalsIgnoreCase("US") || country.equalsIgnoreCase("United States");
 
		}
		return false;
		
		

	}
	
}
	
	// code sourced from https://www.visualcrossing.com/resources/documentation/weather-api/how-to-fetch-weather-forecast-data-from-a-restful-web-service-in-java/

