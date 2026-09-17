package transmetteurs;

import destinations.DestinationInterface;
import information.Information;
import information.InformationNonConformeException;

public class TransmetteurAnalogiqueParfait extends Transmetteur<Float, Float> {

    // Le canal parfait n'a pas besoin de stocker nbEch ou le seuil 
    // pour fonctionner, mais on garde le constructeur avec arguments 
    // pour ne pas casser l'appel dans Simulateur.java
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
        // Un canal parfait analogique transmet l'information telle quelle 
        // sans modification (pas de seuil, pas de transformation en Boolean).
        this.informationEmise = this.informationRecue;

        for (DestinationInterface<Float> dest : destinationsConnectees) {
            dest.recevoir(this.informationEmise);
        }
    }
}