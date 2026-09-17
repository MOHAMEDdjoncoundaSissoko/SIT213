package transmetteurs;

import destinations.DestinationInterface;
import information.Information;
import information.InformationNonConformeException;

/**
 * Canal analogique parfait : transmet le signal Float sans modification.
 *
 * @author ziani
 * @author sissoko
 * @author nanda
 */
public class TransmetteurAnalogiqueParfait extends Transmetteur<Float, Float> {

    public TransmetteurAnalogiqueParfait(int nbEch, float aMin, float aMax) {
        super();
    }

    @Override
    public void recevoir(Information<Float> information) throws InformationNonConformeException {
        this.informationRecue = information;
        emettre();
    }

    @Override
    public void emettre() throws InformationNonConformeException {
        this.informationEmise = this.informationRecue;
        for (DestinationInterface<Float> dest : destinationsConnectees) {
            dest.recevoir(this.informationEmise);
        }
    }
}
