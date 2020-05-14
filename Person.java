import java.util.ArrayList;

public class Person {

    private int id;
    private int status; //0=healthy/uninfected, 1=asymptotic, 2=symptotic, 3=recovered/immune, 4=dead
    private int timeSince;
    private Coordinate c;
    private ArrayList<Person> family;
    private ArrayList<Person> friends;
    private Coordinate home;
    private ArrayList<Coordinate> familyHome; 
    private Coordinate desination;

    public Person(int id, int status, Coordinate c, ArrayList<Person> family) {
        this.setId(id);
        this.setStatus(status);
        this.setC(c);
        this.setHome(c);
        this.setFamily(family);
        this.friends = new ArrayList<Person>();
        this.desination = null;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public int getStatus() {
        return status;
    }


    public void setStatus(int status) {
        this.status = status;
    }


    public int getTimeSince() {
        return timeSince;
    }


    public void setTimeSince(int timeSince) {
        this.timeSince = timeSince;
    }


    public Coordinate getC() {
        return c;
    }


    public void setC(Coordinate c) {
        this.c = c;
    }


    public ArrayList<Person> getFamily() {
        return family;
    }


    public void setFamily(ArrayList<Person> family) {
        this.family = family;
    }

    public void addFriend(Person p) {
        this.friends.add(p);
    }


    public Coordinate getHome() {
        return home;
    }


    public Coordinate getDesination() {
        return desination;
    }


    public void setDesination(Coordinate desination) {
        this.desination = desination;
    }


    public ArrayList<Person> getFriends() {
        return friends;
    }


    public void setHome(Coordinate home) {
        this.home = home;
    }


    public ArrayList<Coordinate> getFamilyHome() {
        return familyHome;
    }


    public void setFamilyHome(ArrayList<Coordinate> familyHome) {
        this.familyHome = familyHome;
    }
    
    

}
