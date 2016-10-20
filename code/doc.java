package code;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

//some basic information about doc
public class Doc {
  public ArrayList<ArrayList<String>> sen = new ArrayList<ArrayList<String>>();// the sentence after tokenize
  public ArrayList<ArrayList<String>> stemmerSen = new ArrayList<ArrayList<String>>();
  public int[] lRange;//the begin of the i'th document
  public int[] rRange;//the end of the i'th document
  public ArrayList<String> originalSen = new ArrayList<String>();//the original sentence
  public ArrayList<Integer> senLen = new ArrayList<>();//the length of original sentence
  public ArrayList<Integer> wordLen = new ArrayList<>();// the length of the vector
  public ArrayList<TreeSet<Integer>> sVector = new ArrayList<TreeSet<Integer>>();
  public ArrayList<ArrayList<Integer>> sTf = new ArrayList<>();//the tf-vector of the sentence; sVector stores index
  public ArrayList<Integer> dTf;//the tf-vector of the document; dVector stores index
  public TreeSet<Integer> dVector;
  public int totalLen;// the lenth of the
  public int fnum, snum = 0, wnum;//fnum-document num ;wnum-word num; snum-sentence num
  public int[] tf;//tf of words
  public int[] df;//df of words
  public double[] idf;//idf of words
  public double[][] sim, normalSim;
  public int maxlen;//the maxlen of the summary
  // public String outfile;
  ArrayList<Integer> summaryId = new ArrayList<>();//index of the sentence picked
  HashMap<String, Integer> dic = new HashMap<String, Integer>();//map words into number
  HashMap<Integer, String> dd= new HashMap<>();


  public void readTopic(String Topicfile, String language, String stopwordPath) throws IOException {
      Tokenizer mytoken = new Tokenizer();
      ArrayList<String> tmp = new ArrayList<>();
      if (language.equals("1"))//1 represent Chinese
          tmp = mytoken.tokenizeChn(Topicfile, stopwordPath);
      else if (language.equals("2"))//2 represent English
          tmp = mytoken.tokenizeEng(Topicfile, stopwordPath);
      else if (language.equals("3"))//3 represent other
      	tmp = mytoken.tokenizeEng(Topicfile, stopwordPath);

      int len = tmp.size();
      String topic = "";
      ArrayList<String> topicWord = new ArrayList<String>();
      ArrayList<String> stemmerTopicWord = new ArrayList<String>();
      int length = 0;
      for(int i = 0; i < len; ++i) {
      	topic = topic + tmp.get(i) + " ";
      	for(int j = 0; j < mytoken.word.get(i).size(); ++j) {
      		topicWord.add(mytoken.word.get(i).get(j));
      		stemmerTopicWord.add(mytoken.stemmerWord.get(i).get(j));
      	}
      	length += mytoken.senLen.get(i);
      }
      
      sen.add(topicWord);
      stemmerSen.add(stemmerTopicWord);
      senLen.add(length);
      originalSen.add(topic);
      snum++;
  }
  
  //read file from the documents
  public void readfile(String[] rfiles,String filepath,String language, String stopwordPath) throws IOException {
      int i = 0;
      lRange = new int[rfiles.length];
      rRange = new int[rfiles.length];
      fnum = 0;
      totalLen = snum;
      for (String infile : rfiles) {
      	if (infile.equals(".DS_Store")) {
				System.out.println("Skiping!!");
				continue;
			}
      	fnum++;
          String path;
          if (!filepath.equals(" ")) {
              path = filepath + System.getProperty("file.separator") + infile;
          }
          else{
              path = infile;
          }

          Tokenizer mytoken = new Tokenizer();
          ArrayList<String> tmp = new ArrayList<>();
          if (language.equals("1"))//1 represent Chinese
              tmp = mytoken.tokenizeChn(path,stopwordPath);
          else
          if (language.equals("2"))//2 represent English
              tmp = mytoken.tokenizeEng(path, stopwordPath);
			else
			if (language.equals("3"))//3 represent other
				tmp = mytoken.tokenizeEng(path, stopwordPath);
          int len = tmp.size();
          lRange[i] = totalLen;
          totalLen += len;
          rRange[i] = totalLen;
          i++;
          sen.addAll(mytoken.word);
          stemmerSen.addAll(mytoken.stemmerWord);
          senLen.addAll(mytoken.senLen);
          originalSen.addAll(tmp);
      }
      snum = originalSen.size();
  }
  

  // op 1 represent tf-isf; 2 and 3 represent tf-idf
  // stemOrNot 1 represent no stemmer; 2 represent stemmer
  // calculate the tf-idf of the words
  void calcTfidf(int op, int stemOrNot) {
      int i = 0,wlen = 0;
      wnum = 0;
      dic = new HashMap<String, Integer>();
      dTf = new ArrayList<>();
      dVector = new TreeSet<>();
      int[] allTf = new int [100000];
      Arrays.fill(allTf,0);
      wordLen = new ArrayList<>();
      int dnum = 0;
      tf = new int[100000];
      df = new int[100000];
      boolean[] occur = new boolean[100000];
      
      ArrayList<ArrayList<String>> calTfIdfVec = new ArrayList<ArrayList<String>>();
      if(stemOrNot == 1) {
      	calTfIdfVec = sen;
      }else {
      	calTfIdfVec = stemmerSen;
      }
      
      for (ArrayList<String> tmpSen : calTfIdfVec) {
          wlen=0;
          TreeSet<Integer> tmpSet = new TreeSet<Integer>();
          Arrays.fill(tf,0);
          if (op == 2 || op == 3) {
              if (i == rRange[dnum]){
                  dnum++;
                  Arrays.fill(occur,false);
              }
          }else
              Arrays.fill(occur,false);
          for (String tmpWord : tmpSen) {
              wlen++;
              if (dic.get(tmpWord) != null) {
                  int k = dic.get(tmpWord);
                  tmpSet.add(k);
                  tf[k]++;
                  allTf[k]++;
                  if (!occur[k]) {
                      occur[k] = true;
                      df[k]++;
                  }

              } else {
                  dic.put(tmpWord, wnum);
                  dd.put(wnum,tmpWord);
                  tf[wnum]++;
                  allTf[wnum]++;
                  df[wnum]++;
                  tmpSet.add(wnum);
                  occur[wnum] = true;
                  wnum++;
              }
          }
          wordLen.add(wlen);
          ArrayList<Integer> tmpTf=new ArrayList<>();
          for (int j:tmpSet)
          {
              tmpTf.add(tf[j]);
          }
          sTf.add(tmpTf);
          sVector.add(tmpSet);


          i++;
      }
      idf = new double[wnum];
      if (op == 2 || op == 3){
          for (i=0;i<wnum;i++)
          {
              idf[i]= Math.log((double)(1+fnum)/df[i]);
          }
      }else
      {
          for (i=0;i<wnum;i++)
          {
              idf[i]= Math.log((double)(1+snum)/df[i]);
          }
      }
      for (i=0;i<wnum;i++){
          if (allTf[i]!=0)
          {
              dVector.add(i);
              dTf.add(allTf[i]);
          }
      }
  }
  //calculate cosine of two sentence vector
  double calcCos(TreeSet<Integer> a1 , ArrayList<Integer> a2, int lenA , TreeSet<Integer> b1, ArrayList<Integer>b2, int lenB)
  {
      int x1 = 0,x2 = 0;
      double l1 = 0,l2 = 0;
      int idA = 0, idB = 0;
      double cos = 0;
      TreeSet<Integer> a = new TreeSet<>();
      TreeSet<Integer> b = new TreeSet<>();
      a.addAll(a1);
      b.addAll(b1);
      while (a.size() > 0 && b.size() > 0)
      {

          x1 = a.first();
          x2 = b.first();
          if ( x1 == x2 )
          {
              l1 += Math.pow((double)a2.get(idA)/(double)lenA*idf[x1],2);
              l2 += Math.pow((double)b2.get(idB)/(double)lenB*idf[x2],2);
              cos += Math.pow(idf[x1],2)*(double)a2.get(idA)/(double)lenA*(double)b2.get(idB)/(double)lenB;
              a.pollFirst();
              idA++;
              b.pollFirst();
              idB++;
          }else
          if ( x1 < x2 )
          {
              l1 += Math.pow((double)a2.get(idA)/(double)lenA*idf[x1],2);
              a.pollFirst();
              idA++;
          }else
          if ( x1 > x2)
          {
              l2 += Math.pow((double)b2.get(idB)/(double)lenB*idf[x2],2);
              b.pollFirst();
              idB++;
          }
      }
      while (a.size() > 0)
      {
          x1 = a.first();
          l1 += Math.pow((double)a2.get(idA)/(double)lenA*idf[x1],2);
          a.pollFirst();
          idA++;
      }
      while (b.size() > 0)
      {
          x2 = b.first();
          l2 += Math.pow((double)b2.get(idB)/(double)lenB*idf[x2],2);
          b.pollFirst();
          idB++;
      }

      if (l1==0 || l2==0) return 0;
      return cos/Math.pow(l1*l2,0.5) ;
  }

  //calculate the similarity of two sentence
  void calcSim()
  {
      sim = new double[snum][snum];
      normalSim = new double[snum][snum];
      for (int i = 0 ; i < snum; i++){
      	double sumISim = 0.0;
      	for (int j = 0; j < snum; j++)
      	{
      		if (i == j) {
      			sim[i][j] = 1;
      		}
      		else if (i > j) {
      			sim[i][j] = sim[j][i];

      		}
      		else{
      			sim[i][j] = calcCos(sVector.get(i), sTf.get(i), wordLen.get(i), sVector.get(j), sTf.get(j), wordLen.get(j));
      		}
      		sumISim += sim[i][j];
      	}
      	for(int j = 0; j < snum; ++j) {
      		if(sumISim != 0.0) {
      			normalSim[i][j] = sim[i][j] / sumISim;
      		}
      		else 
      			normalSim[i][j] = 0.0;
      	}
      }
  }

  //using MMR to remove redundancy
  ArrayList<Integer> pickSentenceMMR(double[] score, double para, double beta)
  {
      summaryId =new ArrayList<>();
      int len = 0;
      if (para < 0) para = 0.7;
      boolean[] chosen = new boolean[snum];
      for (int i=0;i<snum;i++)
          chosen[i]=false;
      while ( len < maxlen)
      {
          double maxscore = 0;
          int pick = -1;
          for (int i=0;i<snum;i++)
          {
              double tmpscore = score[i];


              for (int j : summaryId)
                  if (score[i] - sim[i][j] * score[j] *para< tmpscore)
                      tmpscore =  score[i] - sim[i][j] * score[j] *para;

              if (tmpscore/Math.pow(senLen.get(i),beta)>maxscore && !chosen[i] && len+ senLen.get(i)<maxlen && senLen.get(i)>=5)
              {

                  maxscore = tmpscore/Math.pow(senLen.get(i),beta);
                  pick = i;

              }
          }
          if (pick==-1)
              break;
          chosen[pick]=true;
          len += senLen.get(pick);
          summaryId.add(pick);
          if (len>=maxlen-20)
              break;
      }
      return summaryId;
  }

  //using threshold to remove redundancy
  ArrayList<Integer> pickSentenceThreshold(double[] score, double threshold, double beta){
      summaryId = new ArrayList<>();
      int len = 0;
      boolean[] chosen = new boolean[snum];
      for (int i = 0; i < snum; i++)
          chosen[i] = false;
      while(len < maxlen)
      {
          double maxscore = 0;
          int pick = -1;
          for (int i = 0; i < snum; i++)
          {
              double tmpscore = score[i];
              for (int j : summaryId)
                  if (sim[i][j] > threshold)
                      tmpscore = 0;

              if (tmpscore/Math.pow(senLen.get(i),beta)>maxscore && !chosen[i] && len+ senLen.get(i)<maxlen && senLen.get(i)>=5)
              {

                  maxscore = tmpscore/Math.pow(senLen.get(i),beta);
                  pick = i;

              }
          }
          if (pick==-1)
              break;
          chosen[pick]=true;
          len += senLen.get(pick);
          summaryId.add(pick);
          if (len>=maxlen-20)
              break;
      }
      return summaryId;
  }

  //using punishment to remove redundancy
  ArrayList<Integer> pickSentenceSumpun(double[] score, double para){
  	summaryId = new ArrayList<>();
  	Map<Integer,Double> m = new TreeMap<Integer,Double>();
		int contentNum = 0;
		int Numm = 0;
		double maxSenScore = -10000.0;
		boolean[] yes = new boolean[snum];
		for(int i = 0; i < snum; i++){
			yes[i] = false;
		}
		while(contentNum <= maxlen){
			
			for(int i = 1; i < snum; i++){
				if(yes[i] == false && score[i] > maxSenScore){
					maxSenScore = score[i];
					Numm = i;
				}
			}
			
			m.put(Numm, maxSenScore);
			maxSenScore = -10000.0;
			contentNum += senLen.get(Numm);
			yes[Numm] = true;
			
			for(int i = 1;i < snum; i++){
				if(yes[i] == false){
					score[i] = score[i] - para * normalSim[i][Numm] * score[Numm];
				}
			}
		} 
		for (Integer key : m.keySet()) { 
			summaryId.add(key);
		}  
		
  	return summaryId;
  	
  }
  
}