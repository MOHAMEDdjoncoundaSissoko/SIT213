package sources;

import information.Information;

/**
 * Une Source qui émet un message binaire imposé (fourni sous forme
 * d'une chaîne de caractères composée uniquement de '0' et de '1').
*/

public class SourceFixe extends Source<Boolean> {

    /**
     * construit une SourceFixe à partir du message binaire à émettre.
     * @param messageBinaire une suite de '0' et de '1'
     */

    public SourceFixe(String messageBinaire) {
        super();

        // On crée une nouvelle Information pour stocker le message binaire
        informationGeneree = new Information<Boolean>();

        // Parcourt chaque caractère du message binaire
        for (int i = 0; i < messageBinaire.length(); i++) {

            // Convertit '1' en true et '0' en false, puis ajoute le résultat
            informationGeneree.add(messageBinaire.charAt(i) == '1');
        }
    }
    
}
