// returns: true if optimization succeeded and is no longer needed
boolean optimizationStep(List<Station> stations) {
  // find a station to remove
  Station toRemove = null;
  for (Station s : stations) {
    if (s.numStationsAt <= 0) {
      continue;
    }
    s.numStationsAt--;
    s.quality = distance(cityCenters.get(s.name), centerPos(stations));
    s.numStationsAt++;

    if (toRemove == null || s.quality < toRemove.quality) {
      toRemove = s;
    }
  }
  if (toRemove == null) {
    throw new RuntimeException("No stations in optimization step");
  }
  toRemove.numStationsAt--;

  // similarly, find a station to add - balance must be kept now!
  Station toAdd = null;
  for (Station s : stations) {
    if (s.numStationsAt >= 6) { // TODO: non-hardcoded value 6
      continue;
    }
    s.numStationsAt++;
    s.quality = -distance(cityCenters.get(s.name), centerPos(stations));
    s.numStationsAt--;

    if (toAdd == null || s.quality > toAdd.quality) {
      toAdd = s;
    }
  }
  if (toAdd == null) {
    throw new RuntimeException("No stations in optimization step");
  }
  toAdd.numStationsAt++;


  return isPosCentral(centerPos(stations));
}


class Pos {
  long x, y;
}


// returns: true if given pos is in a city center (be it Warsaw or Wroclaw, doesn't matter)
cityCenters = {"Warszawa": TODO, "Wrocław": TODO};

boolean isPosCentral(Pos pos) {
  return dist(pos, cityCenters.get("Warszawa")) < 200 * 200 ||
         dist(pos, cityCenters.get("Wrocław")) < 100 * 100;
}



Pos centerPos(List<Station> stations) {
  Pos result = new Pos(); // is this the way you do it?
  for (Station s : stations) {
    result.x += s.x * s.numStationsAt;
    result.y += s.y * s.numStationsAt;
  }
  result.x /= stations.Count; // or sth like this
  result.y /= stations.Count;
  return result;
}
