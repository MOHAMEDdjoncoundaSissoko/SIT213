package transmetteurs;

import destinations.DestinationInterface;
import information.Information;
import information.InformationNonConformeException;

public class TransmetteurParfait extends Transmetteur<Boolean, Boolean> {

    public TransmetteurParfait() {
        super();
    }

    @Override
    public void recevoir(Information<Boolean> information) throws InformationNonConformeException {

        // Stocke l'information reçue
        this.informationRecue = information;

        // Transmet l'information reçue aux destinations
        emettre();
    }

    @Override
    public void emettre() throws InformationNonConformeException {

        // L'information émise est identique à l'information reçue
        this.informationEmise = this.informationRecue;

        // Envoie l'information à toutes les destinations connectées
        for (DestinationInterface<Boolean> destinationConnectee : destinationsConnectees) {

            // Transmet l'information à la destination
            destinationConnectee.recevoir(informationEmise);
        }
    }
}