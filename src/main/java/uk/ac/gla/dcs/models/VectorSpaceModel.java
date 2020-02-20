package uk.ac.gla.dcs.models;

import org.terrier.matching.models.WeightingModel;
import org.terrier.querying.*;
import org.terrier.structures.*;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.postings.Posting;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;

/** You should use this sample class to implement a Simple TF*IDF weighting model for Exercise 1
 * of the exercise. You can tell Terrier to use your weighting model by specifying the
 * -w commandline option, or the property trec.model=uk.ac.gla.dcs.models.VectorSpaceModel.
 * NB: There is a corresponding unit test that you should also complete to test your model.
 * @author Zirun Gan
 */
public class VectorSpaceModel extends WeightingModel
{
    private static final long serialVersionUID = 1L;

    public String getInfo() { return this.getClass().getSimpleName(); }

    private static final double LOG_2 = Math.log(2);

    private Map<Integer, Double> tfIdf;
    private static Map<Integer, Integer> Dk = new HashMap<>();

    void init(int docId) {
        tfIdf = this.initTfIdf(docId);
    }

    private Map<Integer, Double> initTfIdf(int docId) {
        Map<Integer, Double> tfIdf = new HashMap<Integer, Double>(10000);
        double numberOfDocuments = this.numberOfDocuments;

        Index index = rq.getIndex();
        Lexicon<String> lex = index.getLexicon();
        PostingIndex<?> di = index.getDirectIndex();
        DocumentIndex doi = index.getDocumentIndex();
        try {
            IterablePosting postings = di.getPostings(doi.getDocumentEntry(docId));
            while (postings.next() != IterablePosting.EOL) {
                int pid = postings.getId();
                double documentFrequency;
                if (Dk.containsKey(postings.getId())) {
                    documentFrequency = Dk.get(pid);
                } else {
                    Map.Entry<String,LexiconEntry> lee = lex.getLexiconEntry(pid);
                    LexiconEntry entry = lee.getValue();
                    documentFrequency = entry.getDocumentFrequency();
                    Dk.put(pid, entry.getDocumentFrequency());
                }
                double termFrequency = postings.getFrequency();
                double tf = Math.log10(termFrequency);
                double idf = Math.log10((numberOfDocuments - documentFrequency + 0.5)/(documentFrequency + 0.5));
                tfIdf.put(pid, 1+tf*idf);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return tfIdf;
    }

    private double getTfIdf(double f) {
        double tf = Math.log10(f);
        double idf = Math.log10((this.numberOfDocuments - this.documentFrequency + 0.5) / (this.documentFrequency + 0.5));
        return 1+tf*idf;
    }

    private double getNorm(Collection<Double> vector) {
        double norm = 0;
        for (double v: vector) {
            norm += v*v;
        }
        return norm;
    }

    private double getCosineSimilarity(double f) {
        double numerator = f * this.getTfIdf(f);
        double denominator = Math.sqrt(f*f) * Math.sqrt(this.getNorm(tfIdf.values()));
        return numerator / denominator;
    }

    @Override
    public double score(Posting p) {
        init(p.getId());

        double score = this.getCosineSimilarity(p.getFrequency());
        return score;
    }

    @Override
    public double score(double tf, double docLength) {
        throw new UnsupportedOperationException("other method is in use");
    }
}
