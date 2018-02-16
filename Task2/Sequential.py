totalPlacedStations = 0


STATIONS_ON_ROUTES = 3000
STATIONS_IN_CITIES = 6000


class Station:
  distAlongRoute = 0
  existingPoint = False
  numStationsAt = 0

  lat = 0
  lon = 0


class Route:
  stations = []
  length = 0




allRoutes = [] #should contain stations from allStations, not copies
allStations = []




def doTask():
  readData()
  placeStations()

  System.out.println("score: " + programScore())
  # print all stations as required by regulamin-san
  printStations(allStations)





def readData():
  pass # TODO





def printStations(stations):
  id = 1
  for s in stations:
    if s.numStationsAt <= 0:
      continue
    print(id + " " + s.numStationsAt + " " + s.lat + " " + s.lon)
    id += 1




def placeStations():
  totalRouteLength = 0
  for route in allRoutes:
    totalRouteLength += route.length

  for route in allRoutes:
    placeStationsOnRoute(route, route.length * STATIONS_ON_ROUTES / totalRouteLength)

  # place remaining stations
  allStations.shuffle() # random order. doable?
  for s in allStations:
    if totalPlacedStations >= STATIONS_ON_ROUTES + STATIONS_IN_CITIES:
      break # we've placed all the stations we needed
    if s.numStationsAt > 0:
      continue

    s.numStationsAt += 1
    totalPlacedStations += 1





def placeStationsOnRoute(route, numStationsToPlace):
  targetDistBetweenStations = route.length / numStationsToPlace
  prevDistAlong = 0
  for s in route.stations:
    if s.numStationsAt > 0:
      # a station was already there
      prevDistAlong = s.distAlongRoute
      continue
    if s.distAlongRoute - prevDistAlong >= targetDistBetweenStations:
      # place a station, it's just about time
      s.numStationsAt += 1
      totalPlacedStations += 1
      prevDistAlong = s.distAlongRoute

  # we might be left with some stations to place now - just put them wherever
  for s in route.stations:
    if s.numStationsAt > 0:
      # there's a station already there, probly makes no sense to put another one
      continue
    s.numStationsAt += 1
    totalPlacedStations+= 1
  # we might still be left with some stations to place (if we saturated the route)
  # don't worry, we'll place them wherever later on



def programScore(routes):
  result = 0
  for route in routes:
    result += greatestDistanceOnRoute(route)
  return result



def greatestDistanceOnRoute (route):
  result = 0
  prevDistAlong = 0
  for s in route.stations:
    if s.numStationsAt > 0:
      result = Math.max(result, s.distAlongRoute - prevDistAlong)
      prevDistAlong = s.distAlongRoute
  return result
