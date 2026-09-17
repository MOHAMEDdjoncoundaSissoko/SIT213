package transmetteurs;

import destinations.DestinationInterface;
import information.Information;
import information.InformationNonConformeException;

/**
 * Convertit un signal logique (Boolean) en signal analogique (Float).
 * Implémente les trois formes d'onde : NRZ, NRZT et RZ.
 *
 * @author ziani
 * @author sissoko
 * @author nanda
 */
public class TransmetteurLogiqueAnalogique extends Transmetteur<Boolean, Float> {

    private String forme;
    private int nbEch;
    private float aMin, aMax;

    /**
     * @param forme  forme d'onde ("NRZ", "NRZT" ou "RZ")
     * @param nbEch  nombre d'échantillons par bit
     * @param aMin   amplitude pour le niveau bas (bit 0)
     * @param aMax   amplitude pour le niveau haut (bit 1)
     */
    public TransmetteurLogiqueAnalogique(String forme, int nbEch, float aMin, float aMax) {
        super();
        this.forme = forme;
        this.nbEch = nbEch;
        this.aMin = aMin;
        this.aMax = aMax;
    }

    @Override
    public void recevoir(Information<Boolean> information) throws InformationNonConformeException {
        this.informationRecue = information;
        emettre();
    }

    @Override
    public void emettre() throws InformationNonConformeException {
        informationEmise = new Information<Float>();
        int nbBits = informationRecue.nbElements();

        for (int i = 0; i < nbBits; i++) {
            boolean bit = informationRecue.iemeElement(i);
            float niveau = bit ? aMax : aMin;

            for (int e = 0; e < nbEch; e++) {
                float valeur;
                switch (forme) {
                    case "NRZ":
                        valeur = niveau;
                        break;

                    case "RZ":
                        // signal actif seulement sur le tiers central du temps bit
                        valeur = (e >= nbEch / 3 && e < 2 * nbEch / 3) ? niveau : aMin;
                        break;

                    case "NRZT":
                        // montée/descente progressive sur T/3 en fonction des bits voisins
                        float niveauPrecedent = (i > 0) ? (informationRecue.iemeElement(i - 1) ? aMax : aMin) : aMin;
                        float niveauSuivant  = (i < nbBits - 1) ? (informationRecue.iemeElement(i + 1) ? aMax : aMin) : aMin;
                        int tiers = nbEch / 3;

                        if (e < tiers) {
                            // montée depuis le niveau précédent
                            float ratio = (float) e / tiers;
                            valeur = niveauPrecedent + ratio * (niveau - niveauPrecedent);
                        } else if (e < 2 * tiers) {
                            // plateau au niveau courant
                            valeur = niveau;
                        } else {
                            // descente vers le niveau suivant
                            float ratio = (float) (e - 2 * tiers) / tiers;
                            valeur = niveau + ratio * (niveauSuivant - niveau);
                        }
                        break;

                    default:
                        valeur = niveau;
                }
                informationEmise.add(valeur);
            }
        }

        for (DestinationInterface<Float> dest : destinationsConnectees) {
            dest.recevoir(informationEmise);
        }
    }
}
