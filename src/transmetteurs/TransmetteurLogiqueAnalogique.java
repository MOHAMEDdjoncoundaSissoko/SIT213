package transmetteurs;

import destinations.DestinationInterface;
import information.Information;
import information.InformationFlottante;
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
     * Construit un émetteur qui convertit les bits en signal analogique.
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
        int nbBits = informationRecue.nbElements();
        // signal rangé dans un float[] (mémoire), de taille connue d'avance
        InformationFlottante signal =
            new InformationFlottante((int) Math.min((long) nbBits * nbEch, Integer.MAX_VALUE - 8));
        informationEmise = signal;

        for (int i = 0; i < nbBits; i++) {
            boolean bit = informationRecue.iemeElement(i);
            // NRZT : la rampe part du niveau du bit précédent (aMin avant le premier bit)
            boolean bitPrecedent = (i > 0) && informationRecue.iemeElement(i - 1);
            signal.ajouter(formeBit(forme, nbEch, aMin, aMax, bit, bitPrecedent));
        }

        for (DestinationInterface<Float> dest : destinationsConnectees) {
            dest.recevoir(informationEmise);
        }
    }

    /**
     * Échantillons d'un bit pour une forme d'onde donnée. Partagé avec le
     * récepteur, qui en déduit son filtre adapté.
     * @param forme         forme d'onde ("NRZ", "NRZT" ou "RZ" ; niveau constant sinon)
     * @param nbEch         nombre d'échantillons par bit
     * @param aMin          amplitude du bit 0
     * @param aMax          amplitude du bit 1
     * @param bit           valeur du bit
     * @param bitPrecedent  valeur du bit précédent (utilisée par la rampe NRZT)
     * @return les nbEch échantillons du bit
     */
    static float[] formeBit(String forme, int nbEch, float aMin, float aMax, boolean bit, boolean bitPrecedent) {
        float niveau = bit ? aMax : aMin;
        float debut = bitPrecedent ? aMax : aMin;

        // 2e borne dérivée de la 1re pour rester cohérent si nbEch n'est pas multiple de 3
        int finPremierTiers = nbEch / 3;
        int finDeuxiemeTiers = 2 * finPremierTiers;

        float[] echantillons = new float[nbEch];
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
            echantillons[e] = valeur;
        }
        return echantillons;
    }
}
