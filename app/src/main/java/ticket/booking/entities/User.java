package ticket.booking.entities;

import java.util.ArrayList;
import java.util.List;

public class User {

    private String name;

    private String password;

    private String hashedPassword;

    private List<Ticket> ticketsBooked;

    private String userId;

    public User() {         // default constructor
            ticketsBooked = new ArrayList<>();
    }

    public User(String name, String password, String hashedPassword, List<Ticket> ticketsBooked, String userId) {
        this.name = name;
        this.password = password;
        this.hashedPassword = hashedPassword;
        this.ticketsBooked = (ticketsBooked != null) ? ticketsBooked : new ArrayList<>();
        this.userId = userId;
    }



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHashedPassword() { return hashedPassword; }
    public void setHashedPassword(String hashedPassword) { this.hashedPassword = hashedPassword; }

    public List<Ticket> getTicketsBooked() {
        return ticketsBooked;
    }

    public void setTicketsBooked(List<Ticket> ticketsBooked) {
        this.ticketsBooked = (ticketsBooked != null) ? ticketsBooked : new ArrayList<>();
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    // --- Utility Methods ---
    public void printTickets () {
        if (ticketsBooked.isEmpty()){
            System.out.println("No Tickets Booked");
        }else {
            for (Ticket ticket : ticketsBooked){
                System.out.println(ticket.getTicketInfo());
            }
        }
    }

    public boolean cancelTickets(String ticketId){
        return ticketsBooked.removeIf(ticket -> ticket.getTicketId().equals(ticketId));
    }


}
