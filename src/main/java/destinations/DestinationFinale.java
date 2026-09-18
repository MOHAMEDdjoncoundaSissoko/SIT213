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
 * @author blombou
 * @author bouaboud
 */
public class DestinationFinale extends Destination<Boolean> {
    /**
    * @param information information binaire reçue
    */
    @Override
    public void recevoir(Information<Boolean> information) throws InformationNonConformeException {
        informationRecue = information;
    }
}
