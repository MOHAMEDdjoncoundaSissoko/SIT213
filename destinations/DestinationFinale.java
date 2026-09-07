package destinations;

import information.Information;
import information.InformationNonConformeException;

/**
 * Représente une destination finale de l'automate.
 * Elle reçoit une information de type Boolean et la mémorise.
 */
public class DestinationFinale extends Destination<Boolean> {

    /**
     * Constructeur de la destination finale.
     * Appelle le constructeur de la classe mère Destination.
     */
    public DestinationFinale() {
        super();
    }

    /**
     * Reçoit une information provenant de l'automate
     * et la stocke dans l'attribut informationRecue.
     *
     * @param information information reçue, de type Boolean
     * @throws InformationNonConformeException si l'information
     *         reçue n'est pas conforme
     */
    @Override
    public void recevoir(Information<Boolean> information) throws InformationNonConformeException {
        
        // Mémorise l'information reçue par la destination finale
        this.informationRecue = information;
    }
}