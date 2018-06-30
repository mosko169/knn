import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.BytesRef;


public class Main {
    public static void main(String[] args) {
        try {           

            Config config = new Config(args[0]);
            IndexingEngine ie = new ImprovedIndexingEngine();
            List<String[]> trainDocs = CsvParser.parse(config.trainFile);
            ie.addToIndex(trainDocs);
            
            IndexReader indexReader = DirectoryReader.open(ie.getIndex());
            List<List<Double>> tfidfs = getDocumentTfIDFVectors(indexReader);
            List<DocumentInstance> processedTrainDocuments = new ArrayList<DocumentInstance>();
            int documentIndex = 0;
            for (String[] trainDoc: trainDocs) {
            	processedTrainDocuments.add(new DocumentInstance(Integer.parseInt(trainDoc[0]), trainDoc[1], tfidfs.get(documentIndex)));
            	++documentIndex;
            }
            
            List<String[]> testDocuments = CsvParser.parse(config.testFile);
        	// TODO - compute k nearest neighbors for each test document and all tfidf
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    /**
     * calculate tfidf vectos for each document in the index
     * @param ir
     * @return
     * @throws Exception
     */
    public static List<List<Double>> getDocumentTfIDFVectors(IndexReader ir) throws Exception {
    	List<List<Double>> tfidfs = new ArrayList<List<Double>>();
    	Set<String> termsInIndex = getAllTerms(ir);
    	int numDocs = ir.numDocs();
    	Map<String, Double> idfs = new HashMap<String, Double>();
    	// calculate idf for each term
    	for (String term: termsInIndex) {
    		int ndf = ir.docFreq(new Term(LuceneConstants.CONTENTS, term));
    		idfs.put(term, Math.log10(((double)numDocs) / (double)ndf));
    	}
    	
    	// calculate tfidf vector for each docuemnt
    	for (int docId = 0; docId < ir.numDocs(); ++docId) {
    		List<Double> tfidf = new ArrayList<Double>();
    		Map<String, Integer> docTerms = termsInDocument(ir, docId);
    		// calculate tf(term, doc) for term in index and current document
    		for (String term: termsInIndex) {
    			Integer termFreq = docTerms.get(term);
    			Double tf =  (termFreq == 0 || termFreq == null) ? 0.0 : (1 + Math.log10(termFreq));
    			tfidf.add(tf * idfs.get(term));
    		}
    		tfidfs.add(tfidf);
    	}
		return tfidfs;
    }
    
    /**
     * retrieves all terms in a given document
     * @param ir
     * @param docId
     * @return
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
     * @param ir
     * @return
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
