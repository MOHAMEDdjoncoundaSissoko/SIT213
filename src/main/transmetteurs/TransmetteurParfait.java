package transmetteurs;

import destinations.*;
import information.*;

/**
 * Transmetteur sans bruit : retransmet les bits reçus sans les modifier.
 * Correspond à un canal de transmission idéal (étape 1).
 *
 * @author ziani
 * @author sissoko
 * @author nanda
 * @author blombou
 * @author bouaboud
 */
public class TransmetteurParfait extends Transmetteur<Boolean, Boolean> {

    @Override
    public void recevoir(Information<Boolean> information) throws InformationNonConformeException {
        informationRecue = information;
        emettre();
    }

    @Override
    public void emettre() throws InformationNonConformeException {
        informationEmise = informationRecue;
        for (DestinationInterface<Boolean> dest : destinationsConnectees) {
            dest.recevoir(informationEmise);
        }
    }
}
