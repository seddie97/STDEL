STDEL是在TDSSA的基础上通过更新两个聚合公式达到提高真值准确率和降低女巫偷取奖励的目的

作者信息：
项目负责人：林德嘉；项目参与者：王玉玫、邹绍熙、苏国海、陈静静和范玉婷

以下是TDSSA算法的介绍：

TDSSA against Long-tail and have HS & LS

# Datasets and Code of Our KDD20 Paper

Please contact the following author if you need further clarification.<br />
Author: Yue Wang<br />
Contact: cat142500@hotmail.com

## SUMMARY
This folder includes the datasets and the source code for the following paper:

Title: Truth Discovery against Strategic Sybil Attack<br />
Authors: Yue Wang, Ke Wang and Chunyan Miao<br />
Conference: KDD 2020<br />
The link to our paper will be provided later.

Please cite this work if either the datasets or the code are used by your works.

We use two real datasets (NLP and DOG) and generate a synthetic dataset (SYN) for testing TDSSA. The details are provided in "DATASET DESCRIPTION".

We implement our TDSSA framework in Java. The details about how to run the program on Windows are provided in "RUNNING TDSSA".

## DATASET DESCRIPTION

Each of the two real datasets (NLP and DOG) and the synthetic dataset (SYN) dataset is characterized by five data parameters, including task number (N), worker number (M), optional label size (L), worker number per task (K), and average worker accuracy (theta). The following table summarizes the data parameters of the NLP and DOG datasets and the tested settings of the SYN datasets.

| Data Parameters | NLP | DOG | SYN |
| ---------- | ---- | ---- | ----- |
| Task Number (N) | 1000 | 807 | 1000 to 9000 |
| Worker Number (M) | 85 | 109 | 100 to 900 |
| Optional Label Size (L) | 2 | 4 | 2 to 5 |
| Worker Number per Task (K) | 20 | 10 | 5 to 20 |
| Average Worker Accuracy (theta) | 0.8 | 0.7 | 0.7 to 1 |

The original NLP dataset (http://www.sananalytics.com/lab/twitter-sentiment/) contained in the "NLP" folder provides four files:
1. "truth.csv" has a (task id, true label) pair in each line, representing the true label of a normal task.
2. "answer.csv" has a (task id, worker id, worker label) triple in each line, representing a worker's label on a normal task.
3. "quali_truth.csv" has a (task id, true label) pair in each line, representing the true label of a golden task. The total number of golden tasks is 20.
4. "quali.csv" has a (task id, worker id, worker label) triple in each line, representing a worker's label on a golden task.

The original DOG dataset (http://dbgroup.cs.tsinghua.edu.cn/ligl/crowddata/) contained in the "DOG" folder provides two files:
1. "truth.csv" has a (task id, true label) pair in each line, representing the true label of a normal task.
2. "answer.csv" has a (task id, worker id, worker label) triple in each line, representing a worker's label on a normal task.

The DOG dataset does not have information about golden tasks, so we create 20 golden tasks and sample each worker's labels on golden tasks based on their accuracy. For example, if a worker correctly labeled 8 out of 10 tasks in the original dataset, the worker would have 0.8 probability to provide the true label on a golden task and 0.2 probability to provide a false one.

Similarly, we generate workers' labels on tasks for the SYN datasets according to the average worker accuracy (theta). The data generation is done together with running TDSSA, described below. The default number of golden tasks is also 20.

## RUNNING TDSSA

The following command compiles our project:

javac TDSSA.java

To run the program on real datasets (NLP and DOG), the user needs to specify the dataset name, the number of runs, and the parameters for our TDSSA framework and injected Sybil attack. We summarize the TDSSA and attack parameters in the following two tables.

| TDSSA Parameters | Description | Setting |
| ---------------- | ----------- | ------- |
| Batch Condition (B) | Percentage of Sybil workers | 5 to 20 |
| Initial Golden Assignment Probability (alpha) | Initial probability to assign a golden task to a new worker | 0.1 to 0.9 |
| Sybil Threshold (tau) | Threshold of Sybil score for banning a worker | 0.6 to 1 |
| Reliability Threshold (delta) | Threshold of reliability score for trusting a worker | 0.6 to 1 |

| Attack Parameters | Description | Setting |
| ----------------- | ----------- | ------- |
| Sybil Proportion (mu) | Percentage of Sybil workers | 0.0 to 0.8 |
| Deviation Probability (epsilon) | Probability for Sybil workers to deviate from sharing | 0.0 to 0.3 |
| Attacker Number (lambda) | Number of Sybil attackers | 1 to 4 |

For example, the following command will run TDSSA 50 times on the NLP dataset with B = 10, alpha = 0.5, tau = 0.8, delta = 0.8, mu = 0.5, epsilon = 0.1 and lambda = 1.

java TDSSA NLP 50 10 0.5 0.1 1 0.5 0.1 1

To run the program on synthetic datasets (SYN), the user also needs to specify the five data parameters (N, M, L, K, theta). For example, with the same TDSSA and attack parameters, the following command will run TDSSA on a synthetic dataset with N = 5000, M = 500, L = 4, K = 10 and theta = 0.8.

java TDSSA SYN 50 10 0.5 0.1 1 0.5 0.1 1 5000 500 4 10 0.8

The performance of TDSSA will be evaluated based on four metrics:
1. A-Accuracy: the aggregation accuracy, i.e., the percentage of tasks whose aggregated label is identical to the true label.
2. E-Number: the number of golden tasks identified by the attacker.
3. T-Cost: the avergae number of golden tasks used for testing a worker.
4. Time: the running time of TDSSA.

The following sample output (saved in "result.txt") shows the performance of TDSSA in 50 runs, along with the averaged statistics and standard error.

<pre>
Run 1 --- A-Accuracy:0.87  E-Number:0.0  T-Cost:5.1647058823529415  Time:561ms
Run 2 --- A-Accuracy:0.896  E-Number:0.0  T-Cost:5.317647058823529  Time:379ms
Run 3 --- A-Accuracy:0.883  E-Number:0.0  T-Cost:5.235294117647059  Time:386ms
Run 4 --- A-Accuracy:0.8  E-Number:0.0  T-Cost:5.270588235294118  Time:604ms
Run 5 --- A-Accuracy:0.815  E-Number:0.0  T-Cost:5.376470588235295  Time:454ms
Run 6 --- A-Accuracy:0.922  E-Number:0.0  T-Cost:5.776470588235294  Time:371ms
Run 7 --- A-Accuracy:0.877  E-Number:0.0  T-Cost:4.811764705882353  Time:277ms
Run 8 --- A-Accuracy:0.61  E-Number:0.0  T-Cost:6.5058823529411764  Time:761ms
Run 9 --- A-Accuracy:0.816  E-Number:0.0  T-Cost:5.023529411764706  Time:239ms
Run 10 --- A-Accuracy:0.921  E-Number:0.0  T-Cost:6.258823529411765  Time:384ms
Run 11 --- A-Accuracy:0.886  E-Number:0.0  T-Cost:5.341176470588235  Time:288ms
Run 12 --- A-Accuracy:0.898  E-Number:0.0  T-Cost:5.764705882352941  Time:380ms
Run 13 --- A-Accuracy:0.882  E-Number:0.0  T-Cost:5.411764705882353  Time:262ms
Run 14 --- A-Accuracy:0.884  E-Number:0.0  T-Cost:4.811764705882353  Time:377ms
Run 15 --- A-Accuracy:0.899  E-Number:0.0  T-Cost:5.376470588235295  Time:326ms
Run 16 --- A-Accuracy:0.684  E-Number:0.0  T-Cost:5.847058823529411  Time:390ms
Run 17 --- A-Accuracy:0.867  E-Number:0.0  T-Cost:5.729411764705882  Time:382ms
Run 18 --- A-Accuracy:0.854  E-Number:0.0  T-Cost:5.305882352941176  Time:318ms
Run 19 --- A-Accuracy:0.792  E-Number:0.0  T-Cost:5.0588235294117645  Time:348ms
Run 20 --- A-Accuracy:0.825  E-Number:0.0  T-Cost:5.094117647058823  Time:396ms
Run 21 --- A-Accuracy:0.876  E-Number:0.0  T-Cost:4.847058823529411  Time:275ms
Run 22 --- A-Accuracy:0.858  E-Number:0.0  T-Cost:5.211764705882353  Time:304ms
Run 23 --- A-Accuracy:0.897  E-Number:0.0  T-Cost:4.847058823529411  Time:479ms
Run 24 --- A-Accuracy:0.876  E-Number:0.0  T-Cost:5.729411764705882  Time:301ms
Run 25 --- A-Accuracy:0.9  E-Number:0.0  T-Cost:5.776470588235294  Time:376ms
Run 26 --- A-Accuracy:0.849  E-Number:0.0  T-Cost:5.447058823529412  Time:515ms
Run 27 --- A-Accuracy:0.75  E-Number:0.0  T-Cost:6.364705882352941  Time:641ms
Run 28 --- A-Accuracy:0.892  E-Number:0.0  T-Cost:5.1647058823529415  Time:274ms
Run 29 --- A-Accuracy:0.9  E-Number:0.0  T-Cost:5.270588235294118  Time:364ms
Run 30 --- A-Accuracy:0.894  E-Number:0.0  T-Cost:4.91764705882353  Time:320ms
Run 31 --- A-Accuracy:0.895  E-Number:0.0  T-Cost:5.341176470588235  Time:291ms
Run 32 --- A-Accuracy:0.923  E-Number:0.0  T-Cost:5.517647058823529  Time:334ms
Run 33 --- A-Accuracy:0.887  E-Number:0.0  T-Cost:4.91764705882353  Time:321ms
Run 34 --- A-Accuracy:0.906  E-Number:0.0  T-Cost:5.270588235294118  Time:429ms
Run 35 --- A-Accuracy:0.92  E-Number:0.0  T-Cost:5.552941176470588  Time:410ms
Run 36 --- A-Accuracy:0.88  E-Number:0.0  T-Cost:5.270588235294118  Time:364ms
Run 37 --- A-Accuracy:0.893  E-Number:0.0  T-Cost:5.129411764705883  Time:324ms
Run 38 --- A-Accuracy:0.888  E-Number:0.0  T-Cost:5.129411764705883  Time:364ms
Run 39 --- A-Accuracy:0.887  E-Number:0.0  T-Cost:5.6  Time:379ms
Run 40 --- A-Accuracy:0.858  E-Number:0.0  T-Cost:5.305882352941176  Time:451ms
Run 41 --- A-Accuracy:0.91  E-Number:0.0  T-Cost:5.305882352941176  Time:332ms
Run 42 --- A-Accuracy:0.768  E-Number:0.0  T-Cost:6.08235294117647  Time:552ms
Run 43 --- A-Accuracy:0.79  E-Number:0.0  T-Cost:5.623529411764705  Time:526ms
Run 44 --- A-Accuracy:0.877  E-Number:0.0  T-Cost:5.235294117647059  Time:378ms
Run 45 --- A-Accuracy:0.918  E-Number:0.0  T-Cost:5.705882352941177  Time:350ms
Run 46 --- A-Accuracy:0.843  E-Number:0.0  T-Cost:5.623529411764705  Time:290ms
Run 47 --- A-Accuracy:0.862  E-Number:0.0  T-Cost:5.023529411764706  Time:278ms
Run 48 --- A-Accuracy:0.851  E-Number:0.0  T-Cost:5.317647058823529  Time:281ms
Run 49 --- A-Accuracy:0.836  E-Number:0.0  T-Cost:4.952941176470588  Time:440ms
Run 50 --- A-Accuracy:0.869  E-Number:0.0  T-Cost:5.129411764705883  Time:381ms

Average:
A-Accuracy: 0.8606799999999997  Standard Eror: 0.00847822854042479
E-Number :0.0  Standard Eror: 0.0
T-cost:5.381882352941179  Standard Eror: 0.05512216629071057
Time:384ms  Standard Eror: 14
</pre>
