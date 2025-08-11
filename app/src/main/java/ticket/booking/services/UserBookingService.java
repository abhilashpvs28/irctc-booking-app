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
            List<User> loaded = mapper.readValue(userFile, new TypeReference<List<User>>() {});
            if (loaded != null) userList.addAll(loaded);
        } else {
            File parent = userFile.getParentFile();
            if (parent != null) parent.mkdirs();
            userFile.createNewFile();
            mapper.writeValue(userFile, userList);
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
        String inPwd  = password.trim();

        for (User u : userList) {
            String uname = (u.getName() != null) ? u.getName().trim() : "";
            String hpwd  = (u.getHashedPassword() != null) ? u.getHashedPassword().trim() : "";

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

    public User getCurrentUser() { return currentUser; }
}

// serialize -> Java object to JSON
// deserialize -> JSON to Java object
