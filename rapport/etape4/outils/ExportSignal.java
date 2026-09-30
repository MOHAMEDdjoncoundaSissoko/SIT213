import information.Information;
import transmetteurs.TransmetteurAnalogiqueTrajetsMultiples;
import transmetteurs.TransmetteurLogiqueAnalogique;

/**
 * Exporte, pour les figures du rapport, le signal émis et le signal reçu
 * produits par les classes du simulateur (une ligne par échantillon : émis;reçu).
 *
 * Usage : java ExportSignal message forme nbEch aMin aMax ebN0Db|inf seed [dt ar ...]
 */
public class ExportSignal {

    public static void main(String[] args) throws Exception {
        String message = args[0];
        String forme = args[1];
        int nbEch = Integer.parseInt(args[2]);
        float aMin = Float.parseFloat(args[3]);
        float aMax = Float.parseFloat(args[4]);
        float ebN0 = args[5].equals("inf") ? Float.POSITIVE_INFINITY : Float.parseFloat(args[5]);
        int seed = Integer.parseInt(args[6]);
        int nbTrajets = (args.length - 7) / 2;
        int[] retards = new int[nbTrajets];
        float[] amplitudes = new float[nbTrajets];
        for (int k = 0; k < nbTrajets; k++) {
            retards[k] = Integer.parseInt(args[7 + 2 * k]);
            amplitudes[k] = Float.parseFloat(args[8 + 2 * k]);
        }

        Information<Boolean> bits = new Information<>();
        for (char c : message.toCharArray()) {
            bits.add(c == '1');
        }

        TransmetteurLogiqueAnalogique emetteur = new TransmetteurLogiqueAnalogique(forme, nbEch, aMin, aMax);
        TransmetteurAnalogiqueTrajetsMultiples canal =
            new TransmetteurAnalogiqueTrajetsMultiples(nbEch, ebN0, retards, amplitudes, seed);
        emetteur.connecter(canal);
        emetteur.recevoir(bits);

        Information<Float> emis = emetteur.getInformationEmise();
        Information<Float> recu = canal.getInformationEmise();
        StringBuilder sortie = new StringBuilder();
        for (int i = 0; i < emis.nbElements(); i++) {
            sortie.append(emis.iemeElement(i)).append(';').append(recu.iemeElement(i)).append('\n');
        }
        System.out.print(sortie);
    }
}
