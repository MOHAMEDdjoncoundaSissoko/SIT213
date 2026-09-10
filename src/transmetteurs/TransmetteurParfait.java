package transmetteurs;

import destinations.DestinationInterface;
import information.Information;
import information.InformationNonConformeException;

/**
 * Transmetteur sans bruit : retransmet les bits reçus sans les modifier.
 * Correspond à un canal de transmission idéal (étape 1).
 *
 * @author ziani
 * @author sissoko
 * @author nanda
 */
public class TransmetteurParfait extends Transmetteur<Boolean, Boolean> {

    public TransmetteurParfait() {
        super();
    }

    @Override
    public void recevoir(Information<Boolean> information) throws InformationNonConformeException {
        this.informationRecue = information;
        emettre();
    }

    @Override
    public void emettre() throws InformationNonConformeException {
        this.informationEmise = this.informationRecue;
        for (DestinationInterface<Boolean> dest : destinationsConnectees) {
            dest.recevoir(informationEmise);
        }
    }
}
