package sources;

import java.util.Random;
import information.Information;

/**
 * Source générant un message binaire aléatoire.
 *
 * @author ziani
 * @author sissoko
 * @author nanda
 * @author blombou
 * @author bouaboud
 */
public class SourceAleatoire extends Source<Boolean> {

    /**
     * @param nbBits longueur du message à générer
     */
    public SourceAleatoire(int nbBits) {
        this(nbBits, null);
    }

    /**
     * @param nbBits longueur du message à générer
     * @param seed   semence du générateur, ou null
     */
    public SourceAleatoire(int nbBits, Integer seed) {
        super();
        informationGeneree = new Information<Boolean>();
        Random rand = (seed != null) ? new Random(seed) : new Random();
        for (int i = 0; i < nbBits; i++) {
            informationGeneree.add(rand.nextBoolean());
        }
    }
}
