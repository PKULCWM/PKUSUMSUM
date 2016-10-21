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
    public Doc myDoc = new Doc();//some basic information about doc
    public int k;//Kmeans-k
    public ArrayList<TreeSet<Integer>> cluster = new ArrayList<>();//cluster_center
    public TreeSet<Integer> docVector; //the vector of the document
    public double[] cdSim;// the similarity between cluster and doc
    public double[] scSim;//the similarity between sentence and cluster
    public double[] sdSim;//the similarity between sentence and doc
    public ArrayList<Integer> dTf;// tf vector of doc
    public ArrayList<ArrayList<Integer>> cTf;// tf vector of cluster
    public ArrayList<Integer> cLen;//length of the cluster vector
    public int dLen;// length of the doc vector
    public int[] belong;// the cluster center that sentence belongs to
    public double[] score;//score of the sentence
    public double Lambda,Mu,Alpha,Beta;//parameter
    
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
    	
        myDoc = new Doc();
        File myfile = new File(args[0]);
        myDoc.readfile(myfile.list(),args[0],args[2],args[6]);
        //allow the length of summary to exceed 20
        myDoc.maxlen = Integer.parseInt(args[4])+20;
        int sx = Integer.parseInt(args[5]);
        myDoc.calcTfidf(1, sx);//1 represents use tf-isf vector;2 represent tf-idf vector
        myDoc.calcSim();
        Alpha = 0.3;
        Lambda = 0.85;
        Beta = 0.1;
        Mu = 0.85;
        // get the parameter
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
        generateDocVector();
        calcCdSim();
        calcScSim();
        CMRW();

        /* Remove redundancy and get the abstract */
        if (args[7].equals("-1"))
            myDoc.pickSentenceMMR(score,Double.parseDouble(args[8]),Beta);
        if (args[7].equals("1"))
            myDoc.pickSentenceMMR(score,Double.parseDouble(args[8]),Beta);
        else
        if (args[7].equals("2"))
            myDoc.pickSentenceThreshold(score, Double.parseDouble(args[8]),Beta);
        else
        if (args[7].equals("3"))
            myDoc.pickSentenceSumpun(score,Double.parseDouble(args[8]));

        /* Output the abstract */
    	try{
    		File outfile = new File(args[1]);
    		OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(outfile),"utf-8");
    		BufferedWriter writer = new BufferedWriter(write);
    		for (int i : myDoc.summaryId){
                //System.out.println(myDoc.originalSen.get(i));
    			writer.write(myDoc.originalSen.get(i));
    			writer.write("\n");
            }
    		writer.close();
    	}
    	catch(Exception e){
    		System.out.println("There are errors in the output.");
    		e.printStackTrace();
    	}
    }

    // calculate the similarity between cluster and document
    public void calcCdSim(){
        cdSim = new double[k];
        for (int i = 0; i < k; i++){
            cdSim[i] = myDoc.calcCos(cluster.get(i), cTf.get(i), cLen.get(i),docVector, dTf, dLen);
        }

    }

    // calculate the similarity between cluster and sentence
    public void calcScSim(){
        scSim = new double[myDoc.snum];
        sdSim = new double[myDoc.snum];
        for (int i = 0; i < myDoc.snum; i++) {
            scSim[i] = myDoc.calcCos(myDoc.sVector.get(i),myDoc.sTf.get(i),myDoc.wordLen.get(i), cluster.get(belong[i]), cTf.get(belong[i]), cLen.get(belong[i]));
            sdSim[i] = scSim[i] * cdSim[belong[i]];
        }
    }

    // get the vector of the document
    public void generateDocVector(){
        int[] tmptf = new int[myDoc.wnum];
        docVector = new TreeSet<>();
        dTf = new ArrayList<>();
        for (int i = 0; i < myDoc.snum; i++){
            int id = 0;
            for (int j : myDoc.sVector.get(i)){
                tmptf[j] += myDoc.sTf.get(i).get(id);
                id++;
            }
        }
        for (int i = 0; i < myDoc.wnum; i++){
            if (tmptf[i] != 0){
                docVector.add(i);
                dTf.add(tmptf[i]);
                dLen += tmptf[i];
            }
        }
    }

    //CMRW algorithm
    public void CMRW(){
        double[] lastScore = new double[myDoc.snum];
        score = new double[myDoc.snum];
        for (int i = 0; i < myDoc.snum; i++){
            score[i] = 1 / myDoc.snum;
            lastScore[i] = score[i];
        }

        //iterate until converges; similar to PageRank
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
                    score[j] += Mu * lastScore[i] * myDoc.sim[i][j] * (Lambda * sdSim[i] + (1 - Lambda) * sdSim[j]) / sum;
            }
            change = false;
            for (int i = 0; i < myDoc.snum; i++)
                if (Math.abs(score[i] - lastScore[i]) > 1e-5){
                    change = true;
                    break;
                }
            for (int i = 0; i < myDoc.snum; i++)
                lastScore[i] = score[i];
        }
    }

    //using Kmeans to clustering
    public void Kmeans(){
        boolean[] chosen = new boolean[myDoc.snum];
        cLen = new ArrayList<>();
        cTf = new ArrayList<>();
        int maxdep = 2000;
        //generate the initial cluster center
        cluster = new ArrayList<>();
        for (int i = 0; i < k; i++){

            int q = (int)(Math.random() * myDoc.snum);
            while (chosen[q]){
                q = (int)(Math.random() * myDoc.snum);
            }
            chosen[q] = true;
            cluster.add(myDoc.sVector.get(q));
            cTf.add(myDoc.sTf.get(q));
            cLen.add(myDoc.wordLen.get(q));
        }

        //iterate until converges
        boolean change = true;
        belong = new int[myDoc.snum];
        int[] lastBelong = new int[myDoc.snum];
        while (change){
            maxdep--;
            if (maxdep == 0) break;
            for (int i = 0; i < myDoc.snum; i++)
                lastBelong[i] = belong[i];
            // find the nearest cluster center
            for (int i = 0; i < myDoc.snum; i++){
                double minDis = 0;
                for (int j = 0; j < k; j++){
                    double dis = myDoc.calcCos(myDoc.sVector.get(i),myDoc.sTf.get(i),myDoc.wordLen.get(i),cluster.get(j), cTf.get(j), cLen.get(j));
                    if (dis > minDis){
                        minDis = dis;
                        belong[i] = j;
                    }
                }
            }

            //update
            change = false;
            for (int i = 0; i < myDoc.snum; i++)
                if (lastBelong[i] != belong[i]){
                    change = true;
                    break;
                }
            //update the cluster center
            if (change){
                cTf = new ArrayList<>();
                cluster = new ArrayList<>();
                cLen = new ArrayList<>();
                int[][] tmpTf = new int[k][myDoc.wnum];
                int[] tmpLen = new int[k];
                for (int i = 0; i < myDoc.snum; i++){
                    int id = 0;
                    for (int j : myDoc.sVector.get(i)){
                        tmpTf[belong[i]][j] += myDoc.sTf.get(i).get(id);
                        tmpLen[belong[i]] += myDoc.sTf.get(i).get(id);
                        id++;
                    }
                }
                for (int i = 0; i < k; i++){
                    ArrayList<Integer> tpTf = new ArrayList<>();
                    TreeSet<Integer> tpVector = new TreeSet<>();
                    for (int j = 0; j < myDoc.wnum; j++)
                        if (tmpTf[i][j] != 0){
                            tpTf.add(tmpTf[i][j]);
                            tpVector.add(j);
                        }
                    cTf.add(tpTf);
                    cluster.add(tpVector);
                    cLen.add(tmpLen[i]);
                }
            }
        }
    }
}
