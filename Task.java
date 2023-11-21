/**
 * Task.java provides the modeling of tasks. Each task is associated with an average
 * reliability of workers on the task and a true label. A task is completed once the
 * aggregated label is determined.
 */

import java.util.ArrayList;

public class Task {
    private ArrayList<Worker> workers; // assigned workers in the original data
    private ArrayList<Worker> assigned; // current assigned workers
    private int true_label; // true label of the task
    private int aggregated; // aggregated label of the task
    private int[] majority; // majority indicator (1 means that the corresponding label is a majority vote)
    private int L; // label size
    private int task_id; // task ID
    private double c_i; // average reliability score of assigned workers
    private double[] conf; // confidence on each optional label
    private int exposed; // number of times assigned to banned workers

    //new
    private int task_reward; //reward of task

    /* initialization */
    public Task(int task_id, int true_label, int L) {
        workers = new ArrayList<Worker>();
        assigned = new ArrayList<Worker>();
        this.true_label = true_label;
        aggregated = -1;
        majority = new int[L];
        this.L = L;
        this.task_id = task_id;
        c_i = 0.0;
        conf = new double[L];
        exposed = 0;
    }

    /* set the confidence on each optional label */
    public void setConf(double[] confidence) {
        conf = confidence;
    }

    /* return the confidence on each optional label */
    public double[] getConf() {
        return conf;
    }

    /* return task ID */
    public int getTaskId() {
        return task_id;
    }

    /* update the original assigned workers for the task */
    public void addWorker(Worker worker) {
        workers.add(worker);
    }

    /* return the original assigned workers for the task */
    public ArrayList<Worker> getWorkers() {
        return workers;
    }

    /* assign a worker to the task */
    public void assign(Worker worker) {
        assigned.add(worker);
    }

    /* return assigned workers */
    public ArrayList<Worker> getAssigned() {
        return assigned;
    }

    /* remove a banned worker from assigned workers for a normal task */
    public void remove(Worker worker) {
        assigned.remove(worker);
    }

    /* return the true label */
    public int getTrueLabel() {
        return true_label;
    }

    /* set the aggregated label */
    public void setAggregated(int aggregated) {
        this.aggregated = aggregated;
    }

    /* return the aggregated label */
    public int getAggregated() {
        return aggregated;
    }

    /* compute the indicator of majority labels that receive the most votes */
    public void calMajority() {
        int max_vote = 0;
        for (int i = 0; i < L; i++) {
            majority[i] = 0;
        }
        for (Worker worker : assigned) {
            int answer = worker.getLabeledPairs().get(this);
            majority[answer]++;
            if (majority[answer] > max_vote) {
                max_vote = majority[answer];
            }
        }
        for (int i = 0; i < L; i++) {
            if (majority[i] == max_vote && max_vote >= 2) {
                majority[i] = 1;
            } else {
                majority[i] = 0;
            }
        }
    }

    /* return the indicator of majority labels that receive the most votes */
    public int[] getMajority() {
        return majority;
    }

    /* update the number of times being assigned to banned workers */
    public void expose() {
        exposed++;
    }

    /* return the number of times being assigned to banned workers */
    public int getExpose() {
        return exposed;
    }

    /* compute the average reliability of assigned workers */
    public void calCi() {
        c_i = 0.0;
        for (Worker worker : assigned) {
            double r_j = worker.getR();
            c_i += r_j;
        }
        c_i = c_i / assigned.size();
    }

    /* return the average reliability of assigned workers */
    public double getCi() {
        return c_i;
    }

    /* reset the features of the task for a new run */
    public void reset() {
        assigned = new ArrayList<Worker>();
        aggregated = -1;
        c_i = 0.0;
        conf = new double[L];
        exposed = 0;
    }


    //new
    /* return the task reward */
    public void setTask_reward(int task_reward) {
        this.task_reward = task_reward;
    }

    /* return the task reward */
    public int getTask_reward() {
        return task_reward;
    }
}
