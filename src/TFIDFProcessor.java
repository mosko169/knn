import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.BytesRef;

public class TFIDFProcessor {
	
	/**
	 * returns a list of training set documents and their tf-idf vectors
	 * @param ir - index reader to read frequencies from
	 * @param trainingSet - training set documents
	 * @throws Exception
	 */
	public static List<DocumentInstance> getTFIDFVectors(IndexReader ir, List<String[]> trainingSet) throws Exception {
        List<List<Double>> tfidfs = getDocumentTfIDFVectors(ir);
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
	
    /**
     * calculate tf-idf vectors for each document in the index
     * @param ir - index reader to read frequencies from
     * @return a list of tf-idf vectors
     * @throws Exception
     */
    public static List<List<Double>> getDocumentTfIDFVectors(IndexReader ir) throws Exception {
    	List<List<Double>> tfidfs = new ArrayList<List<Double>>();
    	Set<String> termsInIndex = getAllTerms(ir);
    	int numDocs = ir.numDocs();
    	
    	// calculate idf for each term
    	Map<String, Double> idfs = new HashMap<String, Double>();
    	for (String term: termsInIndex) {
    		int termDocFreq = ir.docFreq(new Term(LuceneConstants.CONTENTS, term));
    		idfs.put(term, Math.log10(((double)numDocs) / (double)termDocFreq));
    	}
    	
    	// calculate tf-idf vector for each document
    	for (int docId = 0; docId < ir.numDocs(); ++docId) {
    		List<Double> tfidf = new ArrayList<Double>();
    		Map<String, Integer> docTerms = termsInDocument(ir, docId);
    		// calculate tf(term, doc) for term in index and current document
    		for (String term: termsInIndex) {
    			Integer termFreq = docTerms.get(term);
    			Double tf =  (termFreq == null || termFreq == 0) ? 0.0 : (1 + Math.log10(termFreq));
    			tfidf.add(tf * idfs.get(term));
    		}
    		tfidfs.add(tfidf);
    	}
		return tfidfs;
    }
    
    /**
     * retrieves a map of all terms in a given document to their frequency in the document
     * @param ir - index reader to read frequencies from
     * @param docId - id document to process
     * @throws Exception
     */
    public static Map<String, Integer> termsInDocument(IndexReader ir, int docId) throws Exception {
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
}
