package sources;

import information.Information;

/**
 * Source émettant un message binaire imposé, fourni sous forme de chaîne
 * de caractères ('0' et '1').
 *
 * @author ziani
 * @author sissoko
 * @author nanda
 */
public class SourceFixe extends Source<Boolean> {

    /**
     * @param messageBinaire chaîne composée de '0' et '1'
     */
    public SourceFixe(String messageBinaire) {
        super();
        informationGeneree = new Information<Boolean>();
        for (int i = 0; i < messageBinaire.length(); i++) {
            informationGeneree.add(messageBinaire.charAt(i) == '1');
        }
    }
}
