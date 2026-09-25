package transmetteurs;

import destinations.DestinationInterface;
import information.Information;
import information.InformationNonConformeException;

import java.util.Random;

/**
 * Canal à bruit blanc additif gaussien : r(n) = s(n) + b(n), b(n) ~ N(0, sigma_b^2).
 * sigma_b^2 est déduit de Eb/N0 = Ps * nbEch / (2 * sigma_b^2), avec Ps mesurée sur le signal reçu.
 *
 * @author ziani
 * @author sissoko
 * @author nanda
 * @author blombou
 * @author bouaboud
 */
public class TransmetteurAnalogiqueBruite extends Transmetteur<Float, Float> {

    private final int nbEch;
    private final float ebN0Db;
    private final Random rand;

    /**
     * @param nbEch   nombre d'échantillons par bit
     * @param ebN0Db  Eb/N0 en dB
     * @param seed    semence du générateur, ou null
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

        double sommeCarres = 0.0;
        for (int i = 0; i < n; i++) {
            float valeur = this.informationRecue.iemeElement(i);
            sommeCarres += valeur * valeur;
        }
        double ps = sommeCarres / n;

        double ebN0Lineaire = Math.pow(10.0, ebN0Db / 10.0);
        double sigmaB2 = (ps * this.nbEch) / (2.0 * ebN0Lineaire);
        double sigmaB = Math.sqrt(sigmaB2);

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
     * On utilise 1 - a1 car nextDouble() peut renvoyer 0 (ln(0) indéfini).
     * @param sigmaB écart-type du bruit
     * @return un échantillon de bruit
     */
    private double genererBruitGaussien(double sigmaB) {
        double a1 = rand.nextDouble();
        double a2 = rand.nextDouble();
        return sigmaB * Math.sqrt(-2.0 * Math.log(1.0 - a1)) * Math.cos(2.0 * Math.PI * a2);
    }
}
