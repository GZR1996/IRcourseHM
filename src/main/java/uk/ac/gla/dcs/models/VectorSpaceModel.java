package uk.ac.gla.dcs.models;


import org.terrier.matching.models.WeightingModel;
import org.terrier.structures.*;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.postings.Posting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/** You should use this sample class to implement a Simple TF*IDF weighting model for Exercise 1
 * of the exercise. You can tell Terrier to use your weighting model by specifying the
 * -w commandline option, or the property trec.model=uk.ac.gla.dcs.models.MyWeightingModel.
 * NB: There is a corresponding unit test that you should also complete to test your model.
 * @author Zirun Gan
 */
public class VectorSpaceModel extends WeightingModel {
    private static final long serialVersionUID = 1L;

    public String getInfo() { return this.getClass().getSimpleName(); }

    boolean init = false;

    private static final double LOG_2 = Math.log(2);

    private Map<String, Double> tf;
    private Map<String, Double> idf;
    private static Map<String, Double> vector;

    //init() will NOT be needed in your Simple TF*IDF implementation but
    //will be needed for your vector space model implementation

    void init() {
        //you may complete any initialisation code here.
        //you may assume access to
        //averageDocumentLength (numberOfTokens /numberOfDocuments )
        //keyFrequency (The frequency of the term in the query)
        //documentFrequency (The document frequency of the term in the collection)
        //termFrequency (The frequency of the term in the collection)
        //numberOfDocuments (The number of documents in the collection)
        //numberOfTokens (the total length of all documents in the collection)

        //rq.getIndex() (the underlying Index)

        //rq.getMatchingQueryTerms() (the MatchingQueryTerms object,
        //which is the system's low level representation of the query)

        //Terrier will only have one index loaded at the once time, so
        //to share variables between weighting model instances, use static variables
        tf = this.getTf();
        idf = this.getIdf();

        init = true;
        Index index = rq.getIndex();
        Lexicon<String> lex = index.getLexicon();
        LexiconEntry le = lex.getLexiconEntry("the");
        double p = le == null
                ?  0.0d
                : (double) le.getFrequency() / index.getCollectionStatistics().getNumberOfTokens();
        System.out.println(le.getFrequency());
        System.out.println(le.getDocumentFrequency());
        this.getTf();
    }

    private double getCosineSimilarity(ArrayList<Double> document, ArrayList<Double> query) {
        double numerator = 0;
        double len1 = 0;
        double len2 = 0;
        for (int i = 0; i < document.size(); i++) {
            numerator += document.get(i) * query.get(i);
            len1 += document.get(i) * document.get(i);
            len2 += query.get(i) * query.get(i);
        }
        double denominator = Math.sqrt(len1) * Math.sqrt(len2);
        return numerator / denominator;
    }

    private double getTfIdf(String term) {
        return 1 + Math.log(tf.get(term))/Math.log(2) * Math.log10(idf.get(term));
    }

    private Map<String, Double> getTf() {
        Map<String, Double> map = new HashMap<>();
        double numberOfDocuments = this.numberOfDocuments;

        Index index = rq.getIndex();
        Lexicon<String> lex = index.getLexicon();
        PostingIndex<?> di = index.getDirectIndex();
        DocumentIndex doi = index.getDocumentIndex();
        //NB: postings will be null if the document is empty
        try {
            for (int docId = 0; docId < numberOfDocuments; docId++) {
                IterablePosting postings = di.getPostings(doi.getDocumentEntry(docId));
                if (postings == null) {
                    continue;
                }

                while (postings.next() != IterablePosting.EOL) {
                    Map.Entry<String, LexiconEntry> lee = lex.getLexiconEntry(postings.getId());
                    if (!map.containsKey(lee.getKey())) {
                        String term = lee.getKey();
                        LexiconEntry entry = lex.getLexiconEntry(term);
                        map.put(term, Math.log(entry.getFrequency())/LOG_2);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return map;
    }

    private Map<String, Double> getIdf() {
        Map<String, Double> map = new HashMap<>();
        double numberOfDocuments = this.numberOfDocuments;

        Index index = rq.getIndex();
        Lexicon<String> lex = index.getLexicon();
        PostingIndex<?> di = index.getDirectIndex();
        DocumentIndex doi = index.getDocumentIndex();
        //NB: postings will be null if the document is empty
        try {
            for (int docId = 0; docId < numberOfDocuments; docId++) {
                IterablePosting postings = di.getPostings(doi.getDocumentEntry(docId));
                if (postings == null) {
                    continue;
                }

                while (postings.next() != IterablePosting.EOL) {
                    Map.Entry<String, LexiconEntry> lee = lex.getLexiconEntry(postings.getId());
                    if (!map.containsKey(lee.getKey())) {
                        String term = lee.getKey();
                        LexiconEntry entry = lex.getLexiconEntry(term);
                        double documentFrequency = entry.getDocumentFrequency();
                        double idf = numberOfDocuments - documentFrequency + 0.5;
                        idf /= (documentFrequency + 0.5);
                        map.put(term, Math.log10(idf));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return map;
    }


    private Map<String, Double> getVector(Posting posting) {
        Map<String, Double> vector = new HashMap<>();

        return vector;
    }


    @Override
    public double score(Posting p) {
        if (! init)
            init();

        double tf = p.getFrequency();
        double docLength = p.getDocumentLength();
        //you should implement this method to return a score for a term occurring tf times in a document of docLength tokens.

        //you may assume access to the following member variables of the superclass:
        //averageDocumentLength (numberOfTokens /numberOfDocuments )
        //keyFrequency (The frequency of the term in the query)
        //documentFrequency (The document frequency of the term in the collection)
        //termFrequency (The frequency of the term in the collection)
        //numberOfDocuments (The number of documents in the collection)
        //numberOfTokens (the total length of all documents in the collection)
        //as well as any member variables you create
        System.out.println(" pppp: " + p.getFrequency());
        double numberOfTokens = this.numberOfTokens;
        double numberOfDocuments = this.numberOfDocuments;
        double averageDocumentLength = numberOfTokens / numberOfDocuments;
        double documentFrequency = this.documentFrequency;
        double termFrequency = this.termFrequency;
        double idf = Math.log10((numberOfDocuments - termFrequency + 0.5));
        idf /= termFrequency + 0.5;
        tf = Math.log(tf) / Math.log(2);
        double score = 1 + tf * idf;

        return score;
    }

    @Override
    public double score(double tf, double docLength) {
        throw new UnsupportedOperationException("other method is in use");
    }
}

