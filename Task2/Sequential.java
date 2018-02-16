int totalPlacedStations = 0;


final int STATIONS_ON_ROUTES = 3000;
final int STATIONS_IN_CITIES = 6000;


class Station {
  long distAlongRoute;
  boolean existingPoint;
  int numStationsAt;

  long lat, lon; // latitude, longitude
}


class Route {
  List<Station> stations;
  long length;
}




List<Route> allRoutes; // should contain stations from allStations, not copies
List<Station> allStations;




void doTask() {
  readData();
  placeStations();

  System.out.println("score: " + programScore());
  // print all stations as required by regulamin-san
  printStations(allStations);
}





void readData() {
  // TODO
}





void printStations(List<Station> stations) {
  int id = 1;
  for (Station s : stations) {
    if (s.numStationsAt <= 0) {
      continue;
    }
    System.out.println(id + " " + s.numStationsAt + " " + s.lat + " " + s.lon);
    id++;
  }
}





void placeStations() {
  long totalRouteLength = 0;
  for (Route route : allRoutes) {
    totalRouteLength += route.length;
  }

  for (Route route : allRoutes) {
    placeStationsOnRoute(route, route.length * STATIONS_ON_ROUTES / totalRouteLength);
  }

  // place remaining stations
  allStations.shuffle(); // random order. doable?
  for (Stations s : allStations) {
    if (totalPlacedStations >= STATIONS_ON_ROUTES + STATIONS_IN_CITIES) {
      break; // we've placed all the stations we needed
    }
    if (s.numStationsAt > 0) {
      continue;
    }

    s.numStationsAt++;
    totalPlacedStations++;
  }
}





void placeStationsOnRoute(Route route, int numStationsToPlace) {
  long targetDistBetweenStations = route.length / numStationsToPlace;
  long prevDistAlong = 0;
  for (Station s : route.stations) {
    if (s.numStationsAt > 0) {
      // a station was already there
      prevDistAlong = s.distAlongRoute;
      continue;
    }
    if (s.distAlongRoute - prevDistAlong >= targetDistBetweenStations) {
      // place a station, it's just about time
      s.numStationsAt++;
      totalPlacedStations++;
      prevDistAlong = s.distAlongRoute;
    }
  }

  // we might be left with some stations to place now - just put them wherever
  for (Station s : route.stations) {
    if (s.numStationsAt > 0) {
      // there's a station already there, probly makes no sense to put another one
      continue;
    }
    s.numStationsAt++;
    totalPlacedStations++;
  }

  // we might still be left with some stations to place (if we saturated the route)
  // don't worry, we'll place them wherever later on
}



long programScore(List<Route> routes) {
  long result = 0;
  for (Route route : routes) {
    result += greatestDistanceOnRoute(route);
  }
  return result;
}



long greatestDistanceOnRoute (Route route) {
  long result = 0;
  long prevDistAlong = 0;
  for (Station s : route.stations) {
    if (s.numStationsAt > 0) {
      result = Math.max(result, s.distAlongRoute - prevDistAlong);
      prevDistAlong = s.distAlongRoute;
    }
  }
  return result;
}
