import java.util.Comparator;

public class Vehicle extends SceneElement {
    private boolean downward;
    private boolean parked = false;
    private double speed;
    private Action current_action;


    public Vehicle(String nam, boolean dwd) {
        this.downward = dwd;
        this.name = nam;
        this.parked = false;
        this.x_position = 0;
        if (this.downward) this.speed = 5.7;
        else               this.speed = 3.1;
        // 3.1 / 5.7
    }

    public Vehicle getCopy() {
        Vehicle ret = new Vehicle(this.name, this.downward);
        ret.parked = this.parked;
        ret.x_position = this.x_position;
        return ret;
    }

    public void setParked(boolean parked) {
        this.parked = parked;
    }

    public String getName() {
        return name;
    }

    public boolean isParked() {
        return parked;
    }

    public boolean isDownward() {
        return downward;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public String getTypeString() {
        if (this.downward)
            return " [↓]";
        else
            return " [↑]";
    }

}

class VehicleXPositionComparatorReverse implements Comparator<Vehicle> {
    @Override
    public int compare(Vehicle v1, Vehicle v2) {
        if (v1.getX_position() > v2.getX_position())
            return -1;
        else if (v1.getX_position() < v2.getX_position())
            return 1;
        return 0;
    }
}


