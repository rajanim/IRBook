package ir.part3;

import java.util.HashMap;

/**
 * Created by rajanishivarajmaski1 on 10/20/16.
 * <p>
 * Implementing one of the synonyms behavior that is expansion=false.
 * As explained in the below link, "expand - true if conflation groups should be expanded, false if they are one-directional"
 * Its one directional implementation here. small => tiny,teeny,weeny
 * {link}
 * https://lucene.apache.org/core/4_6_0/analyzers-common/org/apache/lucene/analysis/synonym/SynonymFilterFactory.html
 */
public class Synonyms {

    static HashMap<String, String> synonymList;

    /**
     * Load synonyms one directional mapping into java hash map.
     */
    public static void loadSynonymsToMemory() {
        synonymList = new HashMap<>();
        synonymList.put("speed", "responsive, fast, quick");
        synonymList.put("responsive", "speed, fast, quick");
        synonymList.put("phone", "mobile, cell");
        synonymList.put("gb", "gib, gigabyte");
    }

    /**
     *look up map to find if there are synonyms matching to search query terms.
     * @param searchString
     * @return
     */

    public static String lookUpSynonyms(String searchString) {
        StringBuffer synQuery = new StringBuffer();
        String[] terms = searchString.split("\\W+");
        for (String string : terms) {
            if(synonymList.containsKey(string))
                synQuery.append(synonymList.get(string)).append(" ");
        }
        return synQuery.toString().replace(",", " ").trim();

    }

}
