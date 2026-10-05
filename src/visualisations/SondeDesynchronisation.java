package visualisations;

import information.Information;
import sources.Source;

/**
 * Sonde d'analyse de la desynchronisation du recepteur analogique.
 */
public class SondeDesynchronisation extends Sonde<Float> {

    private Source<Boolean> source;
    private String forme;
    private int nbEch;
    private float aMin;
    private float aMax;
    private boolean codageCanal;

    public SondeDesynchronisation(String nom, Source<Boolean> source, String forme, int nbEch,
                                  float aMin, float aMax, boolean codageCanal) {
        super(nom);
        this.source = source;
        this.forme = forme;
        this.nbEch = nbEch;
        this.aMin = aMin;
        this.aMax = aMax;
        this.codageCanal = codageCanal;
    }

    @Override
    public void recevoir(Information<Float> information) {
        informationRecue = information;
        Information<Boolean> reference = source.getInformationEmise();
        if (reference != null) {
            new VueDesynchronisation(nom, information, reference, forme, nbEch, aMin, aMax,
                codageCanal);
        }
    }
}
