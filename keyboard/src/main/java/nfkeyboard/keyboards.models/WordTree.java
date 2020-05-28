package nfkeyboard.keyboards.models;

import android.content.Context;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import io.separ.neural.inputmethod.indic.R;

public class WordTree {

    private static Logger log = Logger.getLogger(WordTree.class.getName());

    public static Node createTree(Context mContext, TST tst) {
        long startTime = System.currentTimeMillis();

        try {
            InputStream inputStream = getWordListFile(mContext);
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String line = "";
            String token[] = null;
            while ((line = br.readLine()) != null) {
                token = line.split("\t");
                // token[0] = actual word.
                // token[1] = frequency of the word.
                tst.insert(token[0], token[1]);
            }

            inputStream.close();
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        log.info("Tree creadted in : " + ((System.currentTimeMillis() - startTime)) + " ms");
        return tst.getRoot();
    }

    private static InputStream getWordListFile(Context mContext) throws URISyntaxException {
//        return mContext.getResources().openRawResource(R.raw.word_freq_en);
        return null;
    }
}
