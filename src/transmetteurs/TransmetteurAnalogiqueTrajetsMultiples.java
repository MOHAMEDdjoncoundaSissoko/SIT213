package transmetteurs;

/**
 * Canal analogique à trajets multiples et bruité (Séance 4).
 *
 * Le signal reçu est :
 *
 *     r(n) = s(n) + somme_k( ar_k * s(n - dt_k) ) + b(n)
 *
 * où chaque trajet indirect k est défini par un décalage dt_k (en nombre
 * d'échantillons) et une amplitude relative ar_k. Le bruit b(n) est celui de
 * TransmetteurAnalogiqueBruite : sa puissance est déduite de Eb/N0 et de la
 * puissance Ps du signal avec échos. Avec Eb/N0 = +infini, seuls les échos
 * dégradent le signal.
 *
 * Le récepteur n'est PAS modifié à ce stade : ce canal sert à mesurer la
 * dégradation du TEB causée par les trajets multiples, avec un seuil de
 * décision classique côté récepteur.
 *
 * @author ziani
 * @author sissoko
 * @author nanda
 * @author blombou
 * @author bouaboud
 */
public class TransmetteurAnalogiqueTrajetsMultiples extends TransmetteurAnalogiqueBruite {

    /** nombre maximal de trajets indirects gérés */
    public static final int NB_TRAJETS_MAX = 5;

    private final int[] retards;
    private final float[] amplitudes;

    /**
     * Construit un canal à trajets multiples, bruité ou non.
     * @param nbEch       nombre d'échantillons par bit
     * @param ebN0Db      Eb/N0 en dB (Float.POSITIVE_INFINITY : pas de bruit)
     * @param retards     décalages dt_k des trajets indirects, en échantillons (positifs ou nuls)
     * @param amplitudes  amplitudes relatives ar_k des trajets indirects
     * @param seed        semence du générateur, ou null
     */
    public TransmetteurAnalogiqueTrajetsMultiples(int nbEch, float ebN0Db, int[] retards, float[] amplitudes,
                                                  Integer seed) {
        super(nbEch, ebN0Db, seed);

        if (retards.length != amplitudes.length) {
            throw new IllegalArgumentException("retards et amplitudes doivent avoir la même taille");
        }
        if (retards.length > NB_TRAJETS_MAX) {
            throw new IllegalArgumentException("Trop de trajets indirects : " + retards.length
                + " (maximum " + NB_TRAJETS_MAX + ")");
        }
        for (int dt : retards) {
            if (dt < 0) {
                throw new IllegalArgumentException("Retard de trajet indirect negatif : " + dt);
            }
        }

        this.retards = retards;
        this.amplitudes = amplitudes;
    }

    /**
     * Ajoute les échos au signal reçu : s(n) + somme_k ar_k * s(n - dt_k).
     * Avant le début d'un écho (n inférieur à dt_k), rien n'est encore réfléchi.
     * @return le signal avec échos, avant bruit
     */
    @Override
    protected float[] signalAvantBruit() {
        int n = this.informationRecue.nbElements();
        float[] avecTrajets = new float[n];
        for (int i = 0; i < n; i++) {
            float valeur = this.informationRecue.iemeElement(i);
            for (int k = 0; k < this.retards.length; k++) {
                int idxEcho = i - this.retards[k];
                if (idxEcho >= 0) {
                    valeur += this.amplitudes[k] * this.informationRecue.iemeElement(idxEcho);
                }
            }
            avecTrajets[i] = valeur;
        }
        return avecTrajets;
    }
}
