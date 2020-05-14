import java.util.Objects;

/**
 * The Coordinate class defines an X and Y coordinates. 
 */
public class Coordinate {

    private int x, y;

    /**
     * Constructor for the Coordinate class.
     *
     * @param x The x component of the coordinate
     * @param y The y component of the coordinate
     */
    public Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * @return The x component of the coordinate.
     */
    public int getX() {
        return this.x;
    }

    /**
     * @return The y component of the coordinate.
     */
    public int getY() {
        return this.y;
    }

    /**
     * @return A String representation of the Coordinate object. Note that we use the String.format
     * method to more easily construct strings.
     */
    @Override
    public String toString() {
        return String.format("x: %d, y: %d", this.getX(), this.getY());
    }
    
    /**
     * Java's default implementation of equals() will only return true if two references point to
     * the same object in memory as it compares memory locations with == operator rather than
     * comparing contents. If two objects having same data are stored at different locations in
     * memory, the default implementation will return false. 
     * <p>
     * Therefore, when we define our own data types (classes), we need to override equals().
     * <p>
     * More info can be found at:
     * https://docs.oracle.com/javase/8/docs/api/java/lang/Object.html#equals-java.lang.Object-
     *
     * @return true if this object is the same as the obj argument; false otherwise.
     */
    @Override
    public boolean equals(Object obj) { 
        if (obj == this) { 
            return true; 
        }
        if (!(obj instanceof Coordinate)) { 
            return false; 
        } 

        Coordinate coord = (Coordinate) obj; 

        return this.x == coord.getX() && this.y == coord.getY();
    }

    /**
     * Java's default implementation of hashCode() will return a hashCode that is related to
     * the object's memory location rather than the actual content of the object.
     * If two objects having same data are stored at different locations in
     * memory, the default implementation will return false.
     * <p>
     * Therefore, when we define our own data types (classes), we need to override hashCode().
     * <p>
     * More info can be found at:
     * https://docs.oracle.com/javase/8/docs/api/java/lang/Object.html#hashCode--
     *
     * @return hashCode of obj based on its x, y coordinates.
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.x, this.y);
    }

    /**
     * We've provided this custom toString method to show you how to test with exceptions! You
     * can still choose to use this for debugging purposes if you would like.
     * <p>
     * Also note that this method has the same name as the default toString method but doesn't
     * conflict because it has a different method signature. This is called "method overloading",
     * a useful concept to know in Java programming!
     *
     * @param message String to be pre-pended onto the default Coordinate toString.
     * @throws IllegalArgumentException if the inputted message is null.
     * @return A String representation of the Coordinate object appended to the given message.
     */
    public String toString(String message) {
        if (message == null) {
            // You can add a custom exception message to make debugging easier!
            throw new IllegalArgumentException("Message String cannot be null!");
        }

        return String.format("%s %s", message, this.toString());
    }
}
