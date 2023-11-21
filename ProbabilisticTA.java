/**
 * ProbabilisticTA.java provides the component of probabilistic task assignment for
 * TDSSA, which finds a task to assign when a worker requests.
 */

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class ProbabilisticTA {
    private double tau; // Sybil threshold for banning workers
    private double delta; // reliability threshold for marking reliable workers
    private double alpha; // probability to assign a golden task to a new worker
    private int K; // number of workers per task


    /* initialization */
    public ProbabilisticTA(double tau, double delta, double alpha, int K) {
        this.tau = tau;
        this.delta = delta;
        this.alpha = alpha;
        this.K = K;
    }


    /* probabilically assign a task to a requesting worker based on the worker's Sybil score and reliability score */
    public Task assign(Worker worker, Set<Task> golden_tasks, Set<Task> normal_tasks) {
        //PTA step 1
        if (worker.getS() < tau && worker.getR() < delta) {
            //double g = (1 - alpha) * worker.getS();
            double g = alpha * (1 - worker.getR()) + (1 - alpha) * worker.getS();
            Random rand = new Random();
            // assign a golden task with g probability
            //PTA step 2
            if (rand.nextDouble() <= g) {
                Set<Task> avail_tasks = new HashSet<Task>(golden_tasks);
                avail_tasks.removeAll(worker.getLabeledPairs().keySet());
                for (Task task : avail_tasks) {
                    //PTA step 3
                    if (worker.getAttackerID() == -1 && !worker.getPairs().keySet().contains(task)) {
                        continue;
                    } else {
                        int count = task.getExpose();
                        for (Worker curr_worker : task.getAssigned()) {
                            if (curr_worker.getR() < delta) {
                                count++;
                            }
                        }
                        //PTA step 4
                        if (count >= K) {
                            continue;
                        } else {
                            task.assign(worker);
                            return task;
                        }
                    }
                }
            }
        }

        // assign a normal task
        //PTA step 5 6
        for (Task task : normal_tasks) {
            ArrayList<Worker> assigned_workers = task.getAssigned();
            if (!assigned_workers.contains(worker)) {
                if (worker.getAttackerID() != -1) {
                    task.assign(worker);
                    return task;
                } else {
                    if (task.getWorkers().contains(worker)) {
                        task.assign(worker);
                        return task;
                    }
                }
            }
        }
        return null;
    }
}
