package transmetteurs;

import destinations.DestinationInterface;
import information.Information;
import information.InformationNonConformeException;

/**
 * Récepteur analogique à filtre adapté : reconvertit le signal Float en bits Boolean.
 *
 * Pour chaque bit, le signal reçu r est corrélé sur tout le temps bit avec
 * g = s1 - s0, où s1 et s0 sont les formes d'un bit 1 et d'un bit 0 produites
 * par l'émetteur (TransmetteurLogiqueAnalogique) :
 *
 *     y = somme_k r[k] * g[k]      bit = 1 si y &gt; seuil
 *     seuil = somme_k g[k] * (s1[k] + s0[k]) / 2 = (||s1||^2 - ||s0||^2) / 2
 *
 * La même formule convient à NRZ, NRZT et RZ : g porte la forme d'onde (moyenne
 * du bit en NRZ, tiers central en RZ, rampes moins pondérées en NRZT).
 * En NRZT, la rampe dépend du bit précédent : s1 et s0 sont moyennés sur les
 * deux valeurs possibles de ce bit, ce qui laisse dans g la seule partie qui
 * dépend du bit courant.
 *
 * @author ziani
 * @author sissoko
 * @author nanda
 * @author blombou
 * @author bouaboud
 */
public class TransmetteurAnalogiqueLogique extends Transmetteur<Float, Boolean> {

    private int nbEch;

    /** réponse du filtre adapté sur un temps bit : g = s1 - s0 */
    private float[] filtre;

    /** seuil de décision appliqué à la sortie du filtre */
    private double seuil;

    /** decalage de la fenetre de decision, en nombre d'echantillons */
    private int decalageFenetre;

    /**
     * Construit un récepteur à filtre adapté à la forme d'onde de l'émetteur.
     * @param forme  forme d'onde ("NRZ", "NRZT" ou "RZ"), la même qu'à l'émission
     * @param nbEch  nombre d'échantillons par bit
     * @param aMin   amplitude du bit 0
     * @param aMax   amplitude du bit 1
     */
    public TransmetteurAnalogiqueLogique(String forme, int nbEch, float aMin, float aMax) {
        this(forme, nbEch, aMin, aMax, 0);
    }

    /**
     * Construit un recepteur avec un decalage de fenetre de decision.
     * @param forme             forme d'onde ("NRZ", "NRZT" ou "RZ")
     * @param nbEch             nombre d'echantillons par bit
     * @param aMin              amplitude du bit 0
     * @param aMax              amplitude du bit 1
     * @param decalageFenetre   decalage de la fenetre, en echantillons
     */
    public TransmetteurAnalogiqueLogique(String forme, int nbEch, float aMin, float aMax, int decalageFenetre) {
        super();
        this.nbEch = nbEch;
        setDecalageFenetre(decalageFenetre);

        // formes de référence moyennées sur le bit précédent (seule la rampe NRZT en dépend)
        float[] s1 = new float[nbEch];
        float[] s0 = new float[nbEch];
        for (boolean bitPrecedent : new boolean[] {false, true}) {
            float[] un = TransmetteurLogiqueAnalogique.formeBit(forme, nbEch, aMin, aMax, true, bitPrecedent);
            float[] zero = TransmetteurLogiqueAnalogique.formeBit(forme, nbEch, aMin, aMax, false, bitPrecedent);
            for (int k = 0; k < nbEch; k++) {
                s1[k] += un[k] / 2f;
                s0[k] += zero[k] / 2f;
            }
        }

        this.filtre = new float[nbEch];
        this.seuil = 0.0;
        for (int k = 0; k < nbEch; k++) {
            filtre[k] = s1[k] - s0[k];
            seuil += filtre[k] * (s1[k] + s0[k]) / 2.0;
        }
    }

    /**
     * Modifie le decalage de la fenetre de decision.
     * @param decalageFenetre decalage en echantillons, strictement inferieur a nbEch en valeur absolue
     */
    public void setDecalageFenetre(int decalageFenetre) {
        if (Math.abs(decalageFenetre) >= nbEch) {
            throw new IllegalArgumentException("Decalage de fenetre invalide : " + decalageFenetre
                + " (doit etre strictement inferieur a nbEch en valeur absolue)");
        }
        this.decalageFenetre = decalageFenetre;
    }

    /**
     * @return le decalage courant de la fenetre de decision
     */
    public int getDecalageFenetre() {
        return decalageFenetre;
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
            double y = 0.0;
            for (int k = 0; k < nbEch; k++) {
                y += echantillonFenetre(i * nbEch + k + decalageFenetre) * filtre[k];
            }
            informationEmise.add(y > seuil);
        }

        for (DestinationInterface<Boolean> dest : destinationsConnectees) {
            dest.recevoir(informationEmise);
        }
    }

    private float echantillonFenetre(int index) {
        if (index < 0 || index >= informationRecue.nbElements()) {
            return 0.0f;
        }
        return informationRecue.iemeElement(index);
    }
}
