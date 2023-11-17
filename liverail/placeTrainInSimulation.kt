fun placeTrainInSimulation(train: TrainServiceInfo, position: TrainPosition) {
    val model: TrainModel = getOrCreateTrainModel(train)
    val track_section: TrackSection = getTrackSection(position.previous_station, position.next_station)

    var first_carriage_placed: Boolean = false
    var prev_bogey_position: Float? = null

    // Iterate carriages from front to rear
    for (carriage in model.carriages) {
        var prev_bogey: Bogey? = null

        // Iterate bogeys from front to rear
        for (bogey in carriage.bogeys) {
            val distance_from_last_bogey: Float
            
            if (prev_bogey == null) {
                prev_bogey = bogey

                if (!first_carriage_placed) {
                    // Place first bogey of first carriage at passed `position`
                    placeBogeyOnTrack(bogey, track_section, position.progress)
                    prev_bogey_position = position.progress
                    first_carriage_placed = true
                    continue
                }
                else {
                    // Place first bogey of subsequent carriages at fixed distance behind last bogey
                    distance_from_last_bogey = train.cls.distance_between_carriages
                }
            }
            else {
                distance = bogey.distanceFromBogey(prev_bogey)
            }

            val intersections: List<Float> = track_section.intersectCircle(center = prev_bogey_position, radius = distance)
            for (intersection in intersections) {
                // Reject intersection point if it's ahead of the last bogey
                if (intersection >= prev_bogey_position) {
                    continue
                }

                // Use the first intersection behind the last bogey as this bogey's position
                prev_bogey_position = intersection
                placeBogeyOnTrack(bogey, track_section, intersection)
                break
            }

            prev_bogey = bogey
        }
    }
}

fun placeBogeyOnTrack(bogey: Bogey, track_section: TrackSection, progress: Float) {
    bogey.position = track_section.getPointAt(progress)
    bogey.rotation = track_section.getTangentAt(progress).rotation
}
