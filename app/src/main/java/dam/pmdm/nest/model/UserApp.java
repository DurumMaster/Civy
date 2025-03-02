package dam.pmdm.nest.model;

public class UserApp {

    private String avatar;
    private String firstName;
    private String lastName;
    private String floor;
    private String block;
    private String phone;
    private boolean isAdmin;

    public UserApp() {}

    public UserApp(String avatar, String firstName, String lastName, String floor, String block, String phone, boolean isAdmin) {
        this.avatar = avatar;
        this.firstName = firstName;
        this.lastName = lastName;
        this.floor = floor;
        this.block = block;
        this.phone = phone;
        this.isAdmin = isAdmin;
    }

    public UserApp(String avatar, String firstName, String lastName, String floor, String block, String phone) {
        this.avatar = avatar;
        this.firstName = firstName;
        this.lastName = lastName;
        this.floor = floor;
        this.block = block;
        this.phone = phone;
    }

    public UserApp(String avatar, String firstName, String lastName) {
        this.avatar = avatar;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getAvatar() {return avatar;}

    public void setAvatar(String avatar) {this.avatar = avatar;}

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public String getBlock() {
        return block;
    }

    public void setBlock(String block) {
        this.block = block;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }
}
