public class Passenger {
    private String firstName;
    private String surname;
    private int secondsInQueue = 0;
    private int seatNumber;

    public String getName() {
        return firstName + " " + surname;
    }

    public void setName(String name, String surName) {
        this.firstName = name;
        this.surname = surName;
    }

    public int getSecondsInQueue() {
        return secondsInQueue;
    }

    public void setSecondsInQueue(int sec) {
        this.secondsInQueue = sec;
    }

    public void display() {
        System.out.println("--------------------------------------");
        System.out.println("***       passenger details        ***");
        System.out.println("--------------------------------------");
        System.out.println("name = "+getName());
        System.out.println("seat = "+getSeatNumber());
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(int seat) {
        this.seatNumber = seat;
    }
}