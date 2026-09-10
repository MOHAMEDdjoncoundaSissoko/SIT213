package destinations;

import information.Information;
import information.InformationNonConformeException;

/**
 * Destination en bout de chaîne : stocke les bits reçus
 * pour permettre le calcul du TEB.
 *
 * @author ziani
 * @author sissoko
 * @author nanda
 */
public class DestinationFinale extends Destination<Boolean> {

    public DestinationFinale() {
        super();
    }

    @Override
    public void recevoir(Information<Boolean> information) throws InformationNonConformeException {
        this.informationRecue = information;
    }
}
