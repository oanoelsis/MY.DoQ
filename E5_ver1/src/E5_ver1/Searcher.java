package E5_ver1;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.apache.lucene.analysis.kr.KoreanAnalyzer;

//import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.lucene.document.DateTools;

public class Searcher {

	IndexSearcher indexSearcher;
	QueryParser queryParser;
	MultiFieldQueryParser multifieldqueryparser;
	Query query;
	
	@SuppressWarnings("deprecation")
	public Searcher(String indexDirectoryPath) throws IOException{
		Directory indexDirectory = FSDirectory.open(new File(indexDirectoryPath));
		indexSearcher = new IndexSearcher(indexDirectory);
		queryParser = new QueryParser(Version.LUCENE_36, "contents", new KoreanAnalyzer());
		multifieldqueryparser = new MultiFieldQueryParser(Version.LUCENE_36, new String[]{"contents", "name"}, new KoreanAnalyzer());
	}
	
	// search for contents field(20151121, contents & name multifield query parser using)
	public TopDocs search(String searchQuery) throws IOException, ParseException{
		//query = queryParser.parse(searchQuery);
		query = multifieldqueryparser.parse(searchQuery);
		return indexSearcher.search(query, LuceneConstants.MAX_SEARCH);
	}
	
	public TopDocs content_search(String searchQuery) throws ParseException, IOException{
		query = queryParser.parse(searchQuery);
		return indexSearcher.search(query, LuceneConstants.MAX_SEARCH);
	}
	
	// search for now lastmodifed field
	public TopDocs LastModfied_search() throws IOException{
		long min = Long.parseLong("201510220000");
		Date now = new Date();
		String current_time = DateTools.dateToString(now, DateTools.Resolution.MINUTE);
		NumericRangeQuery<Long> query = NumericRangeQuery.newLongRange("lastmodified", min, Long.parseLong(current_time), true, true);
		return indexSearcher.search(query, LuceneConstants.MAX_SEARCH);
	}
	
	// search for main recent lastmodified field
	/*
	public TopDocs Main_Recent_search(long query){
		long min = Long
	}
	*/
	// search for author field
	
	// search for name field
	
	// search for moreLikeThis function
	
	public Document getDocument(ScoreDoc scoreDoc) throws CorruptIndexException, IOException{
		return indexSearcher.doc(scoreDoc.doc);
	}
	
	public void close() throws IOException{
		indexSearcher.close();
	}
}
