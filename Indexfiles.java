import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
/** Index all text files under a directory.
 048 * <p>
 049 * This is a command-line application demonstrating simple Lucene indexing.
 050 * Run it with no command-line arguments for usage information.
 051 */
public class Indexfiles {
    //private IndexFiles() {
    //}

    /**
     * Index all text files under a directory.
     */
    public static void main(String[] args) {
        String usage = "java org.apache.lucene.demo.IndexFiles"
                + " [-index INDEX_PATH] [-docs DOCS_PATH] [-update]\n\n"
                + "This indexes the documents in DOCS_PATH, creating a Lucene index"
                + "in INDEX_PATH that can be searched with SearchFiles";
        String indexPath = "index";
        String docsPath = null;
        boolean create = true;
        for (int i = 0; i < args.length; i++) {
            if ("-index".equals(args[i])) {
                indexPath = args[i + 1];
                i++;
            } else if ("-docs".equals(args[i])) {
                docsPath = args[i + 1];
                i++;
            } else if ("-update".equals(args[i])) {
                create = false;
            }
        }

        docsPath="cran\\cran.all.1400";

        if (docsPath == null) {
            System.err.println("Usage: " + usage);
            System.exit(1);
        }

        final Path docDir = Paths.get(docsPath);
        if (!Files.isReadable(docDir)) {
            System.out.println("Document directory '" + docDir.toAbsolutePath() + "' does not exist or is not readable, please check the path");
            System.exit(1);
        }

        Date start = new Date();
        try {
            System.out.println("Indexing to directory '" + indexPath + "'...");

            Directory dir = FSDirectory.open(Paths.get(indexPath));
            Analyzer analyzer = new StandardAnalyzer();
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

            if (create) {
                // Create a new index in the directory, removing any
                // previously indexed documents:
                iwc.setOpenMode(OpenMode.CREATE);
            } else {
                // Add new documents to an existing index:
                iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
            }

            // Optional: for better indexing performance, if you
            // are indexing many documents, increase the RAM
            // buffer.  But if you do this, increase the max heap
            // size to the JVM (eg add -Xmx512m or -Xmx1g):
            //
            // iwc.setRAMBufferSizeMB(256.0);

            IndexWriter writer = new IndexWriter(dir, iwc);
            indexCranfield(writer, docDir);

            // NOTE: if you want to maximize search performance,
            // you can optionally call forceMerge here.  This can be
            // a terribly costly operation, so generally it's only
            // worth it when your index is relatively static (ie
            // you're done adding documents to it):
            //
            // writer.forceMerge(1);

            writer.close();

            Date end = new Date();
            System.out.println(end.getTime() - start.getTime() + " total milliseconds");
        } catch (IOException e) {
            System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
        }
    }

    // Creates a document with the fields specified to be written to an index
    static Document createDocument(String s_no, String heading, String author, String ref, String answer){
        Document doc = new Document();
        doc.add(new StringField("s_no", s_no, Field.Store.YES));
        doc.add(new StringField("path", s_no, Field.Store.YES));
        doc.add(new TextField("heading", heading, Field.Store.YES));
        doc.add(new TextField("author", author, Field.Store.YES));
        doc.add(new TextField("reference", ref, Field.Store.YES));
        doc.add(new TextField("answer", answer, Field.Store.YES));
        return doc;
    }
    /**
     * Indexes the cranfield dataset by seperating the different title terms and adding the contents of them in document index
     **/
    static void indexCranfield(final IndexWriter writer, Path path) throws IOException {
        try (InputStream stream = Files.newInputStream(path)) {

            BufferedReader buffer = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));

            String s_no = "", heading = "", author = "", ref = "", answer = "", state = "";
            Boolean flag = true;
            String line_content;


            System.out.println("Indexing documents.");

            // Read in lines from the cranfield collection and create indexes for them
            while ((line_content = buffer.readLine()) != null){
                switch(line_content.substring(0,2)){
                    case ".I":
                        if(!flag){
                            Document fields = createDocument(s_no,heading,author,ref,answer);
                            writer.addDocument(fields);
                        }
                        else{ flag=false; }
                        heading = ""; author = ""; ref = ""; answer = "";
                        s_no = line_content.substring(3,line_content.length());
                        break;
                    case ".T":
                    case ".A":
                    case ".B":
                    case ".W":
                        state = line_content;
                        break;
                    default:
                        switch(state){
                            case ".T": heading += line_content + " ";
                            break;
                            case ".A": author += line_content + " ";
                            break;
                            case ".B": ref += line_content + " ";
                            break;
                            case ".W": answer += line_content + " ";
                            break;
                        }
                }
            }
            Document d = createDocument(s_no,heading,author,ref,answer);
            writer.addDocument(d);
        }
         }
}