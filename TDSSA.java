/**
 * TDSSA.java is the main framework that coordinates the probabilistic task assignment and
 * extended truth discovery to deal with different worker activities for defending against
 * strategic Sybil attack. The statistics about Accuracy, Cost, Completion and running time
 * would be saved in a "result.txt" file.
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.*;

public class TDSSA {
    /* TDSSA parameters */
    private int B; // condition for terminating a batch
    private double alpha; // probability to assign a golden task to a new worker
    private double tau; // Sybil threshold for banning workers
    private double delta; // reliability threshold for marking reliable workers

    /* dataset parameters */
    private ArrayList<Worker> order; // requesting order of workers;
    private int L; // label size
    private int K; // number of workers per task

    /* attack parameters */
    private double epsilon;
    private int lambda;

    /* ID mapping */
    private Map<Integer, Task> id_to_task; // ID to normal task mapping
    private Map<Integer, Worker> id_to_worker; // ID to worker mapping
    private Map<Integer, Task> id_to_golden; // ID to golden task mapping
    private Map<Integer, Attacker> id_to_attacker; // ID to attacker mapping

    /* evaluation parameters */
    private double a_accuracy; // aggregation accuracy
    private double e_number; // average number of exposed golden tasks
    private double t_cost; // average number of golden task assignment for testing each worker
    private long running_time; // running time of TDSSA in millisecond

    //new
    private int attackGotReward = 0; //attack got reward


    public TDSSA(int B, double alpha, double tau, double delta) {
        this.B = B;
        this.alpha = alpha;
        this.tau = tau;
        this.delta = delta;
        order = new ArrayList<Worker>();
        id_to_task = new HashMap<Integer, Task>();
        id_to_worker = new HashMap<Integer, Worker>();
        id_to_golden = new HashMap<Integer, Task>();
        id_to_attacker = new HashMap<Integer, Attacker>();
        a_accuracy = 0.0;
        e_number = 0.0;
        t_cost = 0.0;
        running_time = 0;

        attackGotReward = 0;
    }

    /* read worker labels on normal tasks */
    public void readNormal(String dataset) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(dataset + "//input.txt"));
            String line = reader.readLine();
            String[] elements = line.split("\t");
            L = Integer.parseInt(elements[2]);
            K = Integer.parseInt(elements[3]);
            line = reader.readLine();

            //new
            int c = 0; //record nums of task

            while (line != null) {
                elements = line.split("\t");
                int task_id = Integer.parseInt(elements[0]);
                int true_label = Integer.parseInt(elements[1]);
                int worker_num = Integer.parseInt(elements[2]);
                Task task = new Task(task_id, true_label, L);

                //new
                //NLP
                if (dataset.equals("NLP")) {
                    if (c <= 23) {
                        task.setTask_reward(5);
                    } else if (c <= 159) {
                        task.setTask_reward(13);
                    } else if (c <= 500) {
                        task.setTask_reward(24);
                    } else if (c <= 841) {
                        task.setTask_reward(38);
                    } else if (c <= 977) {
                        task.setTask_reward(55);
                    } else {
                        task.setTask_reward(75);
                    }
                } else if (dataset.equals("DOG")) {
                    //DOG
                    if (c <= 19) {
                        task.setTask_reward(5);
                    } else if (c <= 129) {
                        task.setTask_reward(13);
                    } else if (c <= 404) {
                        task.setTask_reward(24);
                    } else if (c <= 679) {
                        task.setTask_reward(38);
                    } else if (c <= 789) {
                        task.setTask_reward(55);
                    } else {
                        task.setTask_reward(75);
                    }
                }

                c++;

                id_to_task.put(task_id, task);
                for (int i = 0; i < worker_num; i++) {
                    int worker_id = Integer.parseInt(elements[2 * i + 3]);
                    int answer = Integer.parseInt(elements[2 * i + 4]);
                    if (!id_to_worker.containsKey(worker_id)) {
                        Worker worker = new Worker(worker_id);
                        id_to_worker.put(worker_id, worker);
                        worker.addPair(task, answer);
                        task.addWorker(worker);
                    } else {
                        Worker worker = id_to_worker.get(worker_id);
                        worker.addPair(task, answer);
                        task.addWorker(worker);
                    }
                }
                line = reader.readLine();
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* read worker labels on golden tasks */
    public void readGolden(String dataset) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(dataset + "//golden.txt"));
            String line = reader.readLine();
            int golden_num = Integer.parseInt(line);
            line = reader.readLine();
            String[] elements = line.split("\t");
            for (int i = 0; i < golden_num; i++) {
                int golden_id = Integer.parseInt(elements[i * 2]);
                Task task = new Task(golden_id, Integer.parseInt(elements[i * 2 + 1]), L);
                id_to_golden.put(golden_id, task);
            }
            line = reader.readLine();
            while (line != null) {
                elements = line.split("\t");
                int worker_id = Integer.parseInt(elements[0]);
                Worker worker = id_to_worker.get(worker_id);
                for (int i = 0; i < golden_num; i++) {
                    int golden_id = Integer.parseInt(elements[i * 2 + 1]);
                    Task task = id_to_golden.get(golden_id);
                    int answer = Integer.parseInt(elements[i * 2 + 2]);
                    worker.addPair(task, answer);
                }
                line = reader.readLine();
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* read Sybil workers of each attacker for the rth run */
    public void readAttack(String dataset, int r) {
        try {
            id_to_attacker = new HashMap<Integer, Attacker>();
            BufferedReader reader = new BufferedReader(new FileReader(dataset + "//" + r + "//attack.txt"));
            String line = reader.readLine();
            String[] elements = line.split("\t");
            epsilon = Double.parseDouble(elements[1]);
            lambda = Integer.parseInt(elements[2]);
            for (int i = 0; i < lambda; i++) {
                line = reader.readLine();
                elements = line.split("\t");
                int attacker_id = Integer.parseInt(elements[0]);
                int task_num = Integer.parseInt(elements[1]);
                Attacker attacker = new Attacker(attacker_id, K, L);
                id_to_attacker.put(attacker_id, attacker);
                for (int j = 0; j < task_num; j++) {
                    int task_id = Integer.parseInt(elements[2 * j + 2]);
                    int label = Integer.parseInt(elements[2 * j + 3]);
                    if (id_to_task.containsKey(task_id)) {
                        attacker.setTaskLabel(id_to_task.get(task_id), label);
                    } else {
                        attacker.setTaskLabel(id_to_golden.get(task_id), label);
                    }
                }
                line = reader.readLine();
                elements = line.split("\t");
                attacker_id = Integer.parseInt(elements[0]);
                int worker_num = Integer.parseInt(elements[1]);
                for (int j = 0; j < worker_num; j++) {
                    int worker_id = Integer.parseInt(elements[j + 2]);
                    Worker worker = id_to_worker.get(worker_id);
                    attacker.addWorker(worker);
                    worker.setAttackerID(attacker_id);
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* read requesting order of workers for the rth run */
    public void readOrder(String dataset, int r) {
        try {
            order = new ArrayList<Worker>();
            BufferedReader reader = new BufferedReader(new FileReader(dataset + "//" + r + "//order.txt"));
            String line = reader.readLine();
            while (line != null) {
                int worker_id = Integer.parseInt(line);
                order.add(id_to_worker.get(worker_id));
                line = reader.readLine();
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        Set<Worker> workers = new HashSet<Worker>(); // current workers in U
        int promotion_num = 0; // number of completed tasks that can be promoted
        int gold_num = 0; // number of golden task assignment
        ExtendedTD etd = new ExtendedTD(L); // extended truth discovery
        ProbabilisticTA pta = new ProbabilisticTA(tau, delta, alpha, K); // probabilistic task assignment

        long startTime = System.nanoTime();
        // respond to different worker activity
        for (Worker worker : order) {

            // case 1: a worker requests
            if (worker.isBanned()) {
                continue;
            } else {
                workers.add(worker);
            }
            Set<Task> golden_tasks = new HashSet<Task>(id_to_golden.values());
            Set<Task> normal_tasks = new HashSet<Task>(id_to_task.values());
            Task assigned_task = pta.assign(worker, golden_tasks, normal_tasks);
            if (assigned_task != null && worker.getAttackerID() != -1) {
                Attacker attacker = id_to_attacker.get(worker.getAttackerID());
                // update the observation of the attacker if a task is assigned to a Sybil worker
                attacker.observe(assigned_task);
            }

            // case 2: a worker labels a golden task
            if (id_to_golden.values().contains(assigned_task)) {
                gold_num++;
                if (worker.getAttackerID() == -1) {
                    worker.label(assigned_task, worker.getPairs().get(assigned_task));
                } else {
                    Attacker attacker = id_to_attacker.get(worker.getAttackerID());
                    int label = attacker.getTaskLabel(assigned_task);
                    worker.label(assigned_task, label);
                }

                // update s_j, r_j and p_j
                int s_count = 0;
                int r_count = 0;
                double r_correct = 0;
                double ls = 0;
                double hs = 0;

                for (Task task : worker.getLabeledPairs().keySet()) {
                    if (id_to_golden.values().contains(task)) {
                        r_count++;
                        task.calMajority();
                        int[] majority = task.getMajority();
                        int truth = task.getTrueLabel();
                        if (id_to_task.values().contains(task)) {
                            truth = task.getAggregated();
                        }
                        int answer = worker.getLabeledPairs().get(task);
                        for (int i = 0; i < L; i++) {
                            if (majority[i] == 1 && answer == i && i != truth) {
                                s_count++;
                            }
                        }
                        if (answer == truth) {
                            r_correct++;
                        }
                    }

                }
                worker.setS(2.0 / (1 + Math.pow(Math.E, -s_count)) - 1);
                worker.setR((2.0 / (1 + Math.pow(Math.E, -r_count / 3)) - 1) * r_correct / r_count);
                worker.setP(r_correct / r_count);


                // ban the worker if her Sybil score passes the Sybil threshold
                if (worker.getS() >= tau) {
                    worker.ban();
                    // remove the worker's labels on normal tasks
                    workers.remove(worker);
                    Set<Task> to_remove = new HashSet<Task>(worker.getLabeledPairs().keySet());
                    to_remove.removeAll(id_to_golden.values());
                    for (Task task : to_remove) {
                        task.expose();
                        worker.remove(task);
                        task.remove(worker);
                    }
                }
            }

            // case 3: a worker labels a normal task
            else if (id_to_task.values().contains(assigned_task)) {
                int attacker_id = worker.getAttackerID();
                int label = -1;

                //old
                if (attacker_id != -1) {
                    label = id_to_attacker.get(attacker_id).getTaskLabel(assigned_task);
                    // occasionally deviate from the sharing
                    Random rand = new Random();
                    if (rand.nextDouble() <= epsilon) {
                        int temp_label = rand.nextInt(L);
                        while (temp_label == label) {
                            temp_label = rand.nextInt(L);
                        }
                        label = temp_label;
                    }
                } else {
                    label = worker.getPairs().get(assigned_task);
                }

                //new

//                if (attacker_id != -1 && assigned_task.getTask_reward() > 13) {
//                    //The attack of a sybil attacker
//                    label = id_to_attacker.get(attacker_id).getTaskLabel(assigned_task);
//                    //if(assigned_task.getTask_reward())
//                    // occasionally deviate from the sharing
//                    Random rand = new Random();
//                    if (rand.nextDouble() <= epsilon) {
//                        int temp_label = rand.nextInt(L);
//                        while (temp_label == label) {
//                            temp_label = rand.nextInt(L);
//                        }
//                        label = temp_label;
//                    }
//                }
//                //new
//                else if (attacker_id != -1) {
//                    //For tasks with rewards less than or equal to 100
//                    //the intelligent sybil attacker will carefully answer the questions
//                    //and the accuracy rate is set to 80%
//                    Random rand = new Random();
//                    if (rand.nextDouble() <= 0.7) {
//                        label = assigned_task.getTrueLabel();
//                    } else {
//                        label = rand.nextInt(L);
//                        while (label == assigned_task.getTrueLabel()) {
//                            label = rand.nextInt(L);
//                        }
//                    }
//                } else {
//                    label = worker.getPairs().get(assigned_task);
//                }


                worker.label(assigned_task, label);

                // update the number of completed tasks that can be promoted
                if (assigned_task.getAssigned().size() >= K) {
                    assigned_task.calCi();
                    if (assigned_task.getCi() >= delta) {
                        promotion_num++;
                    }
                }
            }

            // if the batch condition is met, update aggregated labels and promote tasks
            if (promotion_num == B) {
                Set<Task> tasks = new HashSet<Task>(id_to_task.values());
                tasks.removeAll(id_to_golden.values());
                // run extended truth discovery
                etd.process(tasks, workers);
                for (Task task : tasks) {
                    if (task.getAssigned().size() >= K) {
                        task.calCi();
                        if (task.getCi() >= delta) {
                            id_to_golden.put(task.getTaskId(), task);
                        }
                    }
                }
                promotion_num = 0;

            }

            //System.out.println("222worker.getLabeledPairs().keySet().size():" + worker.getLabeledPairs().keySet().size());
            //new deference function
            for (Task task : worker.getLabeledPairs().keySet()) {

                //System.out.println("----task.getTaskId():" + task.getTaskId() + "----task.getTask_reward()" + task.getTask_reward());
                if (task.getTask_reward() <= 13) {
                    worker.setCLT();
                    if (task.getTrueLabel() == worker.getLabeledPairs().get(task)) {
                        worker.setCLR();
                    }
                } else {
                    worker.setCHT();
                    if (task.getAggregated() == worker.getLabeledPairs().get(task)) {
                        worker.setCHR();
                    }
                }
            }
            double ls = 0, hs = 0;
            double ql, qh;
            //System.out.println("-------------worker.getCLR():" + worker.getCLR() + "-------------worker.getCLT():" + worker.getCLT());
            //version1
            //if (worker.getCLT() > 0) ls = worker.getCLR() / worker.getCLT();
            //if (worker.getCHT() > 0) hs = worker.getCHR() / worker.getCHT();

            //version2
            if (worker.getCLT() > 0) ls = worker.getCLR() / worker.getCLT() * (2 / (1 + Math.pow(Math.E, -worker.getCLT())) - 1);
            if (worker.getCHT() > 0) hs = worker.getCHR() / worker.getCHT() * (2 / (1 + Math.pow(Math.E, -worker.getCHT())) - 1);
            //if (worker.getCLT() > 0) ql = worker.getCLR() / worker.getCLT();
//            else ql = 1 / L;
//            if (worker.getCHT() > 0)  qh = worker.getCHR() / worker.getCHT();
//            else qh = 1 / L;
//            //ls = ql * (2.0 / (1.0 + Math.pow(Math.E, -worker.getCLT()/3)) - 1);
//            hs = qh * (2.0 / (1.0 + Math.pow(Math.E, -worker.getCHT()/3)) - 1);

            if (ls >= 0.7 && hs <= 1 / L + 0.1) {
                //if (ls >= 0.8 && hs >= 0.8) {
                worker.ban();
                //System.out.println("succeed---------------------------------------------");
                // remove the worker's labels on normal tasks
                workers.remove(worker);
                Set<Task> to_remove = new HashSet<Task>(worker.getLabeledPairs().keySet());
                to_remove.removeAll(id_to_golden.values());
                for (Task task : to_remove) {
                    task.expose();
                    worker.remove(task);
                    task.remove(worker);
                }
            }

        }

        Set<Task> tasks = new HashSet<Task>(id_to_task.values());
        etd.process(tasks, workers);
        long endTime = System.nanoTime();

        a_accuracy = 0.0;

        //new
        attackGotReward = 0;

        for (Task task : id_to_task.values()) {
            if (task.getAggregated() == task.getTrueLabel()) {
                a_accuracy += 1;
            }

            //new
            for (Worker worker : task.getAssigned()) {
                if (worker.getAttackerID() != -1) {
                    if (worker.getLabeledPairs().get(task).intValue() == task.getAggregated()) {
                        attackGotReward += task.getTask_reward();
                    }
                }
            }

            task.reset();
        }
        for (Worker worker : id_to_worker.values()) {
            worker.reset();
        }
        a_accuracy = a_accuracy / id_to_task.size();
        Set<Task> exposed = new HashSet<Task>();
        for (Attacker attacker : id_to_attacker.values()) {
            for (Task golden : id_to_golden.values()) {
                if (attacker.getCount(golden) > K) {
                    exposed.add(golden);
                }
            }
        }

//        //new
//        for (Attacker attacker : id_to_attacker.values()) {
//            for (Worker worker : attacker.getWorkers()) {
//                int size = worker.getLabeledPairs().size();
//            }
//            attackGotReward += attacker.getAttackerReward();
//        }


        e_number = exposed.size();
        t_cost = gold_num * 1.0 / id_to_worker.size();
        running_time = (endTime - startTime) / 1000000;
    }

    /* return the aggregation accuracy */
    public double getAAccuracy() {
        return a_accuracy;
    }

    /* return the average number of exposed golden tasks */
    public double getENumber() {
        return e_number;
    }

    /* return the average number of golden task assignment for testing each worker */
    public double getTCost() {
        return t_cost;
    }

    /* return the running time */
    public long getRunningTime() {
        return running_time;
    }

    //new
    public int getAttackGotReward() {
        return attackGotReward;
    }

    /* main function */
    public static void main(String[] args) {
        if (args.length != 9 && args.length != 14) {
            System.out.println("Invalid number of parameters!");
            System.exit(0);
        }

        String dataset = args[0];
        int run_num = Integer.parseInt(args[1]);
        int B = Integer.parseInt(args[2]);
        double alpha = Double.parseDouble(args[3]);
        double tau = Double.parseDouble(args[4]);
        double delta = Double.parseDouble(args[5]);
        double mu = Double.parseDouble(args[6]);
        double epsilon = Double.parseDouble(args[7]);
        int lambda = Integer.parseInt(args[8]);

        if (args.length == 9) {
            Preprocess pre = new Preprocess(dataset, run_num, mu, epsilon, lambda);
            pre.formalize();
        } else {
            int N = Integer.parseInt(args[9]);
            int M = Integer.parseInt(args[10]);
            int L = Integer.parseInt(args[11]);
            int K = Integer.parseInt(args[12]);
            double theta = Double.parseDouble(args[13]);

            Preprocess pre = new Preprocess(dataset, run_num, mu, epsilon, lambda, N, M, L, K, theta);
            pre.formalize();
        }

        try {
            Date d = new Date();
            SimpleDateFormat sbf = new SimpleDateFormat("yyyyMMdd HHmmss");
            String t = sbf.format(d);
            BufferedWriter w1 = new BufferedWriter(new FileWriter(dataset + "//result.txt", true));
            // write worker number M, task number N, label size L and worker number per task K
            w1.write(dataset + " ");
            TDSSA tdssa = new TDSSA(B, alpha, tau, delta);
            tdssa.readNormal(dataset);

            double[] accuracy = new double[run_num]; // record aggregation accuracy in each run
            double[] exposed = new double[run_num]; // record number of exposed golden tasks in each run
            double[] cost = new double[run_num]; // record number of golden tasks for testing each worker in each run
            long[] time = new long[run_num]; // record running time in each run

            //new
            int[] attackGotReward = new int[run_num]; //attack got reward

            double ave_a_accuracy = 0.0; // average aggregation accuracy
            double ave_e_number = 0.0; // average number of exposed golden tasks
            double ave_t_cost = 0.0; // average number of golden tasks for testing each worker
            long ave_running_time = 0; // average running time

            //new
            double ave_attackGotReward = 0.0; //attack got reward

            for (int r = 0; r < run_num; r++) {
                tdssa.readGolden(dataset);
                tdssa.readAttack(dataset, r);
                tdssa.readOrder(dataset, r);
                tdssa.run();
                accuracy[r] = tdssa.getAAccuracy();
                exposed[r] = tdssa.getENumber();
                cost[r] = tdssa.getTCost();
                time[r] = tdssa.getRunningTime();

                //new
                attackGotReward[r] = tdssa.getAttackGotReward();

                ave_a_accuracy += tdssa.getAAccuracy();
                ave_e_number += tdssa.getENumber();
                ave_t_cost += tdssa.getTCost();
                ave_running_time += tdssa.getRunningTime();

                //new
                ave_attackGotReward += tdssa.getAttackGotReward() / 100;

                System.out.println("Run " + (r + 1) + " --- A-Accuracy:" + accuracy[r] + "  attackGotReward:" + attackGotReward[r] + "  E-Number:" + exposed[r] + "  T-Cost:" + cost[r] + "  Time:" + time[r] + "ms");
            }
            ave_a_accuracy /= run_num;
            ave_e_number /= run_num;
            ave_t_cost /= run_num;
            ave_running_time /= run_num;

            //new
            ave_attackGotReward /= run_num;

            double std_a_accuracy = 0.0; // standard error of A-Accuracy
            double std_e_number = 0.0; // standard error of E-Number
            double std_t_cost = 0.0; // standard error of T-Cost
            long std_running_time = 0; // standard error of Time

            //new
            double std_attackGotReward = 0.0; //standard error of attackGotReward

            for (int r = 0; r < run_num; r++) {
                std_a_accuracy += Math.pow(ave_a_accuracy - accuracy[r], 2);
                std_e_number += Math.pow(ave_e_number - exposed[r], 2);
                std_t_cost += Math.pow(ave_t_cost - cost[r], 2);
                std_running_time += Math.pow(ave_running_time - time[r], 2);

                //new
                std_attackGotReward += Math.pow(ave_attackGotReward - attackGotReward[r], 2);
            }
            std_a_accuracy = Math.sqrt(std_a_accuracy / (run_num - 1)) / Math.sqrt(run_num);
            std_e_number = Math.sqrt(std_e_number / (run_num - 1)) / Math.sqrt(run_num);
            std_t_cost = Math.sqrt(std_t_cost / (run_num - 1)) / Math.sqrt(run_num);
            std_running_time = (long) (Math.sqrt(std_running_time / (run_num - 1)) / Math.sqrt(run_num));

            //new
            std_attackGotReward = Math.sqrt(std_attackGotReward / (run_num - 1)) / Math.sqrt(run_num);

            System.out.println("\nAverage: ");
            System.out.println("A-Accuracy: " + ave_a_accuracy + "  Standard Eror: " + std_a_accuracy);
            System.out.println("E-Number :" + ave_e_number + "  Standard Eror: " + std_e_number);
            System.out.println("T-cost:" + ave_t_cost + "  Standard Eror: " + std_t_cost);
            System.out.println("Time:" + ave_running_time + "ms  Standard Eror: " + std_running_time);

            //new
            System.out.println("attackGotReward:" + ave_attackGotReward + "  Standard Eror: " + std_attackGotReward);


//            w1.write("\nAverage:\n");
//            w1.write("A-Accuracy: " + ave_a_accuracy + "  Standard Eror: " + std_a_accuracy + "\n");
//            w1.write("E-Number :" + ave_e_number + "  Standard Eror: " + std_e_number + "\n");
//            w1.write("T-cost:" + ave_t_cost + "  Standard Eror: " + std_t_cost + "\n");
//            w1.write("Time:" + ave_running_time + "ms  Standard Eror: " + std_running_time);

            //new
            //w1.write("attackGotReward:" + ave_attackGotReward + "  Standard Eror: " + std_attackGotReward);

            w1.write(B + " " + alpha + " " + tau + " " + delta + " " + mu + " " + epsilon + " " + lambda + " ");
            w1.write(String .format("%.6f",ave_a_accuracy) + " " + ave_attackGotReward + "\n");
            w1.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
