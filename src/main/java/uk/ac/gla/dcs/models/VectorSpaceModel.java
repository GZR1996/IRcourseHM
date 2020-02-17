package uk.ac.gla.dcs.models;

import org.terrier.matching.MatchingQueryTerms;
import org.terrier.matching.models.WeightingModel;
import org.terrier.structures.*;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.postings.Posting;

import java.io.IOException;
import java.util.*;

/** You should use this sample class to implement a Simple TF*IDF weighting model for Exercise 1
 * of the exercise. You can tell Terrier to use your weighting model by specifying the
 * -w commandline option, or the property trec.model=uk.ac.gla.dcs.models.MyWeightingModel.
 * NB: There is a corresponding unit test that you should also complete to test your model.
 * @author Zirun Gan
 */
public class VectorSpaceModel extends WeightingModel
{
    private static final long serialVersionUID = 1L;

    public String getInfo() { return this.getClass().getSimpleName(); }

    boolean init = false;

    private static final double LOG_2 = Math.log(2);

    private static Map<Integer, Map<String, Double>> tfIdf;

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
        tfIdf = this.initTfIdf();

        init = true;
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

    private Map<Integer, Map<String, Double>> initTfIdf() {
        Map<Integer, Map<String, Double>> tfIdf = new HashMap<>();
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
                    Map<String, Double> temp = new HashMap<>();
                    tfIdf.put(docId, temp);
                    continue;
                }

                Map<String, Double> temp = new HashMap<>();
                while (postings.next() != IterablePosting.EOL) {
                    Map.Entry<String, LexiconEntry> lee = lex.getLexiconEntry(postings.getId());
                    String term = lee.getKey();
                    if (!tfIdf.containsKey(term)) {
                        LexiconEntry entry = lex.getLexiconEntry(term);
                        double termFrequency = entry.getFrequency();
                        double documentFrequency = entry.getDocumentFrequency();
                        double tf = Math.log(termFrequency) / LOG_2;
                        double idf = Math.log10((numberOfDocuments - documentFrequency + 0.5)/(documentFrequency + 0.5));
                        temp.put(term, (1+tf)*(idf));
                    }
                }
                tfIdf.put(docId, temp);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return tfIdf;
    }
    
    private double getNorm(double[] vector) {
        double norm = 0;
        for (double v: vector) {
            norm += v*v;
        }
        return Math.sqrt(norm);
    }

    private double getNorm(Collection<Double> vector) {
        double norm = 0;
        for (double v: vector) {
            norm += v*v;
        }
        return Math.sqrt(norm);
    }

    private double getCosineSimilarity(int docId, int termId) {
        Map<String, Double> docTfIdf = tfIdf.get(docId);
        double f = this.keyFrequency;
        String[] terms = this.rq.getMatchingQueryTerms().getTerms();
        String term = terms[termId-1];
        double termTfIdf = docTfIdf.containsKey(term) ? docTfIdf.get(term) : 0;
        double numerator = f * termTfIdf;
        double denominator = this.getNorm(new double[]{f}) * this.getNorm(docTfIdf.values());
        return numerator / denominator;
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
        MatchingQueryTerms queryTerms = this.rq.getMatchingQueryTerms();
        double numberOfDocuments = this.numberOfDocuments;
        double termFrequency = this.termFrequency;
        double idf = Math.log10((numberOfDocuments - termFrequency + 0.5));
        idf /= termFrequency + 0.5;
        tf = Math.log(tf) / Math.log(2);
        double score = this.getCosineSimilarity(0, p.getId());
        return this.keyFrequency * tf;
    }

    @Override
    public double score(double tf, double docLength) {
        throw new UnsupportedOperationException("other method is in use");
    }

}
