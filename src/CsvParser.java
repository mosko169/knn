
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CsvParser {

	/**
	 * parses a CSV file
	 * @param filePath - path to file to parse
	 * @return a list of all rows in the csv file
	 * @throws IOException
	 */
    public static List<String[]> parse(String filePath) throws IOException {

        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";
        ArrayList<String[]> rows = new ArrayList<String[]>();
        try {
            br = new BufferedReader(new FileReader(filePath));
            while ((line = br.readLine()) != null) {
                rows.add(line.split(cvsSplitBy));
            }
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return rows;
    }

}