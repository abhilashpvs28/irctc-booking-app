package ticket.booking.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import ticket.booking.entities.User;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UserBookingService {

    private static final String USERS_PATH = "app/src/main/java/ticket/booking/localDb/users.json";

    private final ObjectMapper mapper = new ObjectMapper();
    private final List<User> userList = new ArrayList<>();
    private User currentUser;

    public UserBookingService() throws IOException {
        // Map snake_case JSON fields (user_id, hashed_password, tickets_booked) to camelCase in Java
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        File userFile = new File(USERS_PATH);
        System.out.println("Reading users from: " + userFile.getAbsolutePath());

        if (userFile.exists()) {
            List<User> loaded = mapper.readValue(userFile, new TypeReference<List<User>>() {
            });
            if (loaded != null) userList.addAll(loaded);
        } else {
            File parent = userFile.getParentFile();
            if (parent != null) parent.mkdirs();
            userFile.createNewFile();
            mapper.writeValue(userFile, userList);
        }

        boolean changed = false;

        for (User u : userList) {
            if (u.getTicketsBooked() == null) continue;
            for (ticket.booking.entities.Ticket t : u.getTicketsBooked()) {
                if (t.getUserId() == null || t.getUserId().trim().isEmpty()) {
                    t.setUserId(u.getUserId());
                    changed = true;
                }
                if (t.getTicketId() == null || t.getTicketId().trim().isEmpty()) {
                    t.setTicketId(java.util.UUID.randomUUID().toString());
                    changed = true;
                }
            }
        }
        if (changed) {
            save();
        }

        // Quick debug
        System.out.println("Loaded users: " + userList.size());
        for (User u : userList) {
            System.out.println(" - " + u.getName() + " | " + u.getHashedPassword());
        }
    }

    public boolean login(String name, String password) {
        if (name == null || name.trim().isEmpty()) return false;
        if (password == null) return false;

        String inName = name.trim();
        String inPwd = password.trim();

        for (User u : userList) {
            String uname = (u.getName() != null) ? u.getName().trim() : "";
            String hpwd = (u.getHashedPassword() != null) ? u.getHashedPassword().trim() : "";

            if (inName.equals(uname)) {
                if (inPwd.equals(hpwd)) {
                    currentUser = u;
                    return true;
                } else {
                    return false; // name matched but password wrong
                }
            }
        }
        return false; // no such user
    }

    public void fetchBooking() {
        if (currentUser == null) {
            System.out.println("Please Login First.");
        } else {
            currentUser.printTickets();
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }

    private void save() throws IOException {
        mapper.writeValue(new File(USERS_PATH), userList);
    }

    /**
     * Show bookings with numbers (1-based) so user can choose easily
     */
    public void fetchBookingWithIndex() {
        if (currentUser == null) {
            System.out.println("Please Login First");
            return;
        }

        if (currentUser.getTicketsBooked().isEmpty()) {
            System.out.println("No Tickets Founded.");
            return;
        }

        for (int i = 0; i < currentUser.getTicketsBooked().size(); i++) {
            System.out.println((i + 1) + ". " + currentUser.getTicketsBooked().get(i).getTicketInfo());
        }
    }

    ;

    /**
     * Cancel by the number shown in fetchBookingWithIndex (1-based)
     */
    public boolean cancelBookingByIndex(int oneBasedIndex) {
        if (currentUser == null) {
            System.out.println("First Login Please");
            return false;
        }

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
        if (currentUser == null) {
            System.out.println("Please Login First");
            return false;
        }

        if (ticketId == null || ticketId.trim().isEmpty()) {
            System.out.println("Please Enter a Valid TickedId.");
            return false;
        }

        boolean removed = currentUser.cancelTicket(ticketId.trim());
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

    // For SignUp

    public boolean signUp(String name, String password){
        if(name == null || name.trim().isEmpty()){
            System.out.println("Please Enter the username.");
            return false;
        }

        if(password == null || password.trim().isEmpty()){
            System.out.println("Please Enter the password");
            return false;
        }

        String uname = name.trim();

        // no duplicate names (case-insensitive)

        for(User u : userList){
            if(uname.equalsIgnoreCase(u.getName())){
                System.out.println("User Already Exists.");
                return false;
            }
        }

        ticket.booking.entities.User nu = new ticket.booking.entities.User();
        nu.setName(uname);
        nu.setPassword(password);
        nu.setHashedPassword(password);
        nu.setUserId(java.util.UUID.randomUUID().toString());
        nu.setTicketsBooked(new ArrayList<>());

        userList.add(nu);

        try{
            save();
            System.out.println("✅ Signup SuccessFully. Please Log-In");
            return true;
        } catch(IOException e){
            e.printStackTrace();
            return false;
        }
    }
}

// serialize -> Java object to JSON
// deserialize -> JSON to Java object
