package transmetteurs;

import destinations.DestinationInterface;
import information.Information;
import information.InformationNonConformeException;

/**
 * Récepteur analogique : reconvertit le signal Float en bits Boolean
 * par seuillage au milieu du temps bit.
 *
 * @author ziani
 * @author sissoko
 * @author nanda
 * @author blombou
 * @author bouaboud
 */
public class TransmetteurAnalogiqueLogique extends Transmetteur<Float, Boolean> {

    private int nbEch;
    private float seuil;

    /**
     * @param nbEch  nombre d'échantillons par bit
     * @param aMin   amplitude du bit 0
     * @param aMax   amplitude du bit 1 ; le seuil de décision est (aMin + aMax) / 2
     */
    public TransmetteurAnalogiqueLogique(int nbEch, float aMin, float aMax) {
        super();
        this.nbEch = nbEch;
        this.seuil = (aMin + aMax) / 2f;
    }

    @Override
    public void recevoir(Information<Float> information) throws InformationNonConformeException {
        this.informationRecue = information;
        emettre();
    }

    @Override
    public void emettre() throws InformationNonConformeException {
        informationEmise = new Information<Boolean>();
        int nbBits = informationRecue.nbElements() / nbEch;

        for (int i = 0; i < nbBits; i++) {
            float valeurMilieu = informationRecue.iemeElement(i * nbEch + nbEch / 2);
            informationEmise.add(valeurMilieu > seuil);
        }

        for (DestinationInterface<Boolean> dest : destinationsConnectees) {
            dest.recevoir(informationEmise);
        }
    }
}
