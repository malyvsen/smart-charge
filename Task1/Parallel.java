/**
 * @date 2018-02-16
 * 
 * @author Michał Markiewicz <markiewicz@ii.uj.edu.pl>
 * @author Nicholas Bochenski <iceflamecode@gmail.com>
 */
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import org.pcj.NodesDescription;
import org.pcj.PCJ;
import org.pcj.RegisterStorage;
import org.pcj.StartPoint;
import org.pcj.Storage;

@RegisterStorage(EV03.Shared.class)
public class EV03 implements StartPoint {

	public static void main(String[] args) throws IOException {
		String nodesFile = "nodes.txt";
		PCJ.start(EV03.class, new NodesDescription(nodesFile));

	}

	public void main() throws Throwable {
		log("PCJ Thread " + PCJ.myId() + " out of " + PCJ.threadCount());
		mainProper();

	}

	class City {
		int osm_id;
		int x;
		int y;
		double lat;
		double lon;
		String name;
		int existingCount;
		int targetCount;
		int population;

		public String toString() {
			return name + " " + x + " " + y + " " + population + " [" + lat + ", " + lon + "] " + osm_id + " @"
					+ existingCount + " -> " + targetCount;
		}
	}

	static class Station implements Serializable {
		private static final long serialVersionUID = -4202820817964795319L;
		boolean existing;
		int x;
		int y;
		double lat;
		double lon;
		String name;
		int limit = MAXIMUM_POINTS_PER_CANDIDATE;
		int multiplicity;
		int quality;

		public String toString() {
			return name + " " + x + " " + y + " " + " [" + lat + ", " + lon + "] " + (existing ? "e" : "-") + " Q"
					+ quality;
		}

		public String getId() {
			return x + "$" + y;
		}

		public String getXML() {
			StringBuffer sb = new StringBuffer();
			sb.append("<wpt lat=\"");
			sb.append(lat);
			sb.append("\" lon=\"");
			sb.append(lon);
			sb.append("\">");
			sb.append("<name>");
			sb.append(name);
			sb.append("</name></wpt>");
			return sb.toString();
		}
	}

	class Building {
		int x;
		int y;
		double lat;
		double lon;
		String name;

		public String toString() {
			return name + " " + x + " " + y + " " + " [" + lat + ", " + lon + "] ";
		}
	}

	Map<String, City> citiesByName = new TreeMap<String, City>();
	Map<String, List<Station>> stationByCityName = new TreeMap<String, List<Station>>();
	Map<String, List<Building>> buildingByCityName = new TreeMap<String, List<Building>>();

	public final static String SEMICOLON = ";";
	public final static String CENTERS_FILE = "centers.txt";

	public final static String EXISTING_FILE = "existing.txt";
	public final static String POTENTIAL_FILE = "potential.txt";

	public final static String WAW_FILE = "WAW.txt";
	public final static String WRO_FILE = "WRO.txt";

	public final static int MAXIMUM_POINTS_PER_CANDIDATE = 6;

	public final static int MINIMAL_DISTANCE_SQARED = (int) (250 * 250 / 1.69);

	public List<Building> readBuildings() {
		List<Building> res = new LinkedList<Building>();
		String line;
		// REMOVE ALL FILES TO PREVIOUS FUNCTIONALITY
		for (String file : new String[] { WAW_FILE, WRO_FILE }) {
			try (BufferedReader br = new BufferedReader(new FileReader(file))) {
				while ((line = br.readLine()) != null) {
					String[] fields = line.split(SEMICOLON);
					Building b = new Building();
					int i = 0;
					b.x = Integer.parseInt(fields[i++]);
					b.y = Integer.parseInt(fields[i++]);
					b.lon = Double.parseDouble(fields[i++]);
					b.lat = Double.parseDouble(fields[i++]);
					b.name = fields[i++];
					res.add(b);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return res;
	}

	public List<Station> readStations() {
		List<Station> res = new LinkedList<Station>();
		String line;
		for (String file : new String[] { EXISTING_FILE, POTENTIAL_FILE }) {
			try (BufferedReader br = new BufferedReader(new FileReader(file))) {
				while ((line = br.readLine()) != null) {
					String[] fields = line.split(SEMICOLON);
					Station s = new Station();
					int i = 0;
					s.x = Integer.parseInt(fields[i++]);
					s.y = Integer.parseInt(fields[i++]);
					s.lon = Double.parseDouble(fields[i++]);
					s.lat = Double.parseDouble(fields[i++]);
					s.existing = file == EXISTING_FILE;
					s.multiplicity = file == EXISTING_FILE ? 1 : 0;
					s.name = fields[i++];

					boolean tooClose = false;
					for (Station comp : res) {
						if (dist(comp, s) < MINIMAL_DISTANCE_SQARED) {
							tooClose = true;
							break;
						}
					}
					if (!tooClose) {
						res.add(s);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return res;
	}

	public List<City> readCities() {
		List<City> res = new LinkedList<City>();
		String line;
		try (BufferedReader br = new BufferedReader(new FileReader(CENTERS_FILE))) {
			while ((line = br.readLine()) != null) {
				String[] fields = line.split(SEMICOLON);
				City c = new City();
				int i = 0;
				c.osm_id = Integer.parseInt(fields[i++]);
				c.population = Integer.parseInt(fields[i++]);
				c.x = Integer.parseInt(fields[i++]);
				c.y = Integer.parseInt(fields[i++]);
				c.lon = Double.parseDouble(fields[i++]);
				c.lat = Double.parseDouble(fields[i++]);
				c.name = fields[i++];
				res.add(c);
				citiesByName.put(c.name, c);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return res;
	}

	public final static long dist(City c0, City c1) {
		return dist(c0.x, c0.y, c1.x, c1.y);
	}

	public final static long dist(Station c0, Station c1) {
		return dist(c0.x, c0.y, c1.x, c1.y);
	}

	public final static long dist(Building b0, Station c1) {
		return dist(b0.x, b0.y, c1.x, c1.y);
	}

	public final static long dist(long x0, long y0, long x1, long y1) {
		return (x0 - x1) * (x0 - x1) + (y0 - y1) * (y0 - y1);
	}

	public final static boolean DEBUG = !false;

	public void mainProper() {
		log("+EV Thread " + PCJ.myId() + " out of " + PCJ.threadCount());
		Random random = new Random();
		EV03 ev = this;

		List<City> cities = ev.readCities();
		List<Station> stations = ev.readStations();
		List<Building> buildings = ev.readBuildings();

		City myPCJCity = null;
		if (PCJ.myId() > cities.size()) {
			log("-EV Thread " + PCJ.myId() + " out of " + PCJ.threadCount());
		} else {
			myPCJCity = cities.get(PCJ.myId() % cities.size());

			final int TARGET_STATIONS = 6000;
			ev.initalizeStationsAndBuildings(cities, stations, buildings, TARGET_STATIONS);
			// FIXME:START
			// Find the closest station for all buildings
			for (String buildingCityName : ev.buildingByCityName.keySet()) {
				if (citiesByName.get(buildingCityName) != myPCJCity) {
					continue;
				}
				List<Building> buildingsInThisCity = ev.buildingByCityName.get(buildingCityName);
				List<Station> stationsInThisCity = ev.stationByCityName.get(buildingCityName);
				for (Building building : buildingsInThisCity) {
					long bestDistance = Long.MAX_VALUE;
					Station bestStation = null;
					for (Station station : stationsInThisCity) {
						long dist = dist(building, station);
						if (bestStation == null || bestDistance > dist) {
							bestDistance = dist;
							bestStation = station;
						}
					}
					if (bestStation != null) {
						bestStation.quality++;
					}
				}
			}
			for (Station station : stations) {
				station.quality = random.nextInt(station.quality + 1);
			}

			for (List<Station> stationsInCity : ev.stationByCityName.values()) {
				stationsInCity.sort(new Comparator<Station>() {
					public int compare(Station s0, Station s1) {
						return s1.quality - s0.quality;
					}
				});
			}
			// FIXME:STOP

			if (DEBUG) {
				log("Before " + myPCJCity);
			}

			// CPU
			for (City c0 : cities) {
				if (myPCJCity != c0) {
					continue;
				}
				List<Station> stationsInCity = ev.stationByCityName.get(c0.name);
				if (stationsInCity == null) {
					throw new RuntimeException("No potential stations in " + c0);
				}

				for (City c1 : cities) {
					// for each other city, find the potential station that is nearest to it - and
					// mark it as occupied
					if (c0 == c1) {
						continue;
					}

					long bestDistance = Long.MAX_VALUE;
					Station bestStation = null;
					for (Station s : stationsInCity) {
						long distance = dist(s.x, s.y, c1.x, c1.y);
						if (bestStation == null || distance < bestDistance) {
							bestDistance = distance;
							bestStation = s;
						}
					}

					if (bestStation == null) {
						throw new RuntimeException("No stations found in city " + c0);
					} else if (bestStation.multiplicity > 0) {
						/* there's something already there - do nothing */
					} else if (c0.existingCount < c0.targetCount) {
						bestStation.multiplicity++;
						c0.existingCount++;
					} else {
						break; // the city is saturated, we shouldn't place any more stations in it
					}
				}

				for (Station s : stationsInCity) {
					// mark stations as occupied (in order of quality)
					if (c0.existingCount >= c0.targetCount) {
						break; // no need to place any more stations
					}
					if (s.multiplicity > 0) {
						continue; // nothing to do with this station
					}
					s.multiplicity++;
					c0.existingCount++;
				}

				// forcibly fill city if number of charging stations in it < what it should be
				for (Station s : stationsInCity) {
					if (s.multiplicity < s.limit) {
						int diff = Math.min(s.limit - s.multiplicity, c0.targetCount - c0.existingCount);
						s.multiplicity += diff;
						c0.existingCount += diff;
						if (c0.existingCount >= c0.targetCount) {
							break;
						}
					}
				}
				if (c0.existingCount < c0.targetCount) {
					log("Unable to fill all slots in " + c0);
				}

				for (Station s : stationsInCity) {
					if (s.multiplicity > 0) {
						stationsComputedForThisCity.add(s);
					}
				}
			}
		}
		// log(PCJ.myId() + " has " + stationsComputedForThisCity.size());
		// abcTest = PCJ.myId();

		PCJ.barrier();

		// log(PCJ.myId() + " test: " + abcTest + " local: " +
		// PCJ.getLocal(Shared.abcTest));

		if (myPCJCity != null) {
			if (PCJ.myId() == 0) {
				for (int p = 0; p < PCJ.threadCount(); p++) {
					// log("Sending station data from node "+p);
					@SuppressWarnings("unchecked")
					LinkedList<Station> stationsComputedForACity = (LinkedList<Station>) PCJ.get(p,
							Shared.stationsComputedForThisCity);
					// log("Read "+ stationsComputedForACity.size() + " read from "+ p);
					// log("Test "+ PCJ.get(p, Shared.abcTest) + " read from "+ p);
					allComputedStations.addAll(stationsComputedForACity);
				}
				log("Read completed");
				for (int p = 1; p < PCJ.threadCount(); p++) {
					// log("Sending station data to node "+p);
					PCJ.put(allComputedStations, p, Shared.allComputedStations);
				}
				log("Write completed");
			}
		}
		PCJ.barrier();

		if (myPCJCity != null) {
			if (DEBUG) {
				log("After  " + myPCJCity);
			}

			// log(PCJ.myId() + " has information about "+allComputedStations.size() + "
			// stations");
			// FIXME:START
			if (buildings.size() > 0) {
				long cumulativeBuildingDistance = 0;
				// Find the closest existing station for all buildings
				for (String buildingCityName : ev.buildingByCityName.keySet()) {
					if (citiesByName.get(buildingCityName) != myPCJCity) {
						continue;
					}
					List<Building> buildingsInThisCity = ev.buildingByCityName.get(buildingCityName);
					List<Station> stationsInThisCity = allComputedStations;// ev.stationByCityName.get(buildingCityName);

					
					double lat = 0;
					double lon = 0;
					int sCounter = 0;
					for (Station station : stationsInThisCity) {
						if (station.multiplicity == 0 || !station.name.equals(buildingCityName)) {
							continue;
						}
						lat += station.lat * station.multiplicity;
						lon += station.lon * station.multiplicity;
						sCounter += station.multiplicity;
					}
					
					for (Building building : buildingsInThisCity) {
						long bestDistance = Long.MAX_VALUE;
						for (Station station : stationsInThisCity) {
							if (station.multiplicity == 0 || !building.name.equals(station.name)) {
								continue;
							}
							long dist = dist(building, station);
							if (bestDistance > dist) {
								bestDistance = dist;
							}
						}
						if (bestDistance != Long.MAX_VALUE) {
							cumulativeBuildingDistance += Math.sqrt(bestDistance);
						}
					}
					if (sCounter > 0) {
						lon /= sCounter;
						lat /= sCounter;
					}
					log("Cumulative building distance for " + myPCJCity + ": " + cumulativeBuildingDistance + " Center: ["+lat+", "+lon+"]");
					
					
				}
			}
			// FIXME:STOP

			// BARRIER
			long minimalDistanceSum = 0;
			for (City c0 : cities) {
				if (myPCJCity != c0) {
					continue;
				}
				// List<Station> stationsInCityC0 = ev.stationByCityName.get(c0.name);
				for (City c1 : cities) {
					if (c0 == c1) {
						continue;
					}
					// List<Station> stationsInCityC1 = ev.stationByCityName.get(c1.name);
					long bestDistance = Long.MAX_VALUE;
					for (Station s0 : allComputedStations) {
						if (!s0.name.equals(c0.name)) {
							continue;
						}
						if (s0.multiplicity <= 0) {
							continue;
						}
						for (Station s1 : allComputedStations) {
							if (!s1.name.equals(c1.name)) {
								continue;
							}
							if (s1.multiplicity <= 0) {
								continue;
							}
							long dist = dist(s0, s1);
							if (dist < bestDistance) {
								bestDistance = dist;
							}
						}
					}
					if (bestDistance == Long.MAX_VALUE) {
						throw new RuntimeException("Invalid sum calculation for " + c0 + " and " + c1);
					}
					minimalDistanceSum += Math.sqrt(bestDistance);
				}
			}

			minimalDistanceSumForNode = minimalDistanceSum;
			log("Minimal distance sum for node: " + PCJ.myId() + ": " + minimalDistanceSumForNode + " " + myPCJCity);
		}
		PCJ.barrier();

		if (myPCJCity != null) {
			long totalMinimalDistanceSum = minimalDistanceSumForNode;
			if (PCJ.myId() == 0) {
				for (int p = 1; p < PCJ.threadCount(); p++) {
					totalMinimalDistanceSum += +(long) PCJ.get(p, Shared.minimalDistanceSumForNode);
				}
				log("Total minimal distance sum: ");
				log(totalMinimalDistanceSum);

				if (DEBUG_PLACES) {
					// Trackpoints
					log("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>");
					log("<gpx version=\"1.0\">\n<name>EV - Michał Markiewicz, Nicholas Bochenski</name>");
					for (Station station : allComputedStations) {
						log(station.getXML());
					}
					log("</gpx>");

				}
			}
		}
		// log("Minimal distance sum: ");
		// log(minimalDistanceSum);

	}

	final static boolean DEBUG_PLACES = false;
	
	@Storage(EV03.class)
	enum Shared {
		minimalDistanceSumForNode, stationsComputedForThisCity, allComputedStations, abcTest;
	}

	public long minimalDistanceSumForNode;
	public LinkedList<Station> stationsComputedForThisCity = new LinkedList<Station>();
	public LinkedList<Station> allComputedStations = new LinkedList<Station>();
	public long abcTest = -1;

	private void initalizeStationsAndBuildings(List<City> cities, List<Station> stations, List<Building> buildings,
			int targetStationsCount) {
		int totalPopulation = 0;

		for (City c : cities) {
			totalPopulation += c.population;
		}

		for (City c : cities) {
			c.targetCount = (int) Math.round((double) c.population / totalPopulation * targetStationsCount);
		}

		for (Station s : stations) {
			City c = citiesByName.get(s.name);
			if (s.existing) {
				c.existingCount++;
			}
			List<Station> stationsInCity = stationByCityName.get(s.name);
			if (stationsInCity == null) {
				stationByCityName.put(s.name, stationsInCity = new LinkedList<Station>());
			}
			stationsInCity.add(s);
		}

		for (Building b : buildings) {
			List<Building> buildingsInCity = buildingByCityName.get(b.name);
			if (buildingsInCity == null) {
				buildingByCityName.put(b.name, buildingsInCity = new LinkedList<Building>());
			}
			buildingsInCity.add(b);
		}

	}

	public static void log(Object o) {
		System.out.println(o);
	}

}
