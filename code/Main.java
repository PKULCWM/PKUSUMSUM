package code;

import java.io.File;

public class Main {
	
	/**
	 * ============ Necessary Parameters ============
	 * type: Specify which task to do.
	 * 		1: single-document summarization, 2: multi-document summarization,
	 * 		3: topic-based multi-document summarization
	 * topicFile: The path of the topic file. (Only for topic-based multi-document summarization task)
	 * inputPath: The path of the input.
	 * 		Single-document summarization task: The path of the input file and this file can only contain one document you want to get the summary from.
	 * 		Multi-document summarization task or topic-based multi-document summarization task: The path of the input directory and this directory can only contain one document set you want to get the summary from.
	 * outputFile: The path of the output file and one file only contains one summary.
	 * language: The language of the document. 1: Chinese, 2: English, 3: other Western languages
	 * abNum: The expected number of words in summary.
	 * method: Specify which method to solve the problem.
	 * 		Single-document summarization task: 1: Lead, 2: Centroid, 3: ILP, 4: LexPageRank, 5: TextRank, 6: Submodular;
	 * 		Multi-document summarization task: 0: Coverage, 1: Lead, 2: Centroid, 3: ILP, 4: LexPageRank, 5: TextRank, 6: Submodular, 7: ClusterCMRW;
	 * 		Topic-based multi-document summarization task: 0: Coverage, 1: Lead, 2: Centroid, 8: ManifoldRank.
	 * stopwordPath: Choose whether you need remove the stop words.
	 * 		If you need remove the stop words, you should input the path of stop word list. 
	 * 		Or we have prepared an English stop words list as file ¡°stopword_Eng¡±, you can use it by input ¡°y¡±.
	 * 		If you don¡¯t need remove the stop words, please input ¡°n¡±.
	 * 
	 * ============ Optional Parameters ============
	 * [stemmerOrNot]: Choose if you want to stem the input. (Only for English document) 
	 * 		1: stem, 2: no stem, default = 1
	 * [ReMethod]: Specify which redundancy removal method to use. ILP and Submodular needn't extra redundancy removal. default = 3 for ManifoldRank, default = 1 for the other methods which need redundancy removal
	 *		1: MMR
	 *		2: threshold: If the similarity between an unchosen sentence and the sentence chosen this time is upper than the threshold, this unchosen sentence will be deleted from candidate set.
	 *		3: sum punishment: The scores of all unchosen sentences will decrease the product of penalty ratio and the similarity with the sentence chosen this time.
	 * [RePara]: The parameter of redundancy removal methods. default = 0.7
	 *		For MMR and sum punishment: it represents the penalty ratio. 
	 *		For threshold: it represents the threshold.
	 * [beta]: [0, 1] A scaling factor of sentence length when we choose sentences. default = 0.1
	 *  [-] LexPageRank-specific parameters
	 * [linkThresh]: [0, 1] If the similarity of two sentence is greater than this parameter they can link. default = 0.1
	 *	[-] ClusterCMRW-specific parameters
	 * [AlphaC]: [0, 1] The ratio controlling the expected cluster number for the document set. default = 0.1
	 * [LambdaC]: [0, 1] The combination weight controlling the relative contributions from the source cluster and the destination cluster. default = 0.8
	 *	[-] Submodular-specific parameters
	 * [op]: Type of submodular method. 
	 *		1: Li's paper (Li at el, 2012)
	 *		2: modification method from Lin's paper (Lin and Bilmes, 2010) default = 2
	 * [AlphaS]: [0, 1] Threshold coefficient. default = 0.5
	 * [LambdaS]: [0, 1] Trade-off coefficient. 
	 *		default = 0.15 in multi-document task and default = 0.5 in single-document task
	 * 
	 **/

	
	public static void main(String args[])throws Exception{
		/* Params and default value, you can modify them */
		String language = "1", type = "1", abNum = "120", stopwordPath = "n", stemmerOrNot = "2";
		String topic = "-1", ReMethod = "1", RePara = "0.7", beta = "0"/*, alpha = "0.85", eps = "0.00001"*/;
		String linkThresh = "0.1", AlphaC = "0.1", LambdaC = "0.8", op = "2", AlphaS = "0.5", LambdaS = "-1";
		String[] arg;
		
		String file = "D:\\research\\PKUSUMSUM\\data\\dataChn";
		String outFile = "D:\\research\\PKUSUMSUM\\data\\dataChn-ILP";
		
		File dir = new File(file);
		File[] files = dir.listFiles();
			
		File outDir = new File(outFile);
		if(!outDir.exists()){
			boolean file_true = outDir.mkdir(); 
			if (!file_true) {
				System.out.println("No valid dir!");
			}
		}
		/**
		 *  arg[0] = inputPath;
			arg[1] = outputFile;
			arg[2] = language;
			arg[3] = type;
			arg[4] = abNum;
			arg[5] = stemmerOrNot;
			arg[6] = stopwordPath;
		 * */
		for(File fOrd : files) {
			if (fOrd.getName().equals(".DS_Store")) {
				continue;
			}
			ILP ilp = new ILP();
			arg = new String[7];
			arg[0] = file + System.getProperty("file.separator") + fOrd.getName();
			arg[1] = outFile + System.getProperty("file.separator") + fOrd.getName();
			arg[2] = language;
			arg[3] = type;
			arg[4] = abNum;
			arg[5] = stemmerOrNot;
			arg[6] = stopwordPath;
 			ilp.Summarize(arg);
			
			/*Lead lead = new Lead();
			arg = new String[7];
			arg[0] = file + System.getProperty("file.separator") + fOrd.getName();
			arg[1] = outFile + System.getProperty("file.separator") + fOrd.getName();
			arg[2] = language;
			arg[3] = type;
			arg[4] = abNum;
			arg[5] = stemmerOrNot;
			arg[6] = stopwordPath;
 			lead.Summarize(arg);*/
			
			/*Coverage coverage = new Coverage();
			arg = new String[7];
			arg[0] = file + System.getProperty("file.separator") + fOrd.getName();
			arg[1] = outFile + System.getProperty("file.separator") + fOrd.getName();
			arg[2] = language;
			arg[3] = type;
			arg[4] = abNum;
			arg[5] = stemmerOrNot;
			arg[6] = stopwordPath;
			coverage.Summarize(arg);*/
			
			/*LexPageRank lexpagerank = new LexPageRank();
			arg = new String[13];
			arg[0] = file + System.getProperty("file.separator") + fOrd.getName();
			arg[1] = outFile + System.getProperty("file.separator") + fOrd.getName();
			arg[2] = language;
			arg[3] = type;
			arg[4] = abNum;
			arg[5] = stemmerOrNot;
			arg[6] = stopwordPath;
			arg[7] = ReMethod;
			arg[8] = RePara;
			arg[9] = beta;
			arg[8] = alpha;
			arg[9] = eps;
			arg[12] = linkThresh;
 			lexpagerank.Summarize(arg);*/
			
 			/*TextRank textrank = new TextRank();
 			arg = new String[10];
 			arg[0] = file + System.getProperty("file.separator") + fOrd.getName();
			arg[1] = outFile + System.getProperty("file.separator") + fOrd.getName();
			arg[2] = language;
			arg[3] = type;
			arg[4] = abNum;
			arg[5] = stemmerOrNot;
			arg[6] = stopwordPath;
			arg[7] = ReMethod;
			arg[8] = RePara;
			arg[9] = beta;
			arg[8] = alpha;
			arg[9] = eps;
			textrank.Summarize(arg);*/
			
			/*MEAD mead = new MEAD();
 			arg = new String[11];
 			arg[0] = file + System.getProperty("file.separator") + fOrd.getName();
			arg[1] = outFile + System.getProperty("file.separator") + fOrd.getName();
			arg[2] = language;
			arg[3] = type;
			arg[4] = abNum;
			arg[5] = stemmerOrNot;
			arg[6] = stopwordPath;
			arg[7] = ReMethod;
			arg[8] = RePara;
			arg[9] = beta;
			arg[10] = "D:\\research\\PKUSUMSUM\\PKUSUMSUM\\Data\\topicAll\\topic\\" + fOrd.getName() + ".topic";
			mead.Summarize(arg);*/
			
			/*ManifoldRank manifoldRank = new ManifoldRank();
			arg = new String[11];
			arg[0] = file + System.getProperty("file.separator") + fOrd.getName();
			arg[1] = outFile + System.getProperty("file.separator") + fOrd.getName();
			arg[2] = language;
			arg[3] = single;
			arg[4] = abNum;
			arg[5] = ReMethod;
			arg[6] = RePara;
			arg[7] = beta;
			arg[8] = alpha;
			arg[9] = eps;
			arg[10] = "D:\\research\\PKUSUMSUM\\PKUSUMSUM\\Data\\topicAll\\topic\\" + fOrd.getName() + ".topic";
 			manifoldRank.Summarize(arg);*/
 			
 			/*Submodular submodular = new Submodular();
			arg = new String[11];
			arg[0] = file + System.getProperty("file.separator") + fOrd.getName();
			arg[1] = outFile + System.getProperty("file.separator") + fOrd.getName();
			arg[2] = language;
			arg[3] = type;
			arg[4] = abNum;
			arg[5] = stemmerOrNot;
			arg[6] = stopwordPath;
			arg[7] = op;
			arg[8] = beta;
			arg[9] = AlphaS;
			arg[10] = LambdaS;
			submodular.Summarize(arg);*/
			
			/*ClusterCMRW clusterCMRW = new ClusterCMRW();
			arg = new String[12];
			arg[0] = file + System.getProperty("file.separator") + fOrd.getName();
			arg[1] = outFile + System.getProperty("file.separator") + fOrd.getName();
			arg[2] = language;
			arg[3] = type;
			arg[4] = abNum;
			arg[5] = stemmerOrNot;
			arg[6] = stopwordPath;
			arg[7] = ReMethod;
			arg[8] = RePara;
			arg[9] = beta;
			arg[10] = AlphaC;
			arg[11] = LambdaC;
			clusterCMRW.Summarize(arg);*/
			
		}
		
	}
}
