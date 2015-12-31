package E5_ver1;

import java.io.*;
import java.util.Arrays;

//import java.util.*;

import org.apache.lucene.analysis.kr.KoreanAnalyzer;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.document.DateTools;
//import org.apache.lucene.analysis.Analyzer;
//import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericField;
import org.apache.lucene.index.CorruptIndexException;
//import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
//import org.apache.lucene.util.Version;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;



public class Indexer {
	private IndexWriter writer;
	
	@SuppressWarnings("deprecation")
	public Indexer(String indexDirectoryPath) throws IOException{
		//this directory will contain the indexes
		Directory indexDirectory = FSDirectory.open(new File(indexDirectoryPath));
		
		//create indexer that use korean_analyzer
		writer = new IndexWriter(indexDirectory, new KoreanAnalyzer(), true, IndexWriter.MaxFieldLength.UNLIMITED);
		
	}
	
	public void close() throws CorruptIndexException, IOException{
		writer.close();
	}
	


	private Document getDocument(File file) throws IOException, SAXException, TikaException{
		
		// Using Tika for extract plain text from document
		FileInputStream is = new FileInputStream(file);
		// -1 �� document text ������ unlimited�� ���� �ǹ�
		ContentHandler contenthandler = new BodyContentHandler(-1);
		Metadata metadata = new Metadata();
		metadata.set(Metadata.RESOURCE_NAME_KEY, file.getName());
		Parser parser = new AutoDetectParser();
		ParseContext pcontext = new ParseContext();
		parser.parse(is, contenthandler, metadata, pcontext);
		
		//is.close();
		
		//for debugging Tika metadata class
		
		String[] metadataNames = metadata.names();
		/*
		for(String name : metadataNames){
			System.out.println(name + " : " + metadata.get(name));
		}
		*/
		// end Tika
		
		Document document = new Document();
		document.add(new Field("name", file.getName(), Field.Store.YES, Field.Index.ANALYZED));
		document.add(new Field("path", file.getCanonicalPath(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		// last-modified�� ���� ���� ��¥�� ���� ��.
		//NumericField lastmodified = new NumericField("lastmodified");
		document.add(new NumericField("lastmodified", 8).setLongValue(Long.parseLong(DateTools.timeToString(file.lastModified(), DateTools.Resolution.MINUTE))));
		//document.add(new NumericField("modified", Integer.parseInt(DateTools.timeToString(file.lastModified(), DateTools.Resolution.MINUTE)), Field.Store.YES, Field.Index.NOT_ANALYZED));
		
		
		// �ش�Ǵ� metadata������ ���� ���ϵ��� ���� ���־�� ��.
		if (Arrays.asList(metadataNames).contains("title")){
			document.add(new Field("title",metadata.get("title"),Field.Store.YES, Field.Index.ANALYZED));
		}
		if (Arrays.asList(metadataNames).contains("author")){
			document.add(new Field("author",metadata.get("Author"),Field.Store.YES, Field.Index.NOT_ANALYZED));
		}
	    
	    document.add(new Field("contents",contenthandler.toString(),Field.Store.NO,Field.Index.ANALYZED));
		return document;
	}
	
	private void indexFile(File file) throws IOException, SAXException, TikaException{
		System.out.println("Indexing "+file.getCanonicalPath());
		Document document = getDocument(file);
		writer.addDocument(document);
	}
	
	  public int createIndex(String dataDirPath, FileFilter filter) 
		      throws IOException, SAXException, TikaException{
		      //get all files in the data directory
		      File[] files = new File(dataDirPath).listFiles();

		      for (File file : files) {
		         if(!file.isDirectory()
		            && !file.isHidden()
		            && file.exists()
		            && file.canRead()
		            && filter.accept(file)
		         ){
		            indexFile(file);
		         }
		      }
		      return writer.numDocs();
		   }
}
