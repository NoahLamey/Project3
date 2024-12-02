import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class TripPoint {

	private double lat;	// latitude
	private double lon;	// longitude
	private int time;	// time in minutes
	
	private static ArrayList<TripPoint> trip;	// ArrayList of every point in a trip
	private static ArrayList<TripPoint> movingTrip = new ArrayList<>();
	// default constructor
	public TripPoint() {
		time = 0;
		lat = 0.0;
		lon = 0.0;
	}
	
	// constructor given time, latitude, and longitude
	public TripPoint(int time, double lat, double lon) {
		this.time = time;
		this.lat = lat;
		this.lon = lon;
	}
	
	// returns time
	public int getTime() {
		return time;
	}
	
	// returns latitude
	public double getLat() {
		return lat;
	}
	
	// returns longitude
	public double getLon() {
		return lon;
	}
	
	// returns a copy of trip ArrayList
	public static ArrayList<TripPoint> getTrip() {
		return new ArrayList<>(trip);
	}
	
	// uses the haversine formula for great sphere distance between two points
	public static double haversineDistance(TripPoint first, TripPoint second) {
		// distance between latitudes and longitudes
		double lat1 = first.getLat();
		double lat2 = second.getLat();
		double lon1 = first.getLon();
		double lon2 = second.getLon();
		
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
 
        // convert to radians
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
 
        // apply formulae
        double a = Math.pow(Math.sin(dLat / 2), 2) +
                   Math.pow(Math.sin(dLon / 2), 2) *
                   Math.cos(lat1) *
                   Math.cos(lat2);
        double rad = 6371;
        double c = 2 * Math.asin(Math.sqrt(a));
        return rad * c;
	}
	
	// finds the average speed between two TripPoints in km/hr
	public static double avgSpeed(TripPoint a, TripPoint b) {
		
		int timeInMinutes = Math.abs(a.getTime() - b.getTime());
		
		double distance = haversineDistance(a, b);
		
		double kmpmin = distance / timeInMinutes;
		
		return kmpmin * 60;
	}
	
	// returns the total time of trip in hours
	public static double totalTime() {
		int minutes = trip.get(trip.size()-1).getTime();
		double hours = minutes / 60.0;
		return hours;
	}
	
	// finds the total distance traveled over the trip
	public static double totalDistance() throws FileNotFoundException, IOException {
		
		double distance = 0.0;
		
		if (trip.isEmpty()) {
			readFile("triplog.csv");
		}
		
		for (int i = 1; i < trip.size(); ++i) {
			distance += haversineDistance(trip.get(i-1), trip.get(i));
		}
		
		return distance;
	}
	
	public String toString() {
		
		return null;
	}

	public static void readFile(String filename) throws FileNotFoundException, IOException {

		// construct a file object for the file with the given name.
		File file = new File(filename);

		// construct a scanner to read the file.
		Scanner fileScanner = new Scanner(file);
		
		// initiliaze trip
		trip = new ArrayList<TripPoint>();

		// create the Array that will store each lines data so we can grab the time, lat, and lon
		String[] fileData = null;

		// grab the next line
		while (fileScanner.hasNextLine()) {
			String line = fileScanner.nextLine();

			// split each line along the commas
			fileData = line.split(",");

			// only write relevant lines
			if (!line.contains("Time")) {
				// fileData[0] corresponds to time, fileData[1] to lat, fileData[2] to lon
				trip.add(new TripPoint(Integer.parseInt(fileData[0]), Double.parseDouble(fileData[1]), Double.parseDouble(fileData[2])));
			}
		}

		// close scanner
		fileScanner.close();
	}
	public static double movingTime() {
		int mins = (movingTrip.size() - 1)*5;
		double hours = mins / 60.0;
		return hours;
	}
	
	
	public static double stoppedTime() {
		return totalTime() - movingTime();
	}
	
	
	public static double avgMovingSpeed() {
		double totalDist = 0.0;
		double hours = movingTime();
		double speed;
		TripPoint pointA;
		TripPoint pointB;
			for(int i = 1; i < movingTrip.size(); i++) {
				pointA = movingTrip.get(i - 1);
				pointB = movingTrip.get(i);
				totalDist += haversineDistance(pointA, pointB);
		}speed = totalDist / hours;
		
		return speed;
	}
	
	
	public static ArrayList<TripPoint> getMovingTrip(){
		return new ArrayList<>(movingTrip);
	}


//Heuristic to find stops based on the threshold in kilometers
	public static int h1StopDetection() {
		double dThreshold = 0.6;
		 if (trip == null || trip.isEmpty()) {
			 try {
			   readFile("triplog.csv");
			 }catch(FileNotFoundException e) {
				 System.out.println("triplog.csv file not found.");
			 }catch(IOException e) {
				 System.out.println("Error reading triplog.csv file.");
			 		}
			 
			    }
		    
		movingTrip.clear();
		int count = 0;
		TripPoint previous = null;
		for(TripPoint point : trip) {
			if(previous != null) {
				double distance = haversineDistance(previous, point);
				if(distance > dThreshold) {
					movingTrip.add(point);
				}else {
					count++;
				}
			}else {
				movingTrip.add(point);
			}
			previous = point;
		}return count;
}
	//Heuristic to find stops based on the stop radius in kilometers
	public static int h2StopDetection(){
		boolean stop;
		int count = 0;
		double distance;
		double stopRadius = 0.5; // 0.5 km stop Radius
		
   		movingTrip = new ArrayList<>();
		ArrayList<TripPoint> stops = new ArrayList<>();
		for (TripPoint a : trip) {

			if(stops.isEmpty())
				stops.add(a); 
			else{
				stop = false;
				for(TripPoint b : stops){
					distance = haversineDistance(a, b);
					if(distance <= stopRadius){
						stop = true;
						break;
					}
				}
				if(stop)
					stops.add(a);
				else{
					if(stops.size() >= 3)
						count+= stops.size();
					else
						movingTrip.addAll(stops);
					stops.clear();
					movingTrip.add(a);
				}
			}
			if(stops.isEmpty()){
				stop = false;
				for(TripPoint b: movingTrip){
					distance = haversineDistance(a, b);
					if(distance <= stopRadius){
						stop = true;
						break;
					}
				}

				if(stop){
					stops.add(a);
					movingTrip.removeAll(stops);
				}
				else
					movingTrip.add(a);
			}
		}
		if(stops.size() >= 3)
			count+= stops.size();
		else
			movingTrip.addAll(stops);
		stops.clear();
		
		return count;	
	}
}
	