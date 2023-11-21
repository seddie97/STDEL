/**
 * ExtendedTD.java provides the component of extended truth discovery for TDSSA,
 * which iteratively infers the true label of tasks and the quality of workers
 * with the Sybil score and reliability score of workers taken into consideration.
 */

import org.apache.commons.math3.distribution.ChiSquaredDistribution;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class ExtendedTD {
    private int L; // label size

    /* initialization */
    public ExtendedTD(int L) {
        this.L = L;
    }

    /* iteratively run extended truth discovery*/
    public void process(Set<Task> tasks, Set<Worker> workers) {
        // set the initial weight of workers to their accuracy on golden tasks
        //ETD step 1
        for (Worker worker : workers) {
            worker.setWeight(worker.getP());
        }

        int iteration = 0;
        //ETD step 3
        while (iteration < 1000) {
            iteration++;
            int difference = 0;
            double[] votes = new double[L];
            // extended label aggregation
            //ETD step 4
            for (Task task : tasks) {
                int original_label = task.getAggregated();
                ArrayList<Worker> assigned = task.getAssigned();

                for (int i = 0; i < L; i++) {
                    votes[i] = 0;
                }
                for (Worker worker : assigned) {
                    int label = worker.getLabeledPairs().get(task);
                    //等式6
                    votes[label] += worker.getS() / L + (1 - worker.getS()) * worker.getWeight();
                }
                double max_vote = -1;
                for (int i = 0; i < L; i++) {
                    if (votes[i] > max_vote) {
                        task.setAggregated(i);
                        max_vote = votes[i];
                    }
                }
                if (original_label != task.getAggregated()) {
                    difference++;
                }
            }

            // terminate if converge
            if (difference == 0) {
                break;
            }

            // extended weight estimation
            //ETD step 6
            for (Worker worker : workers) {
                double correct = 0;
                double count = 0;

                //new
                double sigema = 0.0;
                double alpha = 0.05;

                Map<Task, Integer> assigned = worker.getLabeledPairs();
                for (Task task : assigned.keySet()) {
                    /*if (assigned.get(task).intValue() == task.getAggregated()) {
                        correct += task.getCi();
                    }
                    count += task.getCi();*/
                    //if vectors have the same value, subtract equal 0, else square of vectors modulus equal 2
                    sigema += assigned.get(task) == task.getAggregated() ? 0 : 2;
                }
                //if (count > 0) {
                //count += new ChiSquaredDistribution(worker.getLabeledPairs().size()).inverseCumulativeProbability(1 - alpha / 2) / sigema;
                //worker.setWeight(correct / count);
                //according to catd
                worker.setWeight(new ChiSquaredDistribution(worker.getLabeledPairs().size()).inverseCumulativeProbability(1 - alpha / 2) / sigema);
                //System.out.println(  " ChiSquared:" + new ChiSquaredDistribution(worker.getLabeledPairs().size()).inverseCumulativeProbability(1 - alpha / 2) / sigema );
                //}
            }
        }
    }
}
