package ticket.booking.entities;


import java.util.List;
import java.util.Map;

public class Train {

    private String trainId;

    private String trainNo;

    private List<List<Integer>> seats;

    private Map<String, String> stations;;

    public Train () {}  // default constructor

    public Train(String trainId, String trainNo, List<List<Integer>> seats, Map<String, String> stations) {
        this.trainId = trainId;
        this.trainNo = trainNo;
        this.seats = seats;
        this.stations = stations;
    }

    public String getTrainId() {
        return trainId;
    }

    public void setTrainId(String trainId) {
        this.trainId = trainId;
    }

    public String getTrainNo() {
        return trainNo;
    }

    public void setTrainNo(String trainNo) {
        this.trainNo = trainNo;
    }

    public List<List<Integer>> getSeats() {
        return seats;
    }

    public void setSeats(List<List<Integer>> seats) {
        this.seats = seats;
    }

    public Map<String, String> getStations() {
        return stations;
    }

    public void setStations(Map<String, String> stations) {
        this.stations = stations;
    }
}
