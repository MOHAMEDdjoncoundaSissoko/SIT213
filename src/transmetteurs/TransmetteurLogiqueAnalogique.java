package transmetteurs;

import destinations.DestinationInterface;
import information.Information;
import information.InformationNonConformeException;

public class TransmetteurLogiqueAnalogique extends Transmetteur<Boolean, Float> {

    private String forme;      // "NRZ", "NRZT", "RZ"
    private int nbEch;         // échantillons par bit
    private float aMin, aMax;  // amplitudes

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
                        // actif seulement sur le tiers central
                        valeur = (e >= nbEch / 3 && e < 2 * nbEch / 3) ? niveau : aMin;
                        break;
                    case "NRZT":
                        // TODO : transition progressive sur T/3 selon bit précédent/suivant
                        valeur = niveau;
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