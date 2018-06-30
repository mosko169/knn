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
            TFIDFProcessor tf_idfProcessor = new TFIDFProcessor(indexReader);
            List<DocumentInstance> tfidfs = tf_idfProcessor.getTFIDFVectors(trainDocs);
            KNNClassifier classifier = new KNNClassifier(tfidfs);
            List<String[]> testDocuments = CsvParser.parse(config.testFile);
            for (String[] testDocument: testDocuments) {
                DocumentInstance testDoc = tf_idfProcessor.getTFIDFVectorForTestDoc(testDocument, ie);
                classifier.classify(testDoc);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    

}
