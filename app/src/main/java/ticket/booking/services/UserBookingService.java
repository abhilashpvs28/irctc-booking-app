package ticket.booking.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import ticket.booking.entities.Ticket;
import ticket.booking.entities.Train;
import ticket.booking.entities.User;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class UserBookingService {

    private static final String USERS_PATH  = "app/src/main/java/ticket/booking/localDb/users.json";
    private static final String TRAINS_PATH = "app/src/main/java/ticket/booking/localDb/trains.json";
    private static final String DATE_FMT    = "dd-MM-yyyy";

    private final ObjectMapper mapper = new ObjectMapper();
    private final List<User>  userList   = new ArrayList<>();
    private final List<Train> trainsList = new ArrayList<>();
    private User currentUser;

    // --------- tiny helpers (DRY) ---------
    private String n(String s) { return s == null ? "" : s.trim(); }

    /** require login once */
    private boolean requireLogin() {
        if (currentUser == null) {
            System.out.println("Please Login First");
            return false;
        }
        return true;
    }

    /** require non-empty fields with simple message */
    private boolean requireFields(Map<String, String> fields) {
        List<String> missing = new ArrayList<>();
        for (Map.Entry<String, String> e : fields.entrySet()) {
            if (e.getValue() == null || e.getValue().trim().isEmpty()) {
                missing.add(e.getKey());
            }
        }
        if (!missing.isEmpty()) {
            System.out.println("Missing/empty fields: " + String.join(", ", missing));
            return false;
        }
        return true;
    }

    private Date parseDateStrict(String ddMMyyyy) {
        try {
            return new java.text.SimpleDateFormat(DATE_FMT).parse(n(ddMMyyyy));
        } catch (Exception e) {
            System.out.println("Invalid date format. Use " + DATE_FMT);
            return null;
        }
    }
    // --------------------------------------

    public UserBookingService() throws IOException {
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        File userFile   = new File(USERS_PATH);
        File trainsFile = new File(TRAINS_PATH);

        if (userFile.exists()) {
            List<User> loaded = mapper.readValue(userFile, new TypeReference<List<User>>() {});
            if (loaded != null) userList.addAll(loaded);
        } else {
            File parent = userFile.getParentFile();
            if (parent != null) parent.mkdirs();
            userFile.createNewFile();
            mapper.writeValue(userFile, userList);
        }

        if (trainsFile.exists()) {
            List<Train> loadedTrains = mapper.readValue(trainsFile, new TypeReference<List<Train>>() {});
            if (loadedTrains != null) trainsList.addAll(loadedTrains);
        } else {
            File tp = trainsFile.getParentFile();
            if (tp != null) tp.mkdirs();
            trainsFile.createNewFile();
            mapper.writeValue(trainsFile, trainsList);
        }

        boolean changed = false;
        for (User u : userList) {
            if (u.getTicketsBooked() == null) continue;
            for (Ticket t : u.getTicketsBooked()) {
                if (n(t.getUserId()).isEmpty())   { t.setUserId(u.getUserId()); changed = true; }
                if (n(t.getTicketId()).isEmpty()) { t.setTicketId(UUID.randomUUID().toString()); changed = true; }
            }
        }
        if (changed) save();

        System.out.println("Loaded users: " + userList.size());
        System.out.println("Loaded trains: " + trainsList.size());
    }

    // --------- auth & basic ---------
    public boolean login(String name, String password) {
        if (!requireFields(mapOf("username", name, "password", password))) return false;

        String inName = n(name), inPwd = n(password);
        for (User u : userList) {
            if (inName.equals(n(u.getName()))) {
                if (inPwd.equals(n(u.getHashedPassword()))) { currentUser = u; return true; }
                return false;
            }
        }
        return false;
    }

    public void fetchBooking() {
        if (!requireLogin()) return;
        currentUser.printTickets();
    }

    public User getCurrentUser() { return currentUser; }

    private void save() throws IOException { mapper.writeValue(new File(USERS_PATH), userList); }

    // --------- bookings list/cancel ---------
    public void fetchBookingWithIndex() {
        if (!requireLogin()) return;
        if (currentUser.getTicketsBooked().isEmpty()) {
            System.out.println("No Tickets Founded.");
            return;
        }
        for (int i = 0; i < currentUser.getTicketsBooked().size(); i++) {
            System.out.println((i + 1) + ". " + currentUser.getTicketsBooked().get(i).getTicketInfo());
        }
    }

    public boolean cancelBookingByIndex(int oneBasedIndex) {
        if (!requireLogin()) return false;

        if (oneBasedIndex < 1 || oneBasedIndex > currentUser.getTicketsBooked().size()) {
            System.out.println("Invalid Index.");
            return false;
        }
        currentUser.getTicketsBooked().remove(oneBasedIndex - 1);
        try {
            save();
            System.out.println("Tickets Cancelled");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean cancelBookingById(String ticketId) {
        if (!requireLogin()) return false;
        if (!requireFields(mapOf("ticketId", ticketId))) return false;

        boolean removed = currentUser.cancelTicket(n(ticketId));
        if (!removed) {
            System.out.println("Ticket Not fount with the Current user.");
            return false;
        }
        try {
            save();
            System.out.println("✅ Cancelled Ticket");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --------- signup ---------
    public boolean signUp(String name, String password){
        if (!requireFields(mapOf("username", name, "password", password))) return false;

        String uname = n(name);
        for (User u : userList) {
            if (uname.equalsIgnoreCase(u.getName())) {
                System.out.println("User Already Exists.");
                return false;
            }
        }

        User nu = new User();
        nu.setName(uname);
        nu.setPassword(password);
        nu.setHashedPassword(password);
        nu.setUserId(UUID.randomUUID().toString());
        nu.setTicketsBooked(new ArrayList<>());
        userList.add(nu);

        try {
            save();
            System.out.println("✅ Signup SuccessFully. Please Log-In");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --------- trains (list/search/find) ---------
    public void listTrains(){
        if (trainsList.isEmpty()) {
            System.out.println("No Trains Available");
            return;
        }
        for (Train t : trainsList) {
            System.out.print("Train " + t.getTrainNo() + " (" + t.getTrainId() + "): ");
            String route = String.join(" -> ", new ArrayList<>(t.getStations()));
            System.out.println(route);
        }
    }

    public List<Train> searchTrain(String from, String to) {
        if (from == null || to == null) return Collections.emptyList();

        String f = n(from).toLowerCase();
        String d = n(to).toLowerCase();

        return trainsList.stream()
                .filter(t -> {
                    List<String> stations = t.getStations();
                    if (stations == null) return false;
                    Set<String> lower = stations.stream().map(String::toLowerCase).collect(Collectors.toSet());
                    return lower.contains(f) && lower.contains(d);
                })
                .collect(Collectors.toList());
    }

    /** match by train number OR train id (both trimmed) */
    private Train findTrainByNo(String input) {
        if (input == null) return null;
        String key = n(input);

        return trainsList.stream()
                .filter(t -> key.equals(n(t.getTrainNo())) || key.equalsIgnoreCase(n(t.getTrainId())))
                .findFirst()
                .orElse(null);
    }

    /** ensure from appears before to (case-insensitive compare) */
    private boolean trainCoversRoute(Train t, String from, String to) {
        if (t.getStations() == null) return false;
        List<String> s = t.getStations();

        int iFrom = -1, iTo = -1;
        for (int i = 0; i < s.size(); i++) {
            String st = s.get(i);
            if (iFrom < 0 && st.equalsIgnoreCase(from)) iFrom = i;
            if (iTo   < 0 && st.equalsIgnoreCase(to))   iTo   = i;
        }
        return iFrom >= 0 && iTo >= 0 && iFrom < iTo;
    }

    // --------- ticket creation + booking ---------
    Ticket makeTicket(String from, String to, Date date, Train chosen){
        Ticket ticket = new Ticket();
        ticket.setTicketId(UUID.randomUUID().toString());
        ticket.setUserId(currentUser.getUserId());
        ticket.setSource(from);
        ticket.setDestination(to);
        ticket.setDateOfTravel(date);
        ticket.setTrain(chosen);
        return ticket;
    }

    private boolean trySave(String ticketId){
        try{
            save();
            System.out.println("✅ Ticket booked! ID: " + ticketId);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Java 8 replacement for Map.of(...)
    private static Map<String, String> mapOf(String... kv) {
        LinkedHashMap<String, String> m = new LinkedHashMap<>();
        for (int i = 0; i + 1 < kv.length; i += 2) {
            m.put(kv[i], kv[i + 1]);
        }
        return m;
    }

    public boolean bookTicket(String from, String to, String dateStr, String trainNo){
        if (!requireLogin()) return false;
        if (!requireFields(mapOf("from", from, "to", to, "date", dateStr, "trainNo", trainNo))) return false;

        String f = n(from), d = n(to), no = n(trainNo);

        Train chosen = findTrainByNo(no);
        if (chosen == null) {
            System.out.println("Train not found.");
            return false;
        }

        if (!trainCoversRoute(chosen, f, d)) {
            System.out.println("This train does not cover the selected route.");
            return false;
        }

        Date travelDate = parseDateStrict(dateStr);
        if (travelDate == null) return false;

        Ticket t = makeTicket(f, d, travelDate, chosen);
        currentUser.getTicketsBooked().add(t);
        return trySave(t.getTicketId());
    }
}

// serialize -> Java object to JSON
// deserialize -> JSON to Java object
