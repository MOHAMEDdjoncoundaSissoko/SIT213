package transmetteurs;

import destinations.DestinationInterface;
import information.Information;
import information.InformationNonConformeException;

/**
 * Decodeur de canal du TP5.
 * Regroupe les bits recus par paquets de trois et corrige un bit errone
 * en choisissant le mot valide le plus proche : 010 ou 101.
 */
public class TransmetteurDecodeurCanal extends Transmetteur<Boolean, Boolean> {

    @Override
    public void recevoir(Information<Boolean> information) throws InformationNonConformeException {
        this.informationRecue = information;
        emettre();
    }

    @Override
    public void emettre() throws InformationNonConformeException {
        if (informationRecue.nbElements() % 3 != 0) {
            throw new InformationNonConformeException(
                "Le decodeur de canal attend des paquets complets de 3 bits");
        }

        this.informationEmise = new Information<Boolean>();
        for (int i = 0; i < informationRecue.nbElements(); i += 3) {
            boolean b0 = informationRecue.iemeElement(i);
            boolean b1 = informationRecue.iemeElement(i + 1);
            boolean b2 = informationRecue.iemeElement(i + 2);
            informationEmise.add(decoderTriplet(b0, b1, b2));
        }

        for (DestinationInterface<Boolean> dest : destinationsConnectees) {
            dest.recevoir(informationEmise);
        }
    }

    private boolean decoderTriplet(boolean b0, boolean b1, boolean b2) {
        int distanceAvecUn = (b0 ? 0 : 1) + (b1 ? 1 : 0) + (b2 ? 0 : 1);
        int distanceAvecZero = (b0 ? 1 : 0) + (b1 ? 0 : 1) + (b2 ? 1 : 0);
        return distanceAvecUn < distanceAvecZero;
    }
}
