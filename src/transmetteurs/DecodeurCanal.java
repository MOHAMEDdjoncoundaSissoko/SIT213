package transmetteurs;

import destinations.DestinationInterface;
import information.Information;
import information.InformationNonConformeException;

/**
 * Décodeur de canal (étape 5) : chaque paquet de trois bits reçus redonne un
 * bit du message, en choisissant le mot de code (010 ou 101) le plus proche.
 *
 * Table de décodage : 000, 010, 011, 110 donnent 0 ; 001, 100, 101, 111
 * donnent 1. Une erreur sur un seul des trois bits est ainsi corrigée.
 *
 * Le mot le plus proche s'obtient par un vote majoritaire sur les trois bits,
 * après inversion de celui du milieu (que le codeur a inversé).
 * Les bits restants d'un paquet incomplet en fin de message sont ignorés.
 *
 * @author ziani
 * @author sissoko
 * @author nanda
 * @author blombou
 * @author bouaboud
 */
public class DecodeurCanal extends Transmetteur<Boolean, Boolean> {

    /** Construit un décodeur de canal. */
    public DecodeurCanal() {
        super();
    }

    @Override
    public void recevoir(Information<Boolean> information) throws InformationNonConformeException {
        this.informationRecue = information;
        emettre();
    }

    @Override
    public void emettre() throws InformationNonConformeException {
        informationEmise = new Information<Boolean>();
        int nbPaquets = informationRecue.nbElements() / CodeurCanal.LONGUEUR_MOT;

        for (int i = 0; i < nbPaquets; i++) {
            int debut = i * CodeurCanal.LONGUEUR_MOT;
            int votesPourUn = 0;
            if (informationRecue.iemeElement(debut)) votesPourUn++;
            if (!informationRecue.iemeElement(debut + 1)) votesPourUn++;
            if (informationRecue.iemeElement(debut + 2)) votesPourUn++;
            informationEmise.add(votesPourUn >= 2);
        }

        for (DestinationInterface<Boolean> dest : destinationsConnectees) {
            dest.recevoir(informationEmise);
        }
    }
}
