import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

public class Search {
    State init_state;
    State final_state;
    PriorityQueue<State> openSet;
    int nb_explored_states;
    public double t_h = 0;
    public double t_s = 0;

    public Search(State s0, State sN) {
        init_state = s0;
        final_state = sN;
    }

    public ArrayList<State> AStar() {
        double t0 = System.currentTimeMillis();
        nb_explored_states = 1;
        openSet = new PriorityQueue<State>(16, new StateComparator());
        openSet.add(init_state);
        init_state.g_score = 0;
        init_state.f_score = 1000; // h(init_state);

        while (openSet.size() > 0) {
            if ((System.currentTimeMillis() - t0) / 1000 > 300) {
                System.out.println("time out!");
                return null;
            }
            State current = openSet.poll();
            nb_explored_states ++;
            //if (current.equals(final_state)) {
            if (current.getNv() == 0) {
                return reconstruct_path(current);
            }

            System.out.println("     [ASTAR] get_next_states()");
            ArrayList<State> successors = current.get_next_states();

            for (State succ : successors) {
                double tentative_gScore = current.g_score + G(succ);
                if (tentative_gScore < succ.g_score) {
                    succ.cameFrom = current;
                    succ.g_score = tentative_gScore;
                    succ.f_score = tentative_gScore + H_react(succ);
                    succ.depth = current.depth + 1;
                    if (!is_in_openSet(succ)) {
                        openSet.add(succ);
                        //if (openSet.size() % 1000 == 0)
                        //    System.out.println("openSet.size = " + openSet.size());
                    }
                }
            }
        }
        return null;
    }

    private double G(State s) {
        //return 1;
        return s.getDuration();
    }
    private double H_overest(State s) {
        return time_to_goal2(s);
    }

    private double H_silly(State s) {
        return time_to_goal_silly(s);
    }

    private double H_react(State s) {
        double start = System.currentTimeMillis();
        double ret = State.mini_simulator.reactively_simulate(s);
        double end = System.currentTimeMillis();
        t_h += (end - start) / 1000;
        return ret;
    }

    private double time_to_goal2(State s) {
        // over-estimates time_to_goal
        double t = 0;
        for (Vehicle v : s.getVehicles()) {

            if (v.isDownward()) t += (State.y_max - v.getY_position()) / v.getSpeed();
            else t += v.getY_position() / v.getSpeed();

            // More optimal: increases search time!
            if (v.isParked())
                t += 15 / State.parking_speed;
        }
        return t;
    }

    private double time_to_goal_silly(State s) {
        // under-under-estimates time_to_goal
        double tmax = 0;

        for (Vehicle v : s.getVehicles()) {
            double t = 0;

            if (v.isDownward()) t = (State.y_max - v.getY_position()) / v.getSpeed();
            else t = v.getY_position() / v.getSpeed();

            // More optimal: increases search time!
            if (v.isParked())
                t += 15 / State.parking_speed;

            if (!v.isIn_ramp())
                t += 15 / v.getSpeed();

            if (t > tmax)
                tmax = t;
        }

        return tmax;
    }


    private boolean is_in_openSet(State x) {
        for (State s : openSet) {
            if (!x.equals(s) && x.toString().equals(s.toString())) {
                System.out.println("hihihi");
                System.out.println("x\n---------------------\n" + x);
                System.out.println("s\n---------------------\n" + s);
                x.equals(s);
            }
            if (x.equals(s))
                return true;
        }
        return false;
    }

    private ArrayList<State> reconstruct_path(State s) {
        ArrayList<State> ret = new ArrayList<State>();
        float total_duration = 0;
        State x = s;
        while (x.cameFrom != null) {
            ret.add(x);
            total_duration += x.getDuration();
            x = x.cameFrom;
        }
        //TODO: add s0 ?
        ret.add(x);
        total_duration += x.getDuration();
        //System.out.println("total_duration = " + total_duration);
        s.total_duration = total_duration;
        return ret;
    }

}


class StateComparator implements Comparator<State> {
    public int compare(State s1, State s2) {
        if (s1.f_score < s2.f_score)
            return -1;
        else if (s1.f_score > s2.f_score)
            return 1;
        return 0;
    }
}