import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;

public class compareAlgorithms {
	static  String indexPath="C:\\Users\\Manasa\\Documents\\Assignments and Work\\Fall 2014\\Info Retrieval\\assignment 2\\default";
	static  String topicsPath="C:\\Users\\Manasa\\Documents\\Assignments and Work\\Fall 2014\\Info Retrieval\\assignment 2\\topics.51-100";
	public static void main(String[] args) throws IOException, ParseException {
		int id=51;
		File file = new File(topicsPath);
		String readfile=	FileUtils.readFileToString(file);
		String titles[]=StringUtils.substringsBetween(readfile, "<title>","<desc>");
		appendFile("C:\\Users\\Manasa\\Desktop\\DefaultSimilarityshortQuery.txt");
		appendFile("C:\\Users\\Manasa\\Desktop\\BM25shortQuery.txt");
		appendFile("C:\\Users\\Manasa\\Desktop\\LMDirichletshortQuery.txt");
		appendFile("C:\\Users\\Manasa\\Desktop\\LMJelinekMercershortQuery.txt");

		for(String title:titles){
		title=StringUtils.chop(title);
		title=StringUtils.lowerCase(title);
		title=title.trim().replaceAll("[\\t\\n\\r]"," ").replaceAll("-", " ").replaceAll("topic:", "").replaceAll("[^a-zA-Z 0-9]","").trim();
		ReqTopDocs(id,title,new DefaultSimilarity(),"C:\\Users\\Manasa\\Desktop\\DefaultSimilarityshortQuery.txt");
		ReqTopDocs(id,title,new BM25Similarity(),"C:\\Users\\Manasa\\Desktop\\BM25shortQuery.txt");
		ReqTopDocs(id,title,new LMDirichletSimilarity(),"C:\\Users\\Manasa\\Desktop\\LMDirichletshortQuery.txt");
		ReqTopDocs(id,title,new LMJelinekMercerSimilarity((float) 0.7),"C:\\Users\\Manasa\\Desktop\\LMJelinekMercershortQuery.txt");
		id++;
		}
		id=51;
		appendFile("C:\\Users\\Manasa\\Desktop\\DefaultSimilaritylongQuery.txt");
		appendFile("C:\\Users\\Manasa\\Desktop\\BM25longQuery.txt");
		appendFile("C:\\Users\\Manasa\\Desktop\\LMDirichletlongQuery.txt");
		appendFile("C:\\Users\\Manasa\\Desktop\\LMJelinekMercerlongQuery.txt");
		String descs[]=StringUtils.substringsBetween(readfile, "<desc>","<smry>") ;
		for(String desc:descs){
			desc=StringUtils.chop(desc);
			desc=StringUtils.lowerCase(desc);
			desc=desc.trim().replaceAll("[\\t\\n\\r]"," ").replaceAll("-"," ").replaceAll("description:","").replaceAll("[^a-zA-Z 0-9]","").trim();
			ReqTopDocs(id,desc,new DefaultSimilarity(),"C:\\Users\\Manasa\\Desktop\\DefaultSimilaritylongQuery.txt");
			ReqTopDocs(id,desc,new BM25Similarity(),"C:\\Users\\Manasa\\Desktop\\BM25longQuery.txt");
			ReqTopDocs(id,desc,new LMDirichletSimilarity(),"C:\\Users\\Manasa\\Desktop\\LMDirichletlongQuery.txt");
			ReqTopDocs(id,desc,new LMJelinekMercerSimilarity((float) 0.7),"C:\\Users\\Manasa\\Desktop\\LMJelinekMercerlongQuery.txt");
			id++;
		}
	}
	public static void ReqTopDocs(int id,String querystring ,Similarity sim,String file) throws ParseException, IOException{
		IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexPath)));
		IndexSearcher search = new IndexSearcher(reader);
		search.setSimilarity(sim); 
		Analyzer analyzer = new StandardAnalyzer();
		QueryParser parser = new QueryParser("TEXT", analyzer);
		Query query = parser.parse(querystring);
		TopScoreDocCollector collect = TopScoreDocCollector.create(1000, true);
		search.search(query, collect);
		ScoreDoc[] docs = collect.topDocs().scoreDocs;
		int count=1;
		boolean status = false;
		String result="";
		for (int i = 0; i < docs.length; i++) {
				Document doc = search.doc(docs[i].doc);
				result = id + "\t" +"Q0"+ "\t" + doc.get("DOCNO") + "\t"+ count + "\t"+ docs[i].score +"\t"+ "run_1"+"\n";
		
			try{
				FileWriter filewriter = new FileWriter(file,true);
				BufferedWriter bufferwriter = new BufferedWriter(filewriter);
		        bufferwriter.write(result);
		        bufferwriter.newLine();
		        bufferwriter.flush();
		        bufferwriter.close();
		        filewriter.close();      
		        status = true;
			}
		catch(IOException e){
   		e.printStackTrace();
		}	
				
		        if(!status){
					System.out.println("\n Results could not be written to file. ");
					System.exit(1);
				}
				
				count++;
		}
		
		reader.close();
	}
	public static boolean appendFile(String path){
		boolean status=false;
		try{
			File f = new File(path);
			if(f.exists()){
				f.delete();
			}
			status = f.createNewFile();
		}
		catch(Exception e){
			 e.printStackTrace();
		}
		
		return status;
	}

}