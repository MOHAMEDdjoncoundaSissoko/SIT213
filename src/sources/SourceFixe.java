package sources;

import information.Information;

/**
 * Source émettant un message binaire imposé (option -mess suivie de 0 et de 1).
 *
 * @author ziani
 * @author sissoko
 * @author nanda
 * @author blombou
 * @author bouaboud
 */
public class SourceFixe extends Source<Boolean> {

    /**
     * @param messageBinaire suite de '0' et de '1'
     */
    public SourceFixe(String messageBinaire) {
        super();
        informationGeneree = new Information<Boolean>();
        for (int i = 0; i < messageBinaire.length(); i++) {
            informationGeneree.add(messageBinaire.charAt(i) == '1');
        }
    }
}
