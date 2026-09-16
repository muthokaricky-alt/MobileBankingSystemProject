package wire;

public class User {
    private int id;
    private String name;
    private String idNumber;
    private String phoneNumber;
    private String pin;
    private double balance;

    public User(int id, String name, String idNumber, String phoneNumber, String pin, double balance) {
        this.id = id;
        this.name = name;
        this.idNumber = idNumber;
        this.phoneNumber = phoneNumber;
        this.pin = pin;
        this.balance = balance;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getIdNumber() { return idNumber; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getPin() { return pin; }
    public double getBalance() { return balance; }

    // Setters
    public void setBalance(double balance) { this.balance = balance; }
    public void setPin(String pin) { this.pin = pin; }
}