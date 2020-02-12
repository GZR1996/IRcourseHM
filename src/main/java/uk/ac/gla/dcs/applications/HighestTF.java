package uk.ac.gla.dcs.applications;

import org.terrier.applications.CLITool;
import org.terrier.structures.Index;
import org.terrier.structures.Lexicon;
import org.terrier.structures.LexiconEntry;
import org.terrier.terms.PorterStemmer;

/**
 * This is a template class for the first lab, for identifying the document with the highest term
 * frequency for a given term. To run this class:
 *  - ensure that you have run the "mvn install" command for this project
 *  - add terrier.mvn.coords=uk.ac.gla.dcs:ircourse:1.0-SNAPSHOT to the terrier.properties file
 *  - run bin/terrier highest-tf
 */
public class HighestTF extends CLITool {

    @Override
    public int run(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Usage: " + this.commandname() + " term");
            System.err.println(this.helpsummary());
            return -1;
        }
        String term = args[0];
        term = new PorterStemmer().stem(term);
        

        Index index = Index.createIndex();

        // has the highest term frequency for the specified term
        // You may refer to a useful part of the Terrier documentation
        // https://github.com/terrier-org/terrier-core/blob/5.x/doc/extend_retrieval.md#using-terrier-indices-in-your-own-code
        Lexicon<String> lex = index.getLexicon();
        LexiconEntry le = lex.getLexiconEntry("term");
        if (le != null) {
            System.out.println("Term term occurs in " + le.getDocumentFrequency() + " documents");
        } else {
            System.out.println("Term term does not occur");
        }
        
        //leave this is as.
        return 0;
    }

    @Override
    public String commandname() {
        return "highest-tf";
    }

    @Override
    public String sourcepackage() {
        return "IRcourseHM";
    }

	@Override
	public String helpsummary() {
		return "identifies the document with highest tf for given term";
	}
    
}