fun getTrainPosition(train: TrainServiceInfo, current_time: Time): TrainPosition? {
    val schedule: Map<Time, Station> = applyOffsetsToTrainScedule(train.schedule, train.arrival_offsets, train.departure_offsets)

    val previous_station: Station? = null
    val previous_time: Time
    val next_station: Station? = null

    for (time, station in schedule) {
        if (time <= current_time) {
            previous_station = station
            previous_time = time
        }
        // current_time < time
        else {
            next_station = station
            break
        }
    }

    if (previous_station == null) {
        // No previous station, so train is not in service yet
        return null
    }

    if (next_station == null) {
        // No next station, so train has already finished service
        return null
    }

    val time_in_section: Time = current_time - previous_time
    val total_distance: Distance = getTrackSection(previous_station, next_station).getLength()

    val current_distance: Distance

    // v = u + at
    // t = (v - u) / a
    val time_to_accelerate: Time = train.cls.maximum_speed / train.cls.acceleration
    val time_to_decelerate: Time = train.cls.maximum_speed / train.cls.deceleration

    // s = 0.5 * (v+u) * t
    val acceleration_distance: Distance = 0.5 * train.cls.maximum_speed * time_to_accelerate
    val deceleration_distance: Distance = 0.5 * train.cls.maximum_speed * time_to_decelerate

    val distance_at_speed: Distance = total_distance - acceleration_distance - deceleration_distance

    // t = s / (0.5 * (v+u))
    val time_at_speed: Time = distance_at_speed / (0.5 * train.cls.maximum_speed)
    
    val total_time: Time = time_to_accelerate + time_at_speed + time_to_decelerate

    if (time_in_section <= time_to_accelerate) {
        // Train is accelerating
        
        // s = ut + 0.5at^2
        current_distance = 0.5 * train.acceleration * current_time.pow(2)
    }
    else if (time_in_section >= total_time - time_to_decelerate) {
        // Train is decelerating

        // s = ut + 0.5at^2
        val decelerating_time: Time = time_in_section - time_at_speed - time_to_accelerate
        current_distance = 0.5 * train.deceleration * decelerating_time.pow(2)
    }
    else {
        // Train is travelling at maximum speed
        
        val time_since_acceleration: Time = time_in_section - time_to_accelerate
        val distance_since_acceleration: Distance = time_since_acceleration * train.cls.maximum_speed

        current_distance = acceleration_distance + distance_since_acceleration
    }

    val train_position = TrainPosition()
    train_position.previous_station = previous_station
    train_position.next_station = next_station
    train_position.progress = current_distance / total_distance

    return train_position
}

class TrainService { 
    val info: TrainServiceInfo 
    val class: TrainClass 
    val schedule: Map<Time, Station> 

    val arrival_offsets: Map<Time, Duration> 
    val departure_offsets: Map<Time, Duration> 

    fun getPosition(time: Time, apply_offsets: bool): TrainPosition 
} 
