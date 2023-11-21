/**
 * Worker.java provides the modeling of online workers. Each worker is associated with
 * a Sybil score, a reliability score and an accuracy on golden tasks. The attacker id
 * indicates which Sybil attacker controls the worker. Independent workers will have a
 * -1 attacker id.
 */

import java.util.HashMap;
import java.util.Map;

public class Worker {
    private Map<Task, Integer> pairs; // the (task, label) pairs in the original dataset
    private Map<Task, Integer> labeled_pairs; // the (task, label) pairs for tasks currently assigned to the worker
    private double s_j; // Sybil score
    private double r_j; // reliability score
    private double p_j; // accuracy on golden tasks
    private int attacker_id; // indicate which attacker the worker belongs to (-1 means independent worker)
    private int worker_id; // worker ID
    private double weight; // weight of the worker's labels in extended truth discovery
    private boolean banned; // indicate whether the worker is banned


    //new
    private int coutLowReward;
    private int coutLowTask;
    private int coutHightReward;
    private int coutHightTask;

    /* initialization */
    public Worker(int worker_id) {
        pairs = new HashMap<Task, Integer>();
        labeled_pairs = new HashMap<Task, Integer>();
        s_j = 0;
        r_j = 0;
        p_j = 0;
        this.attacker_id = -1;
        this.worker_id = worker_id;
        weight = 0;
        banned = false;

        //new
        coutLowReward = 0;
        coutHightReward = 0;
        coutLowTask = 0;
        coutHightTask = 0;
    }

    /* return worker ID */
    public int getWorkerId() {
        return worker_id;
    }

    /* add a (task, label) pair in the original dataset */
    public void addPair(Task task, int label) {
        pairs.put(task, label);
    }

    /* return the (task, label) pairs in the original dataset */
    public Map<Task, Integer> getPairs() {
        return pairs;
    }

    /* update the current (task, label) pair */
    public void label(Task task, int label) {
        labeled_pairs.put(task, label);
    }

    /* return the current (task, label) pairs */
    public Map<Task, Integer> getLabeledPairs() {
        return labeled_pairs;
    }

    /* remove the label of a task labeled by the banned worker */
    public void remove(Task task) {
        labeled_pairs.remove(task);
    }

    /* set the Sybil score */
    public void setS(double s) {
        s_j = s;
    }

    /* return the Sybil score */
    public double getS() {
        return s_j;
    }

    /* set the reliability score */
    public void setR(double r) {
        r_j = r;
    }

    /* return the reliability score */
    public double getR() {
        return r_j;
    }

    /* set the accuracy on golden tasks */
    public void setP(double p) {
        p_j = p;
    }

    /* return the accuracy on golden tasks */
    public double getP() {
        return p_j;
    }

    /* set the attacker ID (id=-1 means the worker is an independent worker) */
    public void setAttackerID(int id) {
        attacker_id = id;
    }

    /* return the attacker ID */
    public int getAttackerID() {
        return attacker_id;
    }

    /* update the worker's weight in extended truth discovery */
    public void setWeight(double w) {
        weight = w;
    }

    /* return the worker's weight in extended truth discovery */
    public double getWeight() {
        return weight;
    }

    /* ban the worker */
    public void ban() {
        banned = true;
    }

    /* check whether the worker is banned */
    public boolean isBanned() {
        return banned;
    }

    /* reset the features of the worker for a new run */
    public void reset() {
        labeled_pairs = new HashMap<Task, Integer>();
        s_j = 0;
        r_j = 0;
        attacker_id = -1;
        weight = 0.8;
        banned = false;
    }

    //new
    /* update the worker's CLR */
    public void setCLR() {
        coutLowReward++;
    }

    /* return the worker's CLR */
    public int getCLR() {
        return coutLowReward;
    }

    /* update the worker's CHR */
    public void setCHR() {
        coutHightReward++;
    }

    /* return the worker's CHR */
    public int getCHR() {
        return coutHightReward;
    }

    /* update the worker's CHR */
    public void setCHT() {
        coutHightTask++;
    }

    /* return the worker's CHR */
    public int getCHT() {
        return coutHightTask;
    }

    /* update the worker's CHR */
    public void setCLT() {
        coutLowTask++;
    }

    /* return the worker's CHR */
    public int getCLT() {
        return coutLowTask;
    }
}

