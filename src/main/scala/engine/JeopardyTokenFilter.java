package engine;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import edu.stanford.nlp.simple.*;


import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public final class JeopardyTokenFilter extends TokenFilter {
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

    public JeopardyTokenFilter(TokenStream input) {
        super(input);
    }

    public JeopardyTokenFilter(TokenStream in, String name) {
        super(in);
    }

    /** Returns the next input Token, after being lemmatized */
    @Override
    public final boolean incrementToken() throws IOException {
        if (input.incrementToken()) {
            String buffer_contents = String.valueOf(
                    Arrays.copyOfRange(termAtt.buffer(),0, termAtt.length()));
            if (buffer_contents.length() == 0){
                return true;
            }
            List<String> buffer_sent;
            try {
                buffer_sent = new Sentence(buffer_contents).lemmas();
            } catch (IllegalStateException ex){
                System.out.println("Encountered that exception!");
                return true;
            }
            String output = "";
            for (String token : buffer_sent){
                output += token + " ";
            }
            char[] output_buffer = output.substring(0, output.length()-1).toCharArray();
            termAtt.copyBuffer(output_buffer, 0, output_buffer.length);
            termAtt.setLength(output_buffer.length);
            return true;
        } else {
            return false;
        }
    }
}