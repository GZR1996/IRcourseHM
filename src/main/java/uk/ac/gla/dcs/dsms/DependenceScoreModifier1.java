package uk.ac.gla.dcs.dsms;

import org.terrier.structures.LexiconEntry;
import org.terrier.structures.postings.BlockPosting;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.postings.Posting;
import org.terrier.matching.dsms.DependenceScoreModifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * You should use this sample class to implement a proximity feature in Exercise 2.
 * TODO: Describe the function that your class implements
 * <p>
 * You can add your feature into a learned model by appending DSM:uk.ac.gla.IRcourse.SampleProxFeatureDSM to the features.list file.
 * @author Zirun Gan
 */
public class DependenceScoreModifier1 extends DependenceScoreModifier {

    private static Map<Integer, Integer> Dk = new HashMap<>();

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

        double min_dist = Double.MAX_VALUE;
        for (int i = 0; i < numberOfQueryTerms; i++) {
            if (okToUse[i] == false) {
                continue;
            }

            for (int j = i+1; j < numberOfQueryTerms; j++) {
                if (okToUse[j] == false) {
                    continue;
                }

                BlockPosting psi = (BlockPosting) ips[i];
                BlockPosting psj = (BlockPosting) ips[j];
                int []pi = psi.getPositions();
                int []pj = psj.getPositions();
                int indexI = 0;
                int indexJ = 0;

                while (true) {
                    if (indexI >= pi.length || indexJ >= pj.length) {
                        break;
                    }
                    int dist = Math.abs(pi[indexI]-pj[indexJ]);
                    if (dist < min_dist) {
                        min_dist = dist;
                    }

                    if (pi[indexI] > pj[indexJ]) {
                        indexJ ++;
                    } else {
                        indexI ++;
                    }
                }
            }

            if (min_dist == 1) {
                break;
            }
        }

        return min_dist;
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
