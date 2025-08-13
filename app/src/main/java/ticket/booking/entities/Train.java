package ticket.booking.entities;

import java.util.List;
import java.util.Map;

public class Train {

    private String trainId;                 // "train_id"
    private String trainNo;                 // "train_no" (number in JSON; String is OK)
    private List<List<Integer>> seats;      // "seats"
    private List<String> stations;          // "stations" as array of names
    private Map<String, String> stationTimes; // "station_times" (optional but useful)

    public Train() {}

    public Train(String trainId, String trainNo,
                 List<List<Integer>> seats, List<String> stations) {
        this.trainId = trainId;
        this.trainNo = trainNo;
        this.seats = seats;
        this.stations = stations;
    }

    public String getTrainId() { return trainId; }
    public void setTrainId(String trainId) { this.trainId = trainId; }

    public String getTrainNo() { return trainNo; }
    public void setTrainNo(String trainNo) { this.trainNo = trainNo; }

    public List<List<Integer>> getSeats() { return seats; }
    public void setSeats(List<List<Integer>> seats) { this.seats = seats; }

    public List<String> getStations() { return stations; }
    public void setStations(List<String> stations) { this.stations = stations; }

    public Map<String, String> getStationTimes() { return stationTimes; }
    public void setStationTimes(Map<String, String> stationTimes) { this.stationTimes = stationTimes; }
}
