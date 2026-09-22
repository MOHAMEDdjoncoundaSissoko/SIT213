package simulateur;

import destinations.Destination;
import destinations.DestinationFinale;
import information.Information;
import sources.Source;
import sources.SourceAleatoire;
import sources.SourceFixe;
import transmetteurs.Transmetteur;
import transmetteurs.TransmetteurAnalogiqueLogique;
import transmetteurs.TransmetteurAnalogiqueParfait;
import transmetteurs.TransmetteurLogiqueAnalogique;
import transmetteurs.TransmetteurParfait;
import visualisations.SondeAnalogique;
import visualisations.SondeLogique;


/** La classe Simulateur permet de construire et simuler une chaîne de
 * transmission composée d'une Source, d'un nombre variable de
 * Transmetteur(s) et d'une Destination.
 * @author cousin
 * @author prou
 */
public class Simulateur {

    /** indique si le Simulateur utilise des sondes d'affichage */
    private boolean affichage = false;

    /** indique si le Simulateur utilise un message généré de manière aléatoire */
    private boolean messageAleatoire = true;

    /** indique si le Simulateur utilise un germe pour les générateurs aléatoires */
    private boolean aleatoireAvecGerme = false;

    /** la valeur de la semence utilisée pour les générateurs aléatoires */
    private Integer seed = null;

    /** la longueur du message aléatoire à transmettre */
    private int nbBitsMess = 100;

    /** la chaîne de caractères correspondant à m dans l'argument -mess m */
    private String messageString = "100";

    /** indique si la simulation est analogique (true) ou logique (false) */
    private boolean simulationAnalogique = false;

    /** la forme d'onde utilisée pour la transmission analogique */
    private String forme = "RZ";

    /** le nombre d'échantillons par bit */
    private int nbEch = 30;

    /** les amplitudes min et max du signal analogique */
    private float aMin = 0.0f;
    private float aMax = 1.0f;

    /** le composant Source de la chaine de transmission */
    private Source<Boolean> source = null;

    /** le composant Transmetteur logique de la chaine de transmission */
    private Transmetteur<Boolean, Boolean> transmetteurLogique = null;

    /** le composant Destination de la chaine de transmission */
    private Destination<Boolean> destination = null;


    /** Le constructeur de Simulateur construit une chaîne de
     * transmission composée d'une Source de Boolean, d'une Destination
     * de Boolean et de Transmetteur(s) [voir la méthode analyseArguments].
     * Les différents composants sont créés et connectés.
     * @param args le tableau des différents arguments.
     * @throws ArgumentsException si un des arguments est incorrect
     */
    public Simulateur(String[] args) throws ArgumentsException {
        analyseArguments(args);

        if (messageAleatoire) {
            if (aleatoireAvecGerme) {
                source = new SourceAleatoire(nbBitsMess, seed);
            } else {
                source = new SourceAleatoire(nbBitsMess);
            }
        } else {
            source = new SourceFixe(messageString);
        }

        destination = new DestinationFinale();

        if (!simulationAnalogique) {
            transmetteurLogique = new TransmetteurParfait();
            source.connecter(transmetteurLogique);
            transmetteurLogique.connecter(destination);

            if (affichage) {
                source.connecter(new SondeLogique("Emetteur", 10));
                transmetteurLogique.connecter(new SondeLogique("Recepteur", 10));
            }
        } else {
            TransmetteurLogiqueAnalogique emetteur = new TransmetteurLogiqueAnalogique(forme, nbEch, aMin, aMax);
            TransmetteurAnalogiqueParfait canal     = new TransmetteurAnalogiqueParfait(nbEch, aMin, aMax);
            TransmetteurAnalogiqueLogique recepteur = new TransmetteurAnalogiqueLogique(nbEch, aMin, aMax);

            source.connecter(emetteur);
            emetteur.connecter(canal);
            canal.connecter(recepteur);
            recepteur.connecter(destination);

            if (affichage) {
                emetteur.connecter(new SondeAnalogique("Signal emis"));
                canal.connecter(new SondeAnalogique("Signal recu"));
            }
        }
    }


    /** La méthode analyseArguments extrait d'un tableau de chaînes de
     * caractères les différentes options de la simulation.
     * @param args le tableau des différents arguments.
     * @throws ArgumentsException si un des arguments est incorrect.
     */
    private void analyseArguments(String[] args) throws ArgumentsException {

        for (int i = 0; i < args.length; i++) {

            if (args[i].matches("-s")) {
                affichage = true;

            } else if (args[i].matches("-seed")) {
                aleatoireAvecGerme = true;
                i++;
                try {
                    seed = Integer.valueOf(args[i]);
                } catch (Exception e) {
                    throw new ArgumentsException("Valeur du parametre -seed invalide : " + args[i]);
                }

            } else if (args[i].matches("-mess")) {
                i++;
                messageString = args[i];
                if (args[i].matches("[01]{7,}")) {
                    messageAleatoire = false;
                    nbBitsMess = args[i].length();
                } else if (args[i].matches("[0-9]{1,6}")) {
                    messageAleatoire = true;
                    nbBitsMess = Integer.valueOf(args[i]);
                    if (nbBitsMess < 1)
                        throw new ArgumentsException("Valeur du parametre -mess invalide : " + nbBitsMess);
                } else {
                    throw new ArgumentsException("Valeur du parametre -mess invalide : " + args[i]);
                }

            } else if (args[i].matches("-form")) {
                simulationAnalogique = true;
                i++;
                forme = args[i];

            } else if (args[i].matches("-nbEch")) {
                simulationAnalogique = true;
                i++;
                try {
                    nbEch = Integer.parseInt(args[i]);
                } catch (NumberFormatException e) {
                    throw new ArgumentsException("Valeur du parametre -nbEch invalide : " + args[i]);
                }

                // VALIDATION DU PROF : Il faut au moins NB_ECH_MIN échantillons pour avoir une forme propre
                // (le message précis n'est plus avalé par un catch(Exception) générique)
                if (nbEch < TransmetteurLogiqueAnalogique.NB_ECH_MIN) {
                    throw new ArgumentsException("Valeur du parametre -nbEch invalide : " + nbEch
                        + " (minimum " + TransmetteurLogiqueAnalogique.NB_ECH_MIN + " pour la lisibilité)");
                }
            } else if (args[i].matches("-ampl")) {
                simulationAnalogique = true;
                try {
                    i++;
                    aMin = Float.parseFloat(args[i]);
                    i++;
                    aMax = Float.parseFloat(args[i]);
                } catch (Exception e) {
                    throw new ArgumentsException("Valeurs du parametre -ampl invalides");
                }

            } else {
                throw new ArgumentsException("Option invalide : " + args[i]);
            }
        }
    }


    /** La méthode execute effectue un envoi de message par la source.
     * @throws Exception si un problème survient lors de l'exécution
     */
    public void execute() throws Exception {
        source.emettre();
    }


    /** Calcule le taux d'erreur binaire en comparant les bits émis et reçus.
     * @return la valeur du TEB
     */
    public float calculTauxErreurBinaire() {
        Information<Boolean> emis = source.getInformationEmise();
        Information<Boolean> recu = destination.getInformationRecue();

        int nbBits = emis.nbElements();
        int erreurs = 0;
        for (int i = 0; i < nbBits; i++) {
            if (!emis.iemeElement(i).equals(recu.iemeElement(i))) {
                erreurs++;
            }
        }
        return (float) erreurs / (float) nbBits;
    }


    /** La fonction main instancie un Simulateur et affiche le TEB.
     * @param args les arguments de simulation.
     */
    public static void main(String[] args) {
        Simulateur simulateur = null;

        try {
            simulateur = new Simulateur(args);
        } catch (Exception e) {
            System.err.println(e);
            System.exit(1);
        }

        try {
            simulateur.execute();
            String s = "java  Simulateur  ";
            for (int i = 0; i < args.length; i++) {
                s += args[i] + "  ";
            }
            System.out.println(s + "  =>   TEB : " + simulateur.calculTauxErreurBinaire());
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
            System.exit(-2);
        }
    }
}
