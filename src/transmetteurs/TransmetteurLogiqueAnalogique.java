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

    /** nombre minimal d'échantillons par bit pour obtenir une forme d'onde lisible
     *  (en dessous, le tiers central RZ / la rampe NRZT tient sur 0 ou 1 échantillon
     *  et le tracé dégénère en pointe triangulaire au lieu d'un plateau). */
    public static final int NB_ECH_MIN = 10;

    private String forme;
    private int nbEch;
    private float aMin, aMax;

    /**
     * @param forme  forme d'onde ("NRZ", "NRZT" ou "RZ")
     * @param nbEch  nombre d'échantillons par bit (doit être >= {@value #NB_ECH_MIN})
     * @param aMin   amplitude pour le niveau bas (bit 0)
     * @param aMax   amplitude pour le niveau haut (bit 1)
     * @throws IllegalArgumentException si nbEch < NB_ECH_MIN
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

        // bornes des tiers calculées une seule fois (et de façon cohérente : le 2e tiers
        // est calculé à partir du 1er, plutôt que d'une division entière indépendante,
        // pour éviter que les deux calculs se désynchronisent quand nbEch n'est pas
        // multiple de 3)
        int finPremierTiers = nbEch / 3;
        int finDeuxiemeTiers = 2 * finPremierTiers;

        for (int i = 0; i < nbBits; i++) {
            boolean bit = informationRecue.iemeElement(i);
            float niveau = bit ? aMax : aMin;

            // pour NRZT : niveau de départ = niveau du bit précédent (calculé une seule
            // fois par bit, pas à chaque échantillon)
            float debut = (i == 0) ? aMin : (informationRecue.iemeElement(i - 1) ? aMax : aMin);

            for (int e = 0; e < nbEch; e++) {
                float valeur;
                switch (forme) {
                    case "NRZ":
                        valeur = niveau;
                        break;

                    case "RZ":
                        // signal actif seulement sur le tiers central du temps bit
                        valeur = (e >= finPremierTiers && e < finDeuxiemeTiers) ? niveau : aMin;
                        break;

                    case "NRZT":
                        if (e < finPremierTiers) {
                            // Transition (Montée ou Descente) sur le premier tiers
                            float p = (float) e / (float) finPremierTiers;
                            valeur = debut + p * (niveau - debut);
                        } else {
                            // Stable sur les deux derniers tiers
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
