fun buildStationSchedule(station: String, timetable_data: JSON) {
    val schedule: Map<Time, TrainServiceInfo> = {}
    
    for (train_data in timetable_data) {

        // Iterate over every stop of every train in the timetable
        for (stop in train_data["stops"]) {

            // If the train stops at this station, add it to the schedule
            if (stop["station"] == station) {
                schedule[stop["time"]] = TrainServiceInfo.fromTrainData(train_data)
                break
            }
        }
    }

    return schedule
}

fun loadResources() {
    // Get unprocessed resource data from local file or remote source
    val resource_data: JSON
    if (cache_file.exists()) {
        resource_data = parseJson(cache_file.readFile())
    }
    else {
        resource_data = parseJson(getRequest(DATA_SOURCE_URL))
    }

    // Unpack stations from data
    val stations: Map<String, Station> = []
    for (station_data in resource_data["stations"]) {
        val station = Station()
        station.name = station_data["name"]
        
        // Build and set the station-perspective timetable data for this station
        station.schedule = buildStationSchedule(station.name, resource_data["timetable"])
        
        stations[station.name] = station
    }

    // Unpack timetable from data
    val timetable: List<TrainService> = []
    for (train_data in resource_data["timetable"]) {
        val train = TrainService()
        train.info = TrainServiceInfo.fromTrainData(train_data)
        train.cls = TrainClass.fromTrainInfo(train.info)
        
        // Unpack train schedule, referencing the stations instantiated earlier
        train.schedule = []
        for (stop in train_data["stops"]) {
            train.schedule[stop["time"]] = stations[stop["station"]]
            train.arrival_offsets[stop["time"]] = getRandomTimeOffset()
            train.departure_offsets[stop["time"]] = getRandomTimeOffset()
        }
    }

    return stations, timetable
}
