import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

public class EV02WAW {

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

	class Station {
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

	public final static String EXISTING_FILE = "EXISTING.txt";
	public final static String POTENTIAL_FILE = "POTENTIAL.txt";

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

	public static void main(String[] args) {
		EV02WAW ev = new EV02WAW();
		Random random = new Random();

		List<City> cities = ev.readCities();
		List<Station> stations = ev.readStations();

		final int TARGET_STATIONS = 6000;
		List<Building> buildings = ev.readBuildings();
		ev.initalizeStationsAndBuildings(cities, stations, buildings, TARGET_STATIONS);
		// FIXME:START
		// Find the closest station for all buildings
		for (String buildingCityName : ev.buildingByCityName.keySet()) {
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
			log("Before");
			for (City c : cities) {
				log(c);
			}
		}

		// CPU
		for (City c0 : cities) {
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
		}

		if (DEBUG) {
			log("After");
			for (City c : cities) {
				log(c);
			}
		}

		// FIXME:START
		if (buildings.size() > 0) {
			long cumulativeBuildingDistance = 0;
			// Find the closest existing station for all buildings
			for (String buildingCityName : ev.buildingByCityName.keySet()) {
				List<Building> buildingsInThisCity = ev.buildingByCityName.get(buildingCityName);
				List<Station> stationsInThisCity = ev.stationByCityName.get(buildingCityName);

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
				log("Cumulative building distance: " + cumulativeBuildingDistance + " for " + buildingCityName);
			}
		}
		// FIXME:STOP

		// BARRIER
		long minimalDistanceSum = 0;
		for (City c0 : cities) {
			List<Station> stationsInCityC0 = ev.stationByCityName.get(c0.name);
			for (City c1 : cities) {
				if (c0 == c1) {
					continue;
				}
				List<Station> stationsInCityC1 = ev.stationByCityName.get(c1.name);
				long bestDistance = Long.MAX_VALUE;
				for (Station s0 : stationsInCityC0) {
					if (s0.multiplicity <= 0) {
						continue;
					}
					for (Station s1 : stationsInCityC1) {
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
		log("Minimal distance sum: ");
		log(minimalDistanceSum);

	}

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
