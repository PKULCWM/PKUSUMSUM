package code;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;


//some basic information about doc
public class doc {
    public ArrayList<ArrayList<String>> sen = new ArrayList<ArrayList<String>>();
    public ArrayList<ArrayList<String>> stemmerSen = new ArrayList<ArrayList<String>>();
    public int[] l_range;//the begin of the i'th document
    public int[] r_range;//the end of the i'th document
    public ArrayList<String> original_sen = new ArrayList<String>();//the original sentence
    public ArrayList<Integer> sen_len = new ArrayList<>();//the length of original sentence
    public ArrayList<Integer> word_len = new ArrayList<>();// the length of the vector
    public ArrayList<TreeSet<Integer>> vector = new ArrayList<TreeSet<Integer>>();
    public ArrayList<ArrayList<Integer>> s_tf = new ArrayList<>();// the tf-vector of the sentence
    public ArrayList<Integer> d_tf;
    public TreeSet<Integer> d_vector;
    public int total_len;
    public int fnum, snum = 0, wnum;//fnum-document num ;wnum-word num; snum-sentence num
    public int[] tf;
    public int[] df;
    public double[] idf;
    public double[][] sim, normalSim;
    public int maxlen;//the maxlen of the summary
    // public String outfile;
    ArrayList<Integer> summary_id = new ArrayList<>();
    HashMap<String, Integer> dic = new HashMap<String, Integer>();
    HashMap<Integer, String> dd= new HashMap<>();


    public void readTopic(String Topicfile, String language, String stopwordPath) throws IOException {
        Tokenizer mytoken = new Tokenizer();
        ArrayList<String> tmp = new ArrayList<>();
        if (language.equals("1"))//1 represent Chinese
            tmp = mytoken.tokenize_Chn(Topicfile, stopwordPath);
        else if (language.equals("2"))//2 represent English
            tmp = mytoken.tokenize_Eng(Topicfile, stopwordPath);
        else if (language.equals("3"))
        	tmp = mytoken.tokenize_Eng(Topicfile, stopwordPath);

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
        sen_len.add(length);
        original_sen.add(topic);
        snum++;
    }
    
    //read file from the documents
    public void readfile(String[] rfiles,String filepath,String language, String stopwordPath) throws IOException {
        int i = 0;
        l_range = new int[rfiles.length];
        r_range = new int[rfiles.length];
        fnum = 0;
        total_len = snum;
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
                tmp = mytoken.tokenize_Chn(path,stopwordPath);
            else
            if (language.equals("2"))//2 represent English
                tmp = mytoken.tokenize_Eng(path, stopwordPath);
			else
			if (language.equals("3"))
				tmp = mytoken.tokenize_Eng(path, stopwordPath);
            int len = tmp.size();
            l_range[i] = total_len;
            total_len += len;
            r_range[i] = total_len;
            i++;
            sen.addAll(mytoken.word);
            stemmerSen.addAll(mytoken.stemmerWord);
            sen_len.addAll(mytoken.senLen);
            original_sen.addAll(tmp);
        }
        snum = original_sen.size();
    }
    

    // op 1 represent tf-isf; 2 and 3 represent tf-idf
    // stemOrNot 1 represent no stemmer; 2 represent stemmer
    void calc_tfidf(int op, int stemOrNot) {
        int i = 0,wlen = 0;
        wnum = 0;
        dic = new HashMap<String, Integer>();
        d_tf = new ArrayList<>();
        d_vector = new TreeSet<>();
        int[] all_tf = new int [100000];
        Arrays.fill(all_tf,0);
        word_len = new ArrayList<>();
        int dnum = 0;
        tf = new int[100000];
        df = new int[100000];
        boolean[] occur = new boolean[10000];
        
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
                if (i == r_range[dnum]){
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
                    all_tf[k]++;
                    if (!occur[k]) {
                        occur[k] = true;
                        df[k]++;
                    }

                } else {
                    dic.put(tmpWord, wnum);
                    dd.put(wnum,tmpWord);
                    tf[wnum]++;
                    all_tf[wnum]++;
                    df[wnum]++;
                    tmpSet.add(wnum);
                    occur[wnum] = true;
                    wnum++;
                }
            }
            word_len.add(wlen);
            ArrayList<Integer> tmp_tf=new ArrayList<>();
            for (int j:tmpSet)
            {
                tmp_tf.add(tf[j]);
            }
            s_tf.add(tmp_tf);
            vector.add(tmpSet);


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
            if (all_tf[i]!=0)
            {
                d_vector.add(i);
                d_tf.add(all_tf[i]);
            }
        }
    }

    double calc_cos(TreeSet<Integer> a1 ,ArrayList<Integer> a2,int len_a ,TreeSet<Integer> b1, ArrayList<Integer>b2, int len_b)
    {
        int x1 = 0,x2 = 0;
        double l1 = 0,l2 = 0;
        int id_a = 0, id_b = 0;
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
                l1 += Math.pow((double)a2.get(id_a)/(double)len_a*idf[x1],2);
                l2 += Math.pow((double)b2.get(id_b)/(double)len_b*idf[x2],2);
                cos += Math.pow(idf[x1],2)*(double)a2.get(id_a)/(double)len_a*(double)b2.get(id_b)/(double)len_b;
                a.pollFirst();
                id_a++;
                b.pollFirst();
                id_b++;
            }else
            if ( x1 < x2 )
            {
                l1 += Math.pow((double)a2.get(id_a)/(double)len_a*idf[x1],2);
                a.pollFirst();
                id_a++;
            }else
            if ( x1 > x2)
            {
                l2 += Math.pow((double)b2.get(id_b)/(double)len_b*idf[x2],2);
                b.pollFirst();
                id_b++;
            }
        }
        while (a.size() > 0)
        {
            x1 = a.first();
            l1 += Math.pow((double)a2.get(id_a)/(double)len_a*idf[x1],2);
            a.pollFirst();
            id_a++;
        }
        while (b.size() > 0)
        {
            x2 = b.first();
            l2 += Math.pow((double)b2.get(id_b)/(double)len_b*idf[x2],2);
            b.pollFirst();
            id_b++;
        }

        if (l1==0 || l2==0) return 0;
        return cos/Math.pow(l1*l2,0.5) ;
    }

    void calc_sim()
    {
        sim = new double[snum][snum];
        normalSim = new double[snum][snum];
        for (int i = 0 ; i < snum; i++){
        	double sumISim = 0.0;
        	for (int j = 0; j < snum; j++)
        	{
        		if (i == j) {
        			sim[i][j] = 1;
        			/*sumISim += sim[i][j];
        			continue;*/
        		}
        		else if (i > j) {
        			sim[i][j] = sim[j][i];
        			/*sumISim += sim[i][j];
        			continue;*/
        		}
        		else{
        			sim[i][j] = calc_cos(vector.get(i), s_tf.get(i),word_len.get(i),vector.get(j), s_tf.get(j),word_len.get(j));
        		}
        		sumISim += sim[i][j];
        	}
        	for(int j = 0; j < snum; ++j) {
        		if(sumISim != 0.0) {
        			//System.out.println(sumISim);
        			normalSim[i][j] = sim[i][j] / sumISim;
        		}
        		else 
        			normalSim[i][j] = 0.0;
        	}
        }
    }

    ArrayList<Integer> pick_sentence_MMR(double[] score,double para,double beta)
    {
        summary_id=new ArrayList<>();
        int len = 0;
        if (para<0) para=0.7;
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


                for (int j : summary_id)
                    if (score[i] - sim[i][j] * score[j] *para< tmpscore)
                        tmpscore =  score[i] - sim[i][j] * score[j] *para;

                if (tmpscore/Math.pow(sen_len.get(i),beta)>maxscore && !chosen[i] && len+sen_len.get(i)<maxlen && sen_len.get(i)>=5)
                {

                    maxscore = tmpscore/Math.pow(sen_len.get(i),beta);
                    pick = i;

                }
            }
            if (pick==-1)
                break;
            chosen[pick]=true;
            len += sen_len.get(pick);
            summary_id.add(pick);
            if (len>=maxlen-20)
                break;
        }
        return summary_id;
    }

    ArrayList<Integer> pick_sentence_threshold(double[] score,double threshold,double beta){
        summary_id = new ArrayList<>();
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
                for (int j : summary_id)
                    if (sim[i][j] > threshold)
                        tmpscore = 0;

                if (tmpscore/Math.pow(sen_len.get(i),beta)>maxscore && !chosen[i] && len+sen_len.get(i)<maxlen && sen_len.get(i)>=5)
                {

                    maxscore = tmpscore/Math.pow(sen_len.get(i),beta);
                    pick = i;

                }
            }
            if (pick==-1)
                break;
            chosen[pick]=true;
            len += sen_len.get(pick);
            summary_id.add(pick);
            if (len>=maxlen-20)
                break;
        }
        return summary_id;
    }

    ArrayList<Integer> pick_sentence_sumPun(double[] score,double para){
    	summary_id = new ArrayList<>();
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
			contentNum += sen_len.get(Numm);
			yes[Numm] = true;
			
			for(int i = 1;i < snum; i++){
				if(yes[i] == false){
					score[i] = score[i] - para * normalSim[i][Numm] * score[Numm];
				}
			}
		} 
		for (Integer key : m.keySet()) { 
			summary_id.add(key);
		}  
		
    	return summary_id;
    	
    }
    
}