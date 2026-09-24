package transmetteurs;

import destinations.DestinationInterface;
import information.Information;
import information.InformationNonConformeException;

import java.util.Random;

/**
 * Canal analogique à bruit blanc additif gaussien (AWGN).
 *
 * Le signal reçu r(n) = s(n) + b(n), où b(n) est un bruit gaussien de
 * moyenne nulle dont la variance sigma_b^2 est calculée à partir du
 * rapport signal-sur-bruit par bit souhaité (Eb/N0, en dB) :
 *
 *     (Eb/N0)_dB = 10 * log10( (Ps * N) / (2 * sigma_b^2) )
 *
 * avec Ps la puissance moyenne du signal reçu et N le nombre
 * d'échantillons par bit (nbEch).
 *
 * Le bruit gaussien est généré à partir de deux tirages uniformes
 * a1, a2 dans [0,1[ via la transformée :
 *
 *     b(n) = sigma_b * sqrt(-2*ln(1 - a1)) * cos(2*pi*a2)
 */
public class TransmetteurAnalogiqueBruite extends Transmetteur<Float, Float> {

    private final int nbEch;
    private final float ebN0Db;
    private final Random rand;

    /**
     * @param nbEch   nombre d'échantillons par bit
     * @param ebN0Db  rapport signal-sur-bruit par bit souhaité, en dB (Eb/N0)
     * @param seed    graine du générateur aléatoire (pour la reproductibilité) ;
     *                si null, le générateur est initialisé sans graine fixe
     */
    public TransmetteurAnalogiqueBruite(int nbEch, float ebN0Db, Integer seed) {
        super();
        this.nbEch = nbEch;
        this.ebN0Db = ebN0Db;
        this.rand = (seed != null) ? new Random(seed) : new Random();
    }

    @Override
    public void recevoir(Information<Float> information) throws InformationNonConformeException {
        this.informationRecue = information;
        emettre();
    }

    @Override
    public void emettre() throws InformationNonConformeException {
        int n = this.informationRecue.nbElements();
        this.informationEmise = new Information<Float>();

        if (n == 0) {
            for (DestinationInterface<Float> dest : destinationsConnectees) {
                dest.recevoir(this.informationEmise);
            }
            return;
        }

        // 1) Puissance moyenne du signal reçu (Ps)
        double sommeCarres = 0.0;
        for (int i = 0; i < n; i++) {
            float valeur = this.informationRecue.iemeElement(i);
            sommeCarres += valeur * valeur;
        }
        double ps = sommeCarres / n;

        // 2) sigma_b^2 déduit de Eb/N0 (formule du cours, Séance 3) :
        //    Eb/N0 = Ps * N / (2 * sigma_b^2)  =>  sigma_b^2 = Ps * N / (2 * 10^(EbN0dB/10))
        double ebN0Lineaire = Math.pow(10.0, ebN0Db / 10.0);
        double sigmaB2 = (ps * this.nbEch) / (2.0 * ebN0Lineaire);
        double sigmaB = Math.sqrt(sigmaB2);

        // 3) Génération du bruit gaussien et ajout au signal
        for (int i = 0; i < n; i++) {
            float valeur = this.informationRecue.iemeElement(i);
            double bruit = genererBruitGaussien(sigmaB);
            this.informationEmise.add((float) (valeur + bruit));
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
    private double genererBruitGaussien(double sigmaB) {
        double a1 = rand.nextDouble(); // dans [0,1[
        double a2 = rand.nextDouble(); // dans [0,1[
        return sigmaB * Math.sqrt(-2.0 * Math.log(1.0 - a1)) * Math.cos(2.0 * Math.PI * a2);
    }
}
