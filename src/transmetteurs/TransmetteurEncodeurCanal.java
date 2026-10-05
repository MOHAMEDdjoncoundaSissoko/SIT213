package transmetteurs;

import destinations.DestinationInterface;
import information.Information;
import information.InformationNonConformeException;

/**
 * Encodeur de canal du TP5.
 * Transforme chaque bit logique en un mot de trois bits :
 * 0 -> 010 et 1 -> 101.
 */
public class TransmetteurEncodeurCanal extends Transmetteur<Boolean, Boolean> {

    @Override
    public void recevoir(Information<Boolean> information) throws InformationNonConformeException {
        this.informationRecue = information;
        emettre();
    }

    @Override
    public void emettre() throws InformationNonConformeException {
        this.informationEmise = new Information<Boolean>();

        for (Boolean bit : informationRecue) {
            if (bit) {
                informationEmise.add(true);
                informationEmise.add(false);
                informationEmise.add(true);
            } else {
                informationEmise.add(false);
                informationEmise.add(true);
                informationEmise.add(false);
            }
        }

        for (DestinationInterface<Boolean> dest : destinationsConnectees) {
            dest.recevoir(informationEmise);
        }
    }
}
