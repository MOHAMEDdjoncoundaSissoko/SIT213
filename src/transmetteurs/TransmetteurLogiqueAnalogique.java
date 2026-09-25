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
 * @author blombou
 * @author bouaboud
 */
public class TransmetteurLogiqueAnalogique extends Transmetteur<Boolean, Float> {

    /** En dessous, le tiers central (RZ) ou la rampe (NRZT) tient sur 0 ou 1 échantillon. */
    public static final int NB_ECH_MIN = 10;

    private String forme;
    private int nbEch;
    private float aMin, aMax;

    /**
     * @param forme  forme d'onde ("NRZ", "NRZT" ou "RZ")
     * @param nbEch  nombre d'échantillons par bit (doit être >= {@value #NB_ECH_MIN})
     * @param aMin   amplitude pour le niveau bas (bit 0)
     * @param aMax   amplitude pour le niveau haut (bit 1)
     * @throws IllegalArgumentException si nbEch {@literal <} NB_ECH_MIN
     */
    public TransmetteurLogiqueAnalogique(String forme, int nbEch, float aMin, float aMax) {
        super();
        if (nbEch < NB_ECH_MIN) {
            throw new IllegalArgumentException(
                "nbEch invalide : " + nbEch + " (minimum " + NB_ECH_MIN + " pour la lisibilité)");
        }
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

        // 2e borne dérivée de la 1re pour rester cohérent si nbEch n'est pas multiple de 3
        int finPremierTiers = nbEch / 3;
        int finDeuxiemeTiers = 2 * finPremierTiers;

        for (int i = 0; i < nbBits; i++) {
            boolean bit = informationRecue.iemeElement(i);
            float niveau = bit ? aMax : aMin;

            // NRZT : la rampe part du niveau du bit précédent
            float debut = (i == 0) ? aMin : (informationRecue.iemeElement(i - 1) ? aMax : aMin);

            for (int e = 0; e < nbEch; e++) {
                float valeur;
                switch (forme) {
                    case "NRZ":
                        valeur = niveau;
                        break;

                    case "RZ":
                        valeur = (e >= finPremierTiers && e < finDeuxiemeTiers) ? niveau : aMin;
                        break;

                    case "NRZT":
                        if (e < finPremierTiers) {
                            float p = (float) e / (float) finPremierTiers;
                            valeur = debut + p * (niveau - debut);
                        } else {
                            valeur = niveau;
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
