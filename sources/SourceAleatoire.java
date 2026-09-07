package sources;

import java.util.Random;

import information.Information;

public class SourceAleatoire extends Source<Boolean> {

    /**
     * construit une SourceAleatoire sans semence (tirage non reproductible).
     * @param nbBits la longueur du message à générer
     */
    public SourceAleatoire(int nbBits) {
        this(nbBits, null);
    }

    /**
     * construit une SourceAleatoire dont le générateur aléatoire est
     * initialisé avec la semence fournie (ou non initialisé si seed
     * vaut null).
     * @param nbBits la longueur du message à générer
     * @param seed   la semence à utiliser, ou null
     */
    public SourceAleatoire(int nbBits, Integer seed) {
        super();

        // On crée une nouvelle Information pour stocker le message binaire
        informationGeneree = new Information<Boolean>();

        // Génère un message binaire aléatoire de longueur nbBits
        Random random = (seed != null) ? new Random(seed) : new Random();
        for (int i = 0; i < nbBits; i++) {
            // Ajoute un booléen aléatoire (true ou false) à l'information générée
            informationGeneree.add(random.nextBoolean());
        }
    }
    
}
