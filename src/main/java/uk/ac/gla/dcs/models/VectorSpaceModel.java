package uk.ac.gla.dcs.models;

import org.terrier.matching.models.WeightingModel;
import org.terrier.structures.*;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.postings.Posting;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;

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

    private static Map<String, Double> tfIdf;

    void init(int docId) {
        tfIdf = this.initTfIdf(docId);
    }

    private Map<String, Double> initTfIdf(int docId) {
        Map<String, Double> tfIdf = new HashMap<>();
        double numberOfDocuments = this.numberOfDocuments;

        Index index = rq.getIndex();
        Lexicon<String> lex = index.getLexicon();
        PostingIndex<?> di = index.getDirectIndex();
        DocumentIndex doi = index.getDocumentIndex();
        //NB: postings will be null if the document is empty
        try {
            IterablePosting postings = di.getPostings(doi.getDocumentEntry(docId));
            while (postings.next() != IterablePosting.EOL) {
                String term = lex.getLexiconEntry(postings.getId()).getKey();
                LexiconEntry entry = lex.getLexiconEntry(term);
                double termFrequency = entry.getFrequency();
                double documentFrequency = entry.getDocumentFrequency();
                double tf = Math.log(termFrequency) / LOG_2;
                double idf = Math.log10((numberOfDocuments - documentFrequency + 0.5)/(documentFrequency + 0.5));
                tfIdf.put(term, (1+tf)*(idf));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return tfIdf;
    }

    private double getNumerator(Map<String, Double> docTfIdf, String[] terms) {
        Map<String, Integer> temp = new HashMap<>();
        for (String term: terms) {
            if (!temp.containsKey(term)) {
                temp.put(term, 1);
            } else {
                temp.put(term, temp.get(term)+1);
            }
        }

        double numerator = 0;
        for (int i = 0; i < terms.length; i++) {
            double termTfIdf = docTfIdf.containsKey(terms[i]) ? docTfIdf.get(terms[i]) : 0;
            numerator += temp.get(terms[i]) * termTfIdf;
        }

        return numerator;
    }

    private double getNorm(Collection<Double> vector) {
        double norm = 0;
        for (double v: vector) {
            norm += v*v;
        }
        return norm;
    }

    private double getCosineSimilarity(int docId, String[] terms) {
        double f = this.keyFrequency;
        double numerator = this.getNumerator(tfIdf, terms);
        double denominator = Math.sqrt(f*f * this.getNorm(tfIdf.values()));
        return numerator / denominator;
    }

    @Override
    public double score(Posting p) {
        init(p.getId());

        if (p.getId() % 100 == 0) {
            System.out.println(p.getId());
        }

        double score = this.getCosineSimilarity(p.getId(), this.rq.getMatchingQueryTerms().getTerms());
        return score;
    }

    @Override
    public double score(double tf, double docLength) {
        throw new UnsupportedOperationException("other method is in use");
    }
}
