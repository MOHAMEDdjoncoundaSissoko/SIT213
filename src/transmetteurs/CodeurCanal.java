package transmetteurs;

import destinations.DestinationInterface;
import information.Information;
import information.InformationNonConformeException;

/**
 * Codeur de canal (étape 5) : chaque bit du message devient un paquet de
 * trois bits, 0 donnant 010 et 1 donnant 101.
 *
 * Les deux mots de code diffèrent sur leurs trois bits : le décodeur
 * (DecodeurCanal) peut donc corriger une erreur par paquet. Le signal codé ne
 * contient jamais plus de deux bits consécutifs de même valeur.
 *
 * @author ziani
 * @author sissoko
 * @author nanda
 * @author blombou
 * @author bouaboud
 */
public class CodeurCanal extends Transmetteur<Boolean, Boolean> {

    /** nombre de bits émis pour un bit reçu */
    public static final int LONGUEUR_MOT = 3;

    /** Construit un codeur de canal. */
    public CodeurCanal() {
        super();
    }

    @Override
    public void recevoir(Information<Boolean> information) throws InformationNonConformeException {
        this.informationRecue = information;
        emettre();
    }

    @Override
    public void emettre() throws InformationNonConformeException {
        informationEmise = new Information<Boolean>();
        for (boolean bit : informationRecue) {
            // 0 -> 010, 1 -> 101 : le bit, son inverse, le bit
            informationEmise.add(bit);
            informationEmise.add(!bit);
            informationEmise.add(bit);
        }

        for (DestinationInterface<Boolean> dest : destinationsConnectees) {
            dest.recevoir(informationEmise);
        }
    }
}
