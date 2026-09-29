package transmetteurs;

import destinations.DestinationInterface;
import information.Information;
import information.InformationNonConformeException;

import java.util.Random;

/**
 * Canal analogique à trajets multiples et bruité (Séance 4).
 *
 * Le signal reçu est :
 *
 *     r(n) = s(n) + somme_k( alpha_k * s(n - tau_k) ) + b(n)
 *
 * où chaque trajet k est défini par une atténuation alpha_k (entre 0 et 1)
 * et un retard tau_k (en nombre d'échantillons), et b(n) est un bruit
 * gaussien dont la puissance est déduite du Eb/N0 souhaité, comme dans
 * TransmetteurAnalogiqueBruite.
 *
 * Le récepteur n'est PAS modifié à ce stade : ce canal sert à mesurer la
 * dégradation du TEB causée par les trajets multiples, avec un seuil de
 * décision classique côté récepteur.
*/

public class TransmetteurAnalogiqueTrajetsMultiples extends Transmetteur<Float, Float> {
 
    /** nombre maximal de trajets secondaires gérés */
    public static final int NB_TRAJETS_MAX = 5;
 
    private final int nbEch;
    private final float ebN0Db;
    private final float[] alphas;
    private final int[] taus;
    private final Random rand;

    public TransmetteurAnalogiqueTrajetsMultiples(int nbEch, float ebN0Db, float[] alphas, int[] taus, Integer seed) { 
        
        super();

        if (alphas.length != taus.length) {
            throw new IllegalArgumentException("alphas et taus doivent avoir la même taille");
        }

        if (alphas.length > NB_TRAJETS_MAX) {
            throw new IllegalArgumentException("Trop de trajets multiples : " + alphas.length + " (maximum " + NB_TRAJETS_MAX + ")");
        }
        
        this.nbEch = nbEch;
        this.ebN0Db = ebN0Db;
        this.alphas = alphas;
        this.taus = taus;

        if (seed != null) {
            this.rand = new Random(seed);
        } else {
            this.rand = new Random();
        }
    }

    @Override
    public void recevoir (Information <Float> information) throws InformationNonConformeException {
        this.informationRecue = information;
        emettre();
    }

    @Override 
    public void emettre() throws InformationNonConformeException {
    
        int n = this.informationRecue.nbElements();
        this.informationEmise = new Information<Float>();

        if (n==0) {
            for (DestinationInterface<Float> dest : destinationsConnectees) {
                dest.recevoir(this.informationEmise);
            }
            return;
        }
        
        // 1) Construction du signal avec échos (avant bruit) :
        // valeur(n) = s(n) + somme_k alpha_k * s(n - tau_k)
        float [] avecTrajets = new float[n];
        for (int i = 0; i < n; i++) {
            float valeur = this.informationRecue.iemeElement(i);
            for (int k = 0; k < this.alphas.length; k++) {
                int idxEcho = i - this.taus[k];
                if (idxEcho >= 0) {
                    valeur += this.alphas[k] * this.informationRecue.iemeElement(idxEcho);
                }
                // si idxEcho < 0 : pas d'écho au tout début du signal (rien à réfléchir encore)
            }
            avecTrajets[i] = valeur;
        }

        // 2) Puissance moyenne du signal (après échos, avant bruit)
        double sommeCarres = 0;
        for (int i = 0; i < n; i++) {
            sommeCarres += avecTrajets[i] * avecTrajets[i];
        }
        double puissanceSignal = sommeCarres / n;

        // 3) sigma_b^2 déduit de Eb/N0 (même formule que TransmetteurAnalogiqueBruite)
        double ebN0Lineaire = Math.pow(10, ebN0Db / 10.0);
        double sigmaB2 = puissanceSignal / (2 * ebN0Lineaire);
        double sigmaB = Math.sqrt(sigmaB2);

        // 4) Ajout du bruit au signal
        
        // 4) Ajout du bruit gaussien
        for (int i = 0; i < n; i++) {
            double bruit = genererBruitGaussien(sigmaB);
            this.informationEmise.add((float) (avecTrajets[i] + bruit));
        }

        for (DestinationInterface<Float> dest : destinationsConnectees) {
            dest.recevoir(this.informationEmise);
        }

    }

    /**
     * Génère un échantillon de bruit gaussien centré, d'écart-type sigmaB,
     * à partir de deux tirages uniformes (méthode donnée en cours).
     * @param sigmaB écart-type du bruit
     * @return un échantillon de bruit gaussien
    */

    private double genererBruitGaussien(double sigma) {
        // Génération d'un bruit gaussien avec la méthode de Box-Muller
        double u1 = rand.nextDouble();
        double u2 = rand.nextDouble();
        return sigma * Math.sqrt(-2.0 * Math.log(1.0 - u1)) * Math.cos(2.0 * Math.PI * u2);
    }   
}