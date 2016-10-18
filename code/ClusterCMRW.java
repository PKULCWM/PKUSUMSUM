package code;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.TreeSet;

/**
 * args[0]:The path of the input.
 * 		Single-document summarization task: The path of the input file and this file can only contain one document you want to get the summary from.
 * 		Multi-document summarization task or topic-based multi-document summarization task: The path of the input directory and this directory can only contain one document set you want to get the summary from.
 * args[1]:The path of the output file and one file only contains one summary.
 * args[2]:The language of the document. 1: Chinese, 2: English, 3: other Western languages
 * args[3]:Specify which task to do.
 * 		1: single-document summarization, 2: multi-document summarization,
 * 		3: topic-based multi-document summarization
 * args[4]:The expected number of words in summary.
 * args[5]:Choose if you want to stem the input. (Only for English document) 
 * 		1: stem, 2: no stem, default = 1
 * args[6]:Choose whether you need remove the stop words.
 * 		If you need remove the stop words, you should input the path of stop word list. 
 *  	Or we have prepared an English stop words list as file ¡°stopword_Eng¡±, you can use it by input ¡°y¡±.
 *   	If you don¡¯t need remove the stop words, please input ¡°n¡±.
 * args[7]:Specify which redundancy removal method to use. ILP and Submodular needn't extra redundancy removal. default = 3 for ManifoldRank, default = 1 for the other methods which need redundancy removal
 * 		1: MMR
 *		2: threshold: If the similarity between an unchosen sentence and the sentence chosen this time is upper than the threshold, this unchosen sentence will be deleted from candidate set.
 *		3: sum punishment: The scores of all unchosen sentences will decrease the product of penalty ratio and the similarity with the sentence chosen this time.
 * args[8]:The parameter of redundancy removal methods. default = 0.7
 *		For MMR and sum punishment: it represents the penalty ratio. 
 *		For threshold: it represents the threshold.
 * args[9]:[0, 1] A scaling factor of sentence length when we choose sentences. default = 0.1
 * args[10]:[0, 1] The ratio controlling the expected cluster number for the document set. default = 0.1
 * args[11]:[0, 1] The combination weight controlling the relative contributions from the source cluster and the destination cluster. default = 0.8
 * */

public class ClusterCMRW {
    public doc myDoc = new doc();//some basic information about doc
    public int k;//Kmeans-k
    public ArrayList<TreeSet<Integer>> cluster = new ArrayList<>();//cluster_center
    public TreeSet<Integer> docVector;
    public double[] cdSim;// the similarity between cluster and doc
    public double[] scSim;//the similarity between sentence and cluster
    public double[] sdSim;//the similarity between sentence and doc
    public ArrayList<Integer> d_tf;// tf vector of doc
    public ArrayList<ArrayList<Integer>> c_tf;// tf vector of cluster
    public ArrayList<Integer> c_len;//length of the cluster vector
    public int d_len;// length of the doc vector
    public int[] belong;
    public double[] score;//score of the sentence
    public double Lambda,Mu,Alpha,Beta;
    
    public void Summarize(String args[]) throws IOException
    {
    	if(args[3].equals("1")){
			System.out.println("The ClusterCMRW method can't solve single-document summarization task.");
			return;
		}
		else if(args[3].equals("3")){
			System.out.println("The ClusterCMRW method can't solve topic-based multi-document summarization task.");
			return;
		}
    	
        myDoc = new doc();
        File myfile = new File(args[0]);
        myDoc.readfile(myfile.list(),args[0],args[2],args[6]);
        myDoc.maxlen = Integer.parseInt(args[4])+20;
        int sx = Integer.parseInt(args[5]);
        myDoc.calc_tfidf(1, sx);;//1 represents use tf-isf vector;2 represent tf-idf vector
        myDoc.calc_sim();
        Alpha = 0.3;
        Lambda = 0.85;
        Beta = 0.1;
        Mu = 0.85;
        if (Double.parseDouble(args[10])>=0){
            Alpha = Double.parseDouble(args[10]);
        }
        if (Double.parseDouble(args[9])>=0){
            Beta = Double.parseDouble(args[9]);
        }
        if (Double.parseDouble(args[11])>=0){
            Lambda = Double.parseDouble(args[11]);
        }
        k = (int)(myDoc.snum * Alpha);

        Kmeans();
        generate_docVector();
        calc_cdSim();
        calc_scSim();
        CMRW();

        /* Remove redundancy and get the abstract */
        if (args[7].equals("-1"))
            myDoc.pick_sentence_MMR(score,Double.parseDouble(args[8]),Beta);
        if (args[7].equals("1"))
            myDoc.pick_sentence_MMR(score,Double.parseDouble(args[8]),Beta);
        else
        if (args[7].equals("2"))
            myDoc.pick_sentence_threshold(score, Double.parseDouble(args[8]),Beta);
        else
        if (args[7].equals("3"))
            myDoc.pick_sentence_sumPun(score,Double.parseDouble(args[8]));

        /* Output the abstract */
    	try{
    		File outfile = new File(args[1]);
    		OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(outfile),"utf-8");
    		BufferedWriter writer = new BufferedWriter(write);
    		for (int i : myDoc.summary_id){
                //System.out.println(myDoc.original_sen.get(i));
    			writer.write(myDoc.original_sen.get(i));
    			writer.write("\n");
            }
    		writer.close();
    	}
    	catch(Exception e){
    		System.out.println("There are errors in the output.");
    		e.printStackTrace();
    	}
    }

    public void calc_cdSim(){
        cdSim = new double[k];
        for (int i = 0; i < k; i++){
            cdSim[i] = myDoc.calc_cos(cluster.get(i),c_tf.get(i),c_len.get(i),docVector,d_tf,d_len);
        }

    }

    public void calc_scSim(){
        scSim = new double[myDoc.snum];
        sdSim = new double[myDoc.snum];
        for (int i = 0; i < myDoc.snum; i++) {
            scSim[i] = myDoc.calc_cos(myDoc.vector.get(i),myDoc.s_tf.get(i),myDoc.word_len.get(i), cluster.get(belong[i]),c_tf.get(belong[i]),c_len.get(belong[i]));
            sdSim[i] = scSim[i] * cdSim[belong[i]];
        }
    }

    public void generate_docVector(){
        int[] tmp_tf = new int[myDoc.wnum];
        docVector = new TreeSet<>();
        d_tf = new ArrayList<>();
        for (int i = 0; i < myDoc.snum; i++){
            int id = 0;
            for (int j : myDoc.vector.get(i)){
                tmp_tf[j] += myDoc.s_tf.get(i).get(id);
                id++;
            }
        }
        for (int i = 0; i < myDoc.wnum; i++){
            if (tmp_tf[i] != 0){
                docVector.add(i);
                d_tf.add(tmp_tf[i]);
                d_len += tmp_tf[i];
            }
        }
    }

    public void CMRW(){
        double[] last_score = new double[myDoc.snum];
        score = new double[myDoc.snum];
        for (int i = 0; i < myDoc.snum; i++){
            score[i] = 1 / myDoc.snum;
            last_score[i] = score[i];
        }
        boolean change = true;
        while (change){
            for (int i = 0; i < myDoc.snum; i++)
                score[i] = (1 - Mu) / myDoc.snum;
            for (int i = 0; i < myDoc.snum; i++){
                double sum = 0;
                for (int j = 0; j < myDoc.snum; j++)
                if (i != j)
                    sum += myDoc.sim[i][j] * (Lambda * sdSim[i] + (1 - Lambda) * sdSim[j]);
                if (sum == 0) continue;
                for (int j = 0; j < myDoc.snum; j++)
                if (i != j)
                    score[j] += Mu * last_score[i] * myDoc.sim[i][j] * (Lambda * sdSim[i] + (1 - Lambda) * sdSim[j]) / sum;
            }
            change = false;
            for (int i = 0; i < myDoc.snum; i++)
                if (Math.abs(score[i] - last_score[i]) > 1e-5){
                    change = true;
                    break;
                }
            for (int i = 0; i < myDoc.snum; i++)
                last_score[i] = score[i];
        }
    }

    public void Kmeans(){
        boolean[] chosen = new boolean[myDoc.snum];
        c_len = new ArrayList<>();
        c_tf = new ArrayList<>();
        int maxdep = 2000;
        cluster = new ArrayList<>();
        for (int i = 0; i < k; i++){

            int q = (int)(Math.random() * myDoc.snum);
            while (chosen[q]){
                q = (int)(Math.random() * myDoc.snum);
            }
            chosen[q] = true;
            cluster.add(myDoc.vector.get(q));
            c_tf.add(myDoc.s_tf.get(q));
            c_len.add(myDoc.word_len.get(q));
        }
        boolean change = true;
        belong = new int[myDoc.snum];
        int[] last_belong = new int[myDoc.snum];
        while (change){
            maxdep--;
            if (maxdep == 0) break;
            for (int i = 0; i < myDoc.snum; i++)
                last_belong[i] = belong[i];
            for (int i = 0; i < myDoc.snum; i++){
                double minDis = 0;
                for (int j = 0; j < k; j++){
                    double dis = myDoc.calc_cos(myDoc.vector.get(i),myDoc.s_tf.get(i),myDoc.word_len.get(i),cluster.get(j),c_tf.get(j),c_len.get(j));
                    if (dis > minDis){
                        minDis = dis;
                        belong[i] = j;
                    }
                }
            }

            change = false;
            for (int i = 0; i < myDoc.snum; i++)
                if (last_belong[i] != belong[i]){
                    change = true;
                    break;
                }
            if (change){
                c_tf = new ArrayList<>();
                cluster = new ArrayList<>();
                c_len = new ArrayList<>();
                int[][] tmp_tf = new int[k][myDoc.wnum];
                int[] tmp_len = new int[k];
                for (int i = 0; i < myDoc.snum; i++){
                    int id = 0;
                    for (int j : myDoc.vector.get(i)){
                        tmp_tf[belong[i]][j] += myDoc.s_tf.get(i).get(id);
                        tmp_len[belong[i]] += myDoc.s_tf.get(i).get(id);
                        id++;
                    }
                }
                for (int i = 0; i < k; i++){
                    ArrayList<Integer> tp_tf = new ArrayList<>();
                    TreeSet<Integer> tp_vector = new TreeSet<>();
                    for (int j = 0; j < myDoc.wnum; j++)
                        if (tmp_tf[i][j] != 0){
                            tp_tf.add(tmp_tf[i][j]);
                            tp_vector.add(j);
                        }
                    c_tf.add(tp_tf);
                    cluster.add(tp_vector);
                    c_len.add(tmp_len[i]);
                }
            }
        }
    }
}
