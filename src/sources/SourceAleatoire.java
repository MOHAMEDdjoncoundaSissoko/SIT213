package sources;

import java.util.Random;
import information.Information;

/**
 * Source générant un message binaire aléatoire.
 * On peut fixer une semence pour reproduire le même tirage d'une exécution à l'autre.
 *
 * @author ziani
 * @author sissoko
 * @author nanda
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
     * @param seed   semence du générateur aléatoire, null si on n'en veut pas
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
