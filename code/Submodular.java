package code;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

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
 * args[7]:Type of submodular method. 
 * 		1: Li's paper (Li at el, 2012)
 *		2: modification method from Lin's paper (Lin and Bilmes, 2010) default = 2
 * args[8]:[0, 1] A scaling factor of sentence length when we choose sentences. default = 0.1
 * args[9]:[0, 1] Threshold coefficient. default = 0.5
 * args[10]:[0, 1] Trade-off coefficient. 
 *		default = 0.15 in multi-document task and default = 0.5 in single-document task
 * */


public class Submodular {
    public doc myDoc = new doc();//some basic information about doc
    public ArrayList<Integer> summary_id = new ArrayList<>();
    public double Alpha,Beta,Lambda;
    int op;//op 1 represents use submodular function 1;else use submodular function 2
    public double[] sumSim;//the sum of similarity
    public void Summarize(String args[]) throws IOException
    {
    	if(args[3].equals("3")){
			System.out.println("The Submodular method can't solve topic-based multi-document summarization task.");
			return;
		}
    	
        summary_id = new ArrayList<>();
        myDoc = new doc();
        if (args[3].equals("1"))//single document
        {
            Alpha = 1.0/myDoc.snum*10;
            Beta = 0.1;
            Lambda = 0.5;
            String[] single_file = new String[1];
            single_file[0] = args[0];
            op = Integer.parseInt(args[7]);
            if (op == 1){
                Alpha = 1;
            }
            if (Double.parseDouble(args[9]) >= 0){
                Alpha = Double.parseDouble(args[9]);
            }
            if (Double.parseDouble(args[8]) >= 0){
                Beta = Double.parseDouble(args[8]);
            }
            if (Double.parseDouble(args[10]) >= 0){
                Lambda = Double.parseDouble(args[10]);
            }
            myDoc.maxlen = Integer.parseInt(args[4])+20;
            myDoc.readfile(single_file," ",args[2],args[6]);
			int sx = Integer.parseInt(args[5]);
            myDoc.calc_tfidf(1, sx);
            myDoc.calc_sim();
            greedy();
            
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
        /* Multi document */
        else if (args[3].equals("2")) {
            File myfile = new File(args[0]);
            myDoc.maxlen = Integer.parseInt(args[4])+20;
            myDoc.readfile(myfile.list(),args[0],args[2],args[6]);
            int sx = Integer.parseInt(args[5]);
            myDoc.calc_tfidf(1, sx);
            myDoc.calc_sim();
            op = Integer.parseInt(args[7]);
            Alpha = 1.0 / myDoc.snum*10;
            Beta = 0.1;
            Lambda = 0.15;
            if (op == 1){
                Alpha = 1;
            }
            if (Double.parseDouble(args[9]) >= 0){
                Alpha = Double.parseDouble(args[9]);
            }
            if (Double.parseDouble(args[8]) >= 0){
                Beta = Double.parseDouble(args[8]);
            }
            if (Double.parseDouble(args[10]) >= 0){
                Lambda = Double.parseDouble(args[10]);
            }
            greedy();
           
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
    }

    public double submod1(int id)
    {
        double score = 0;
        for (int i = 0; i < myDoc.snum; i++){
            if (i == id) continue;
            double sum = 0;
            for (int j : myDoc.summary_id)
            if (i != j)
                sum += myDoc.sim[i][j];
            if (id != -1)
                sum += myDoc.sim[i][id];

            if (sum > sumSim[i] * Alpha)
                sum = sumSim[i] * Alpha;
            score += sum;
        }
        return score;
    }
    public double submod2(int id){
        double score=0;
        for (int i : myDoc.summary_id) {
            if (op == 1) {
                score += myDoc.sim[id][i];
            } else {
                if (myDoc.sim[id][i] > score)
                    score = myDoc.sim[id][i];
            }
        }
        return -score;
    }

    public void calc_sumSim(){
        sumSim = new double[myDoc.snum];
        for (int i = 0; i < myDoc.snum; i++){
            sumSim[i] = 0;
            for (int j = 0; j < myDoc.snum; j++)
            if (i!=j)
                sumSim[i] += myDoc.sim[i][j];
        }
    }

    /* pick sentence using greedy algorithm */
    public void greedy(){
        boolean[] chosen = new boolean[myDoc.snum];
        int len=0;
        calc_sumSim();
        while (true){
            double maxInc = -10, initScore = submod1(-1);
            int maxId = -1;
            for (int i = 0; i < myDoc.snum; i++){
                if (!chosen[i] && len+myDoc.sen_len.get(i)<myDoc.maxlen){
                    double inc = (Lambda * submod1(i) +(1-Lambda) * submod2(i) - initScore*Lambda)/Math.pow(myDoc.sen_len.get(i),Beta);
                    if (inc > maxInc){
                        maxInc = inc;
                        maxId = i;
                    }
                }
            }

            if (maxId == -1) break;
            chosen[maxId] = true;
            len += myDoc.sen_len.get(maxId);
            myDoc.summary_id.add(maxId);
            if (len >= myDoc.maxlen-20)
                break;
        }
    }
}
