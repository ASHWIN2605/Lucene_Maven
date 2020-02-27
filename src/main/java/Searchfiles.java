import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

// Lucene scoring
import org.apache.lucene.search.similarities.BM25Similarity;

public class Searchfiles {


        /** Simple command-line based search demo. */
        public void Searchindex(String queriesPath) throws IOException, ParseException {
            String usage = "Usage:\tjava org.apache.lucene.demo.SearchFiles [-index dir] [-field f] [-repeat n] [-queries file] [-query string] [-raw] [-paging hitsPerPage]\n\nSee http://lucene.apache.org/core/4_1_0/demo/ for details.";
//            if (args.length > 0 && ("-h".equals(args[0]) || "-help".equals(args[0]))) {
//                System.out.println(usage);
//                System.exit(0);
//            }

            String index = "index";
            String queryString = null;


            IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
            IndexSearcher searcher = new IndexSearcher(reader);
            String results_path = "cran-results.txt";
            PrintWriter writer = new PrintWriter(results_path, "UTF-8");
            Analyzer analyzer = new EnglishAnalyzer();


            //BM25
            searcher.setSimilarity(new BM25Similarity());

            //---------------- Read in and parse queries ----------------

            //String queriesPath = "cran\\cran.qry";
            BufferedReader buffer = Files.newBufferedReader(Paths.get(queriesPath), StandardCharsets.UTF_8);
            //Multifield query parser used so as to query in multiple fields including heading,answers etc
            MultiFieldQueryParser parser = new MultiFieldQueryParser(new String[] {"heading","author","reference","answer"}, analyzer);

            Integer queryIterator = 1;
            String line_content;
            Boolean flag = true;

            System.out.println("Reading queries and creating search results.");

            while ((line_content = buffer.readLine()) != null){

                if(line_content.substring(0,2).equals(".I")){
                    if(!flag){
                        Query query = parser.parse(QueryParser.escape(queryString));
                        searchCranfield(searcher,writer,queryIterator,query);
                        queryIterator++;
                    }
                    else{ flag=false; }
                    queryString = "";
                } else {
                    queryString += " " + line_content;
                }
            }

            Query query = parser.parse(QueryParser.escape(queryString));
            searchCranfield(searcher,writer,queryIterator,query);

            writer.close();
            reader.close();
        }


    // Performs search and writes results to the writer
    public static void searchCranfield(IndexSearcher searcher, PrintWriter writer, Integer queryIterator, Query query) throws IOException {

        TopDocs results = searcher.search(query, 50);
        ScoreDoc[] hits = results.scoreDocs;

        // Write the results for each hit
        for(int i=0;i<hits.length;i++){
            Document doc = searcher.doc(hits[i].doc);
            /*
             * Write the results in the format expected by trec_eval:*/
            writer.println(queryIterator + " 0 " + doc.get("s_no") + " " + i + " " + hits[i].score + " EXP");
        }
    }
}
