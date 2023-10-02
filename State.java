import java.util.ArrayList;
import java.util.Collections;

public class State {
    public final static double parking_time = 3; // s
    public final static double x_max = 100.0; // m
    public final static double safety_distance = 5.0; // m (center-to-center)
    private double start_time = 0;
    public double g_score = 1000;
    public double f_score = 1000;
    public State cameFrom = null;
    private ArrayList<Vehicle> dw_vehicles;
    private ArrayList<Vehicle> up_vehicles;
    private int Ndw = 0;
    private int Nup = 0;
    private int Npp = 0;
    private double duration;
    private ArrayList<Vehicle> initial_dw_vehicles;
    private ArrayList<ParkingPlace> parking_places;
    private static ArrayList<State> next_states;
    public static MiniSimulator mini_simulator;
    // ----------------------------------------------------------------------
    public int[] parked_at = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
    public int[] preparked_at = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1};


    public State() {
        this.dw_vehicles = new ArrayList<Vehicle>();
        this.up_vehicles = new ArrayList<Vehicle>();
        this.parking_places = new ArrayList<ParkingPlace>();
        this.initial_dw_vehicles = new ArrayList<Vehicle>();
        this.duration = 0;
    }

    public static void setMini_simulator(MiniSimulator mini_simulator) {
        State.mini_simulator = mini_simulator;
    }

    public void addVehicle(Vehicle v) {
        if (v.isDownward()) {
            dw_vehicles.add(v);
            v.id = Ndw;     // id from their respective list!!
            Ndw ++;
        }
        else {
            up_vehicles.add(v);
            v.id = Ndw;     // id from their respective list!!
            Nup ++;
        }
        v.setParentState(this);
    }
    public void addParkingPlace(ParkingPlace pp) {
        parking_places.add(pp);
        //park_clear.add(true);
        //prepark_clear.add(true);
        pp.id = Npp;
        Npp ++;
        pp.setParentState(this);
    }

    public void setParked_vehicle(Vehicle v, ParkingPlace pp) {
        parked_at[v.id] = pp.id;
        v.setParking_progress(1);
        v.x_position = pp.x_position;
    }

    public Vehicle getParked_vehicle(ParkingPlace pp) {
        for (int i=0; i<Ndw; i++) {
            if (parked_at[i] == pp.id)
                return dw_vehicles.get(i);
        }
        return null;
    }

    public boolean hasParkedVehicle(ParkingPlace pp) {
        Vehicle pv = getParked_vehicle(pp);
        if (pv != null)
            return true;
        else
            return false;
    }

    public void removeParked_vehicle(Vehicle v) {
        parked_at[v.id] = -1;
    }

    public boolean allVehiclesOut() {
        for (Vehicle v : dw_vehicles)
            if (!v.isOut())
                return false;
        for (Vehicle v : up_vehicles)
            if (!v.isOut())
                return false;
        return true;
    }

    public void removeVehicle(String vname) {
        for (Vehicle x : this.dw_vehicles) {
            if (x.getName().equals(vname)) {
                this.dw_vehicles.remove(x);
                this.Ndw --;
                return;
            }
        }
        for (Vehicle x : this.up_vehicles) {
            if (x.getName().equals(vname)) {
                this.up_vehicles.remove(x);
                this.Nup --;
                return;
            }
        }
    }

    public State getCopy() {
        State ret = new State();
        // copy vehicle lists
        for (Vehicle v : this.dw_vehicles) {
            if (!v.isOut())
                ret.dw_vehicles.add(v.getCopy());
        }
        for (Vehicle v : this.up_vehicles) {
            if (!v.isOut())
                ret.up_vehicles.add(v.getCopy());
        }
        // copy parking places list
        for (ParkingPlace p : this.parking_places) {
            ret.parking_places.add(p.getCopy());
        }
        for (Vehicle v : this.initial_dw_vehicles) {
            ret.initial_dw_vehicles.add(v.getCopy());
        }
        ret.parked_at = parked_at.clone();
        ret.preparked_at = preparked_at.clone();

        ret.Ndw = ret.dw_vehicles.size();
        ret.Nup = ret.up_vehicles.size();
        ret.Npp = this.Npp;
        ret.start_time = this.start_time;
        return ret;
    }

    public boolean equals(State s) {
        if (this.Ndw != s.Ndw)
            return false;
        if (this.Nup != s.Nup)
            return false;
        for (Vehicle v1 : dw_vehicles)
            for (Vehicle v2 : s.getDw_vehicles())
                if (v1.name == v2.name  &&  v1.x_position != v2.x_position)
                    return false;
        return true;
    }

    public String getOrderedNames() {
        ArrayList<SceneElement> elts = new ArrayList<SceneElement>();
        elts.addAll(this.dw_vehicles);
        elts.addAll(this.up_vehicles);
        elts.addAll(this.parking_places);
        Collections.sort(elts, new SceneElementXPositionComparator());
        String str = "";
        for (SceneElement se : elts) {
            str += se.getName();
        }
        return str;
    }

    public String toString() {
        ArrayList<SceneElement> elts = new ArrayList<SceneElement>();
        elts.addAll(this.dw_vehicles);
        elts.addAll(this.up_vehicles);
        elts.addAll(this.parking_places);
        Collections.sort(elts, new SceneElementXPositionComparator());
        String ret = "";
        for (SceneElement se : elts) {
            ret += se.getName();
            ret += " ";
            ret += se.getX_position();
            ret += se.getTypeString();
            ret += "\n";
        }
        ret += "-------------\n";
        return ret;
    }

    public String current_action_str() {
        ArrayList<Vehicle> elts = new ArrayList<>();
        elts.addAll(this.dw_vehicles);
        Collections.sort(elts, new SceneElementXPositionComparator());
        String ret = "";
        for (Vehicle se : elts) {
            int id = se.getCurrent_action().getId();
            String id_str = se.getCurrent_action().getName();
            ret += id_str;
            if (id == Action.PARK) ret += "(" + se.getCurrent_action().getParameter().name + ") ";
            else                   ret += "     ";
            ret += " ";
        }
        ret += "\n";
        return ret;
    }

    public String initial_vehicle_action_str() {
        String ret = "";
        for (Vehicle v : this.initial_dw_vehicles) {
            int id = v.getCurrent_action().getId();
            String id_str = v.getCurrent_action().getName();
            ret += v.getName() + ": " + id_str;
            if (id == Action.PARK) ret += "(" + v.getCurrent_action().getParameter().name + ") ";
            else                   ret += "     ";
            ret += " ";
        }
        return ret;
    }

    public ArrayList<State> get_next_states() {
        ArrayList<State> ret = new ArrayList<State>();
        next_states = new ArrayList<State>();

        // enumerated possible actions to be applied to this state
        enumerate_actions(0, dw_vehicles.size());
        System.out.println("CURRENT ACTIONS: " + current_action_str());

        // get states resulting from events occurring during simulation
        for (State s : next_states) {
            ArrayList<State> event_based_states = mini_simulator.simulate(s.getCopy(), false, false); // because simulate(x) modifies x
            if (event_based_states != null) {
                for (State ebs : event_based_states) {
                    ebs.initial_dw_vehicles.clear();
                    for (Vehicle v : s.getDw_vehicles())  // to remember what actions have been applied on s to produce ebs
                        ebs.initial_dw_vehicles.add(v.getCopy());
                    ret.add(ebs);
                }
            }
        }

        System.out.println("Simulation has generated " + ret.size() + " states.");
        return ret;
    }
//---------------------------------------------------------------------------------------------------------------------
public ArrayList<State> get_next_states2() {
    ArrayList<State> ret = new ArrayList<State>();
    next_states = new ArrayList<State>();

    // enumerated possible actions to be applied to this state
    enumerate_actions2(0, dw_vehicles.size());
    //System.out.println("CURRENT ACTIONS: " + current_action_str());

    //TODO here: remove all states with logical conflicts:  (then we should not need isBooked any longer)
    //  * PARK / PREPARK / UNPARK with same destination and different vehicles
    //  * ...

    // get states resulting from events occurring during simulation            //TODO: Could be not needed anymore !
    for (State s : next_states) {
        ArrayList<State> event_based_states = mini_simulator.simulate(s.getCopy(), false, false); // because simulate(x) modifies x
        if (event_based_states != null) {
            for (State ebs : event_based_states) {
                ebs.initial_dw_vehicles.clear();
                for (Vehicle v : s.getDw_vehicles())  // to remember what actions have been applied on s to produce ebs
                    ebs.initial_dw_vehicles.add(v.getCopy());
                ret.add(ebs);
            }
        }
    }

    //TODO: Apply symbolic effects of actions on states

    System.out.println("Simulation has generated " + ret.size() + " states.");
    return ret;
}


    public void assignActions(ArrayList<Vehicle> vehicles) {
        for (Vehicle v1 : dw_vehicles) {
            for (Vehicle v2 : vehicles) {
                if (v1.name.equals(v2.name))
                    v1.setCurrent_action(v2.getCurrent_action());
            }
        }
    }

    public void printCurrentActions() {
        System.out.println("CURRENT ACTIONS:");
        for(Vehicle v : this.dw_vehicles) {
            System.out.printf("%s : %s\n", v.name, v.getCurrent_action().getName());
        }
    }

    public void enumerate_actions(int id_vehicle, int Nv) {
        if (id_vehicle == Nv) {
            next_states.add(this);
        }
        else {
            Vehicle v = dw_vehicles.get(id_vehicle);

            // enumerate EXIT actions
            boolean exit_feasible = !this.is_upward_vehicle_below(v);       // GEOMETRIC
            if (exit_feasible) {
                State state_copy_1 = this.getCopy();
                state_copy_1.dw_vehicles.get(id_vehicle).setCurrent_action(new Action(Action.EXIT));
                state_copy_1.enumerate_actions(id_vehicle + 1, Nv);
            }
            else
                System.out.println("[ENUM FILTER!!!] " + v.getName() + " cannot EXIT because of vehicle Up vehicle below.");

            // enumerate PARK actions
            State state_copy_2 = this.getCopy();
            for (int p=0; p<this.Npp; p++)
                state_copy_2.enumerate_parking_places(id_vehicle, p, Nv);

            // enumerate WAIT actions
            State state_copy_3 = this.getCopy();
            state_copy_3.dw_vehicles.get(id_vehicle).setCurrent_action(new Action(Action.WAIT));
            state_copy_3.enumerate_actions(id_vehicle + 1, Nv);
        }
    }

    public void enumerate_parking_places(int id_vehicle, int id_parking, int Nv) {
        if (id_vehicle == Nv) {
            this.next_states.add(this);
        }
        else {
            // Assigns parking places, prunes out parking places which are:
            //  - above
            //  - booked
            //  - with an upward vehicle in-between
            State state_copy_22 = this.getCopy();
            ParkingPlace pp = state_copy_22.parking_places.get(id_parking);
            Vehicle v = state_copy_22.dw_vehicles.get(id_vehicle);
            if (!pp.isBooked()) {                                                   // LOGIC
                if (pp.isBelow(v)) {                                                // GEOMETRIC
                    if (!state_copy_22.exist_upward_vehicle_between(v, pp)) {       // GEOMETRIC
                        v.setCurrent_action(new Action(Action.PARK, pp));
                        pp.setBooked(true);
                        state_copy_22.enumerate_actions(id_vehicle + 1, Nv);
                    }
                    else
                        System.out.println("[ENUM FILTER!!!] " + v.getName() + " cannot PARK because of Up vehicle in-between.");
                }
            }
        }
    }
//-----------------------------------------------------------------------------------------------------------------------
    public void enumerate_actions2(int id_vehicle, int Nv) {
        if (id_vehicle == Nv) {
            next_states.add(this);
        }
        else {
            Vehicle v = dw_vehicles.get(id_vehicle);

            boolean testpreconditions = fulfills_preconditions(v, Action.EXIT);
            if (testpreconditions)
                System.out.println("YES!");
            else
                System.out.println("NO!");

            // enumerate EXIT actions
            boolean exit_feasible = !this.is_upward_vehicle_below(v);       // GEOMETRIC
            if (exit_feasible) {
                State state_copy_1 = this.getCopy();
                state_copy_1.dw_vehicles.get(id_vehicle).setCurrent_action(new Action(Action.EXIT));
                state_copy_1.enumerate_actions(id_vehicle + 1, Nv);
            }
            else
                System.out.println("[ENUM FILTER!!!] " + v.getName() + " cannot EXIT because of vehicle Up vehicle below.");

            // enumerate PARK actions
            State state_copy_2 = this.getCopy();
            for (int p=0; p<this.Npp; p++)
                state_copy_2.enumerate_parking_places2(id_vehicle, p, Nv);

            // enumerate WAIT actions
            State state_copy_3 = this.getCopy();
            state_copy_3.dw_vehicles.get(id_vehicle).setCurrent_action(new Action(Action.WAIT));
            state_copy_3.enumerate_actions(id_vehicle + 1, Nv);
        }
    }

    public void enumerate_parking_places2(int id_vehicle, int id_parking, int Nv) {
        if (id_vehicle == Nv) {
            this.next_states.add(this);
        }
        else {
            // Assigns parking places, prunes out parking places which are:
            //  - above
            //  - booked
            //  - with an upward vehicle in-between
            State state_copy_22 = this.getCopy();
            ParkingPlace pp = state_copy_22.parking_places.get(id_parking);
            Vehicle v = state_copy_22.dw_vehicles.get(id_vehicle);
            if (!pp.isBooked()) {                                                   // LOGIC
                if (pp.isBelow(v)) {                                                // GEOMETRIC
                    if (!state_copy_22.exist_upward_vehicle_between(v, pp)) {       // GEOMETRIC
                        v.setCurrent_action(new Action(Action.PARK, pp));
                        pp.setBooked(true);
                        state_copy_22.enumerate_actions(id_vehicle + 1, Nv);
                    }
                    else
                        System.out.println("[ENUM FILTER!!!] " + v.getName() + " cannot PARK because of Up vehicle in-between.");
                }
            }
        }
    }

    public boolean fulfills_preconditions(Vehicle v, int action, int... param) {
        if (action == Action.EXIT) {
            if (!v.isParked() && v.isIn_ramp())
                return true;
            else
                return false;
        }
        else if (action == Action.PREPARK) {
            if (!v.isParked() && !v.isPreparked() && v.isIn_ramp())
                return true;
            else
                return false;
        }
        else if (action == Action.PARK) {
            int pp_id = param[0];
            if (!v.isParked() && is_park_clear(pp_id) && v.isPreparked() && v.isIn_ramp())
                return true;
            else
                return false;
        }
        else if (action == Action.UNPARK) {
            int pp_id = param[0];
            if (v.isParked() && is_prepark_clear(pp_id))
                return true;
            else
                return false;
        }
        else if (action == Action.WAIT) {
            if (v.isParked() || v.isFirst())
                return true;
            else
                return false;
        }
        else if (action == Action.ENTER) {
            if (!v.isIn_ramp() && v.isFirst())
                return true;
            else
                return false;
        }
        else {
            System.out.println("This action has a weird id!");
            System.exit(0);
            return false;
        }
    }

    public boolean is_park_clear(int pp_id) {
        for (int i=0; i<Ndw; i++) {
            if (parked_at[i] == pp_id)
                return false;
        }
        return true;
    }

    public boolean is_prepark_clear(int pp_id) {
        for (int i=0; i<Ndw; i++) {
            if (preparked_at[i] == pp_id)
                return false;
        }
        return true;
    }

    public double getStart_time() {
        return start_time;
    }

    public ArrayList<Vehicle> getDw_vehicles() {
        return dw_vehicles;
    }
    public ArrayList<Vehicle> getUp_vehicles() {
        return up_vehicles;
    }

    public ArrayList<ParkingPlace> getParking_places() {
        return parking_places;
    }

    public ArrayList<Vehicle> getInitial_dw_vehicles() {
        return initial_dw_vehicles;
    }

    public void increaseStart_time(double start_time) {
        this.start_time += start_time;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public double getDuration() {
        return duration;
    }

    Vehicle get_closest_upward_vehicle_below(Vehicle v) {
        Vehicle closest = null;
        double min_dist = 1000;
        for (Vehicle upv : this.up_vehicles)
            if (upv.x_position > v.x_position) {
                double d = upv.x_position - v.x_position;
                if (d < min_dist) {
                    min_dist = d;
                    closest = upv;
                }
            }
        return closest;
    }

    boolean is_upward_vehicle_below(Vehicle v) {
        for (Vehicle upv : this.up_vehicles)
            if (upv.x_position > v.x_position)
                return true;
        return false;
    }

    ArrayList<ParkingPlace> get_parking_places_below(Vehicle v) {
        ArrayList<ParkingPlace> ret = new ArrayList<>();
        for (ParkingPlace pp : this.parking_places)
            if (pp.x_position > v.x_position)
                ret.add(pp);

        return ret;
    }

    ArrayList<Vehicle> get_dw_vehicles_below(Vehicle v) {
        ArrayList<Vehicle> ret = new ArrayList<>();
        for (Vehicle dwv : this.dw_vehicles)
            if (dwv.x_position > v.x_position)
                ret.add(dwv);

        return ret;
    }

    boolean exist_upward_vehicle_between(Vehicle v, ParkingPlace pp) {
        for (Vehicle upv : this.up_vehicles)
            if (upv.x_position > (v.x_position + 0.1)  &&  upv.x_position < (pp.x_position - 0.1))
                return true;

        return false;
    }

    boolean exist_enough_parking_place_between(Vehicle dwv, Vehicle upv) {
        int Ndw = 1;
        int Npp = 0;

        for (Vehicle v : this.dw_vehicles)
            if (v.x_position > (dwv.x_position + 0.1) && v.x_position < (upv.x_position - 0.1))
                Ndw ++;

        for (ParkingPlace pp : this.parking_places)
            if (pp.x_position > (dwv.x_position + 0.1) && pp.x_position < (upv.x_position - 0.1))
                Npp ++;

        if (Ndw > Npp)
            return false;
        else
            return true;
    }

}
