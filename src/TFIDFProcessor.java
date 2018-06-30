import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.util.BytesRef;


public class TFIDFProcessor {
	public TFIDFProcessor(IndexReader ir) throws Exception {
		_ir = ir;
		calcIDFS();
	}
	
	private void calcIDFS() throws Exception {
		Set<String> termsInIndex = getAllTerms(_ir);
    	int numDocs = _ir.numDocs();
    	
    	// calculate idf for each term
    	_idfs = new HashMap<String, Double>();
    	for (String term: termsInIndex) {
    		int termDocFreq = _ir.docFreq(new Term(LuceneConstants.CONTENTS, term));
    		_idfs.put(term, Math.log10(((double)numDocs) / (double)termDocFreq));
    	}
	}
	
	/**
	 * returns a list of training set documents and their tf-idf vectors
	 * @param ir - index reader to read frequencies from
	 * @param trainingSet - training set documents
	 * @throws Exception
	 */
	public List<DocumentInstance> getTFIDFVectors(List<String[]> trainingSet) throws Exception {
        List<List<Double>> tfidfs = getDocumentTfIDFVectors();
        List<DocumentInstance> processedTrainDocuments = new ArrayList<DocumentInstance>();
        int documentIndex = 0;
        for (String[] trainDoc: trainingSet) {
        	processedTrainDocuments.add(new DocumentInstance(Integer.parseInt(trainDoc[Config.DOC_ID_FIELD])
										        			, trainDoc[Config.LABEL_FIELD]
															, tfidfs.get(documentIndex)));
        	++documentIndex;
        }
        
        return processedTrainDocuments;
	}
	
	public DocumentInstance getTFIDFVectorForTestDoc(String[] testDoc, IndexingEngine index) throws Exception {
		// normalize test document content in the same technique used for training documents
		String testDocContent = index.normalizeString(testDoc[Config.CONTENT_FIELD]);
		QueryParser q = new QueryParser(LuceneConstants.CONTENTS, new StandardAnalyzer());
		String normalizedContent = q.parse(QueryParser.escape(testDocContent)).toString(LuceneConstants.CONTENTS);
	    StringTokenizer st = new StringTokenizer(normalizedContent);
		ArrayList<String> testDocTerms = new ArrayList<String>();        
        while (st.hasMoreTokens()){
        	testDocTerms.add(st.nextToken());
        }
        
        // after normalizing test doc, compute its tf-idf vector against the training set index
        List<Double> testDocTFIDF = new ArrayList<Double>();
	    Iterator<Entry<String, Double>> idfsIter = _idfs.entrySet().iterator();
	    while (idfsIter.hasNext()) {
	        Map.Entry<String, Double> pair = idfsIter.next();
	        Double termTf = tf(Collections.frequency(testDocTerms, pair.getKey()));
	        testDocTFIDF.add(termTf * pair.getValue());
        }
	    
	    return new DocumentInstance(Integer.parseInt(testDoc[Config.DOC_ID_FIELD]), testDoc[Config.LABEL_FIELD], testDocTFIDF);
	}
	
    /**
     * calculate tf-idf vectors for each document in the index
     * @param ir - index reader to read frequencies from
     * @return a list of tf-idf vectors
     * @throws Exception
     */
    public List<List<Double>> getDocumentTfIDFVectors() throws Exception {
    	List<List<Double>> tfidfs = new ArrayList<List<Double>>();

    	
    	// calculate tf-idf vector for each document
    	for (int docId = 0; docId < _ir.numDocs(); ++docId) {
    		List<Double> tfidf = new ArrayList<Double>();
    		Map<String, Integer> docTerms = termsInDocument(_ir, docId);
    		// calculate tf(term, doc) for term in index and current document
    		
    	    Iterator<Entry<String, Double>> idfsIter = _idfs.entrySet().iterator();
    	    while (idfsIter.hasNext()) {
    	        Map.Entry<String, Double> pair = idfsIter.next();
    			Integer termFreq = docTerms.get(pair.getKey());
    			Double termTf = tf(termFreq);
    			tfidf.add(termTf * pair.getValue());
    		}
    		tfidfs.add(tfidf);
    	}
		return tfidfs;
    }
    
    public static Double tf(Integer termFreq) {
    	return (termFreq == null || termFreq == 0) ? 0.0 : (1 + Math.log10(termFreq));
    }
    
    /**
     * retrieves a map of all terms in a given document to their frequency in the document
     * @param ir - index reader to read frequencies from
     * @param docId - id document to process
     * @throws Exception
     */
    private static Map<String, Integer> termsInDocument(IndexReader ir, int docId) throws Exception {
		Terms docTerms = ir.getTermVector(docId, LuceneConstants.CONTENTS);
		TermsEnum termsEnum = docTerms.iterator();
		BytesRef t = termsEnum.next();
    	Map<String, Integer> terms = new HashMap<String, Integer>();
		while (t != null) {
			terms.put(t.utf8ToString(), (int) termsEnum.totalTermFreq());
			t = termsEnum.next();
		}
		return terms;
    }
    
    /**
     * retrieves all terms in the corpus
     * @param ir - index reader to read frequencies from
     * @throws Exception
     */
    public static Set<String> getAllTerms(IndexReader ir) throws Exception {
    	Set<String> terms = new HashSet<String>();
    	for (int docId = 0; docId < ir.numDocs(); ++docId) {
    		terms.addAll(termsInDocument(ir, docId).keySet());
    	}
    	return terms;
    }
    
    private IndexReader _ir;
    private Map<String, Double> _idfs;
}
