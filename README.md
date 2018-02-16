# smart-charge
Smart Charge hackathon - efficient parallel computation of positioning of charging points for electric vehicles.
## Task one
We minimize the distance between nearest charging points in different cities.
For further optimization of solution, we take population & building density into account.
All cities, not only Warsaw and Wroclaw, are optimized for the stations to reflect population density and transit needs - we took the positioning of railway stations, airports and similar into account, which naturally results in the charging stations being placed where they are most needed.
The result is obtained by dividing each city into Voronoi areas, one for each potential charging point. Potential charging point locations that have more buildings in their area are considered more important, and thus are more often selected as final charging point locations.
A weighted randomization allows for re-running of the algorithm and obtaining a different result. A logical next step would be to introduce genetic selection in the results, and use several epochs of training.
## Task two
We minimize the longest distance cars have to cover without recharging when traveling between cities.
## Locations considered for charging station placement
building=transformer_tower
shop=supermarket
office=goverment
building=civic
railway=station
shop=mall
amenity=fast_food
amenity=food_court
amenity=fuel
name like “Stacja Paliw”
aeroway=aerodrome
building=stadium
power=plant
Parkingi powyżej 100m2, amenity=parking
