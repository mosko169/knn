import java.util.List;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;



public class Main {
    public static void main(String[] args) {
        try {           

            Config config = new Config(args[0]);
            IndexingEngine ie = new ImprovedIndexingEngine();
            List<String[]> trainDocs = CsvParser.parse(config.trainFile);
            ie.addToIndex(trainDocs);
            
            IndexReader indexReader = DirectoryReader.open(ie.getIndex());
            List<DocumentInstance> tfidfs = TFIDFProcessor.getTFIDFVectors(indexReader, trainDocs);
            
            List<String[]> testDocuments = CsvParser.parse(config.testFile);
        	// TODO - compute k nearest neighbors for each test document and all tfidf
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    

}
