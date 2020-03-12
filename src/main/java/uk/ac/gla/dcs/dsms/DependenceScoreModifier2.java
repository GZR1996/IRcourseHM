package uk.ac.gla.dcs.dsms;

import org.terrier.structures.postings.BlockPosting;
import org.terrier.structures.postings.Posting;
import org.terrier.matching.dsms.DependenceScoreModifier;

import java.util.Arrays;

/**
 * You should use this sample class to implement a proximity feature in Exercise 2.
 * TODO: Describe the function that your class implements
 * <p>
 * You can add your feature into a learned model by appending DSM:uk.ac.gla.IRcourse.SampleProxFeatureDSM to the features.list file.
 * @author Zirun Gan
 */
public class DependenceScoreModifier2 extends DependenceScoreModifier {


    /** This class is passed the postings of the current document,
     * and should return a score to represent that document.
     */
    @Override
    protected double calculateDependence(
            Posting[] ips, //postings for this document (these are actually IterablePosting[])
            boolean[] okToUse,  //is each posting on the correct document?
            double[] phraseTermWeights, boolean SD //not needed
    )
    {

        final int numberOfQueryTerms = okToUse.length;

        //***
        //TODO: in this part, write your code that inspects
        //the positions of query terms, to make a proximity function
        //NB: you can cast each Posting to org.terrier.structures.postings.BlockPosting
        //and use the getPositions() method to obtain the positions.
        //***
        double avg_dist = 0;
        int count = 0;
        for (int i = 0; i < numberOfQueryTerms; i++) {
            if (okToUse[i] == false) {
                continue;
            }

            for (int j = i + 1; j < numberOfQueryTerms; j++) {
                if (okToUse[j] == false) {
                    continue;
                }

                BlockPosting psi = (BlockPosting) ips[i];
                BlockPosting psj = (BlockPosting) ips[j];
                int []pi = psi.getPositions();
                int []pj = psj.getPositions();

                for (int di: pi) {
                    for (int dj: pj) {
                        count += 1;
                        avg_dist += Math.abs(di-dj);
                    }
                }
            }
        }

        return avg_dist/count;
    }

    /** You do NOT need to implement this method */
    @Override
    protected double scoreFDSD(int matchingNGrams, int docLength) {
        throw new UnsupportedOperationException();
    }


    @Override
    public String getName() {
        return "ProxFeatureDSM_MYFUNCTION";
    }

}
