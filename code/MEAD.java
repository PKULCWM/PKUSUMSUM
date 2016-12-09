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
 * args[10]:The path of the topic file. (Only for topic-based multi-document summarization task)
 * */


public class MEAD {
	public Doc myDoc = new Doc();
	public double[] TfIdf;
    public double[] C;
    public double[] P;
    public double[] F;
    public double[] Score;
    public int sumNum = 0;
    public void Summarize(String args[]) throws IOException
    {
    	
    	
    	/* Read files */
    	if (args[3].equals("1"))
        {
    		String[] singleFile = new String[1];
            singleFile[0] = args[0];
            myDoc.maxlen = Integer.parseInt(args[4]);
            myDoc.readfile(singleFile, " ", args[2],args[6]);
        }
    	else if (args[3].equals("2"))
        {
    		File myfile = new File(args[0]);
            myDoc.maxlen = Integer.parseInt(args[4]);
            myDoc.readfile(myfile.list(),args[0],args[2], args[6]);
            
        }else if (args[3].equals("3")){
        	/* Read topic */
        	if (!args[10].equals("-1")) {
        		myDoc.readTopic(args[10], args[2], args[6]);
        	}
        	File myfile = new File(args[0]);
            myDoc.maxlen = Integer.parseInt(args[4]);
            myDoc.readfile(myfile.list(),args[0],args[2], args[6]);
        }
    	
    	/* Calculate tf*idf */
    	myDoc.calcTfidf(Integer.parseInt(args[3]), Integer.parseInt(args[5]));
    	myDoc.calcSim();
    	int wordNum = myDoc.dTf.size();
    	
    	TfIdf = new double[wordNum];
    	double CMax = 0.0;
    	for(int i = 0; i < wordNum; ++i) {
    		TfIdf[i] = myDoc.dTf.get(i) * myDoc.idf[i];
    		CMax += TfIdf[i];
    	}
    	
    	/* Calculate CMax for each document */
    	ArrayList<TreeSet<Integer>> fVector = new ArrayList<TreeSet<Integer>>();
    	double[] fCMax = new double[myDoc.fnum];
    	int fNumNow = 0;
    	P = new double[myDoc.snum];
    	TreeSet<Integer> tmpSet = new TreeSet<Integer>();
    	for(int i = 0; i < myDoc.snum; ++i) {
    		if(i >= myDoc.rRange[fNumNow]) {
    			fNumNow++;
    			fVector.add(tmpSet);
    			tmpSet.clear();
    		}
    		for(int j : myDoc.sVector.get(i)) {
    			tmpSet.add(j);
    		}
    	}
    	fVector.add(tmpSet);
    	
    	for(int i = 0; i < myDoc.fnum; ++i) {
    		for(int j : fVector.get(i)) {
    			fCMax[i] += TfIdf[j];
    		}
    	}
    	
    	/* Calculate C score of sentences */
    	C = new double[myDoc.snum];
    	for(int i = 0; i < myDoc.snum; ++i) {
    		C[i] = 0.0;
    		for(int j = 0; j < wordNum; ++j) {
    			if(myDoc.sVector.get(i).contains(j)) {
    				C[i] += TfIdf[j];
    			}
    		}
    	}
    	
    	/* Calculate P score of sentences */
    	fNumNow = 0;
    	P = new double[myDoc.snum];
    	for(int i = 0; i < myDoc.snum; ++i) {
    		if(args[3].equals("1")) {
    			P[i] = (myDoc.snum - i) * 1.0 * CMax / (myDoc.snum * 1.0);
    		}
    		else if(args[3].equals("2") || args[3].equals("3")) {
    			if(i >= myDoc.rRange[fNumNow]) {
    				fNumNow++;
    			}
    			int fSnum = myDoc.rRange[fNumNow] - myDoc.lRange[fNumNow];
				P[i] = (fSnum - (i - myDoc.lRange[fNumNow])) * 1.0 * fCMax[fNumNow] / (fSnum * 1.0);
    		}
    	}
    	
    	/* Calculate F score of sentences */
    	F = new double[myDoc.snum];
    	for(int i = 0; i < myDoc.snum; ++i) {
    		F[i] = 0.0;
    		int k = 0;
    		for(int j : myDoc.sVector.get(i)) {
    			F[i] += myDoc.sTf.get(i).get(k) * TfIdf[j];
    			k++;
    		}
    	}
    	
    	
    	/* Calculate MEAD Score of sentences */
    	Score = new double[myDoc.snum];
    	for(int i = 0; i < myDoc.snum; ++i) {
    		/* No topic-focused */
    		if(args[10].equals("-1")) {
    			Score[i] = C[i] + P[i] + F[i];
    		}
    		/* Topic-focused document summarization */
    		else {
    			if(i == 0) 
    				Score[i] = 0.0;
    			else 
    				Score[i] = C[i] + P[i] + F[i] + myDoc.sim[i][0];
    		}
    	}
    	
    	/* Set redundancy removal method and parameter */    	
    	double threshold = 0.9, Beta = 0.1;
    			
    	if (Double.parseDouble(args[8])>=0){
    		threshold = Double.parseDouble(args[8]);
    	}
    	if (Double.parseDouble(args[9])>=0){
    		Beta = Double.parseDouble(args[9]);
    	}
    	
    	/* Remove redundancy and get the abstract */
    	if (args[7].equals("-1"))
			myDoc.pickSentenceMMR(Score, threshold, Beta);
    	else if (args[7].equals("1"))
            myDoc.pickSentenceMMR(Score, threshold, Beta);
        else if (args[7].equals("2"))
            myDoc.pickSentenceThreshold(Score, threshold, Beta);
        else if (args[7].equals("3"))
            myDoc.pickSentenceSumpun(Score, threshold);
    	
    	/* Output the abstract */
    	try{
    		File outfile = new File(args[1]);
    		OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(outfile),"utf-8");
    		BufferedWriter writer = new BufferedWriter(write);
    		for (int i : myDoc.summaryId){
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
}
