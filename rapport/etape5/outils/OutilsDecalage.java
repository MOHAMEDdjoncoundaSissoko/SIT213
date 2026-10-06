import information.Information;
import sources.SourceAleatoire;
import sources.SourceFixe;
import transmetteurs.CodeurCanal;
import transmetteurs.Transmetteur;
import transmetteurs.TransmetteurAnalogiqueBruite;
import transmetteurs.TransmetteurAnalogiqueParfait;
import transmetteurs.TransmetteurLogiqueAnalogique;
import visualisations.VueDecalage;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * Outils du rapport de l'étape 5 pour la désynchronisation (à lancer en mode headless).
 *
 *   capture fichier.png
 *       dessine, avec VueDecalage.tracer, la vue de désynchronisation pour plusieurs
 *       décalages (NRZ antipodal, Eb/N0 = 10 dB) et l'enregistre dans une image.
 *
 *   paquets forme aMin aMax ebN0Db|inf nbBits seed d1 d2 ...
 *       avec le codeur de canal, affiche pour chaque décalage d :
 *       d;proportion de paquets reçus qui ne sont pas des mots de code;TEB après décodage
 */
public class OutilsDecalage {

    private static final int NB_ECH = 30;

    public static void main(String[] a) throws Exception {
        if (a[0].equals("capture")) {
            capture(a[1]);
        } else {
            paquets(a);
        }
    }

    private static void capture(String fichier) throws Exception {
        int[] decalages = {0, 10, 20};
        int largeur = 960, hauteur = 300, legende = 24;
        int bande = hauteur + legende;
        BufferedImage planche = new BufferedImage(largeur, bande * decalages.length, BufferedImage.TYPE_INT_RGB);
        Graphics2D gp = planche.createGraphics();
        for (int c = 0; c < decalages.length; c++) {
            SourceFixe source = new SourceFixe("1011001110100110");
            TransmetteurLogiqueAnalogique emetteur = new TransmetteurLogiqueAnalogique("NRZ", NB_ECH, -1f, 1f);
            TransmetteurAnalogiqueBruite canal = new TransmetteurAnalogiqueBruite(NB_ECH, 10f, 3);
            source.connecter(emetteur);
            emetteur.connecter(canal);
            source.emettre();
            int d = decalages[c];
            Information<Boolean> decides =
                VueDecalage.decider(canal.getInformationEmise(), "NRZ", NB_ECH, -1f, 1f, d);

            BufferedImage image = new BufferedImage(largeur, bande, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, largeur, bande);
            VueDecalage.tracer(g, largeur, hauteur, canal.getInformationEmise(), emetteur.getInformationEmise(),
                emetteur.getInformationRecue(), decides, NB_ECH, -1f, 1f, d);
            g.setColor(Color.BLACK);
            g.drawString("decalage d = " + d + " echantillons ("
                + Math.round(100.0 * d / NB_ECH) + " % d'un bit) ; TEB sur ces 16 bits : "
                + VueDecalage.tauxErreur(source.getInformationEmise(), decides), 70, hauteur + 16);
            gp.drawImage(image, 0, bande * c, null);
            gp.setColor(Color.LIGHT_GRAY);
            gp.drawLine(0, bande * c, largeur, bande * c);
        }
        ImageIO.write(planche, "png", new File(fichier));
    }

    private static void paquets(String[] a) throws Exception {
        String forme = a[1];
        float aMin = Float.parseFloat(a[2]);
        float aMax = Float.parseFloat(a[3]);
        boolean sansBruit = a[4].equals("inf");
        int nbBits = Integer.parseInt(a[5]);
        int seed = Integer.parseInt(a[6]);

        SourceAleatoire source = new SourceAleatoire(nbBits, seed);
        CodeurCanal codeur = new CodeurCanal();
        TransmetteurLogiqueAnalogique emetteur = new TransmetteurLogiqueAnalogique(forme, NB_ECH, aMin, aMax);
        Transmetteur<Float, Float> canal = sansBruit
            ? new TransmetteurAnalogiqueParfait(NB_ECH, aMin, aMax)
            : new TransmetteurAnalogiqueBruite(NB_ECH, Float.parseFloat(a[4]), seed + 1);
        source.connecter(codeur);
        codeur.connecter(emetteur);
        emetteur.connecter(canal);
        source.emettre();

        for (int i = 7; i < a.length; i++) {
            int d = Integer.parseInt(a[i]);
            Information<Boolean> decides =
                VueDecalage.decider(canal.getInformationEmise(), forme, NB_ECH, aMin, aMax, d);
            int invalides = 0;
            int nbPaquets = decides.nbElements() / 3;
            for (int p = 0; p < nbPaquets; p++) {
                boolean b0 = decides.iemeElement(3 * p);
                boolean b1 = decides.iemeElement(3 * p + 1);
                boolean b2 = decides.iemeElement(3 * p + 2);
                // mots de code : 010 et 101, soit b1 different de b0 et b2 egal a b0
                if (b1 == b0 || b2 != b0) {
                    invalides++;
                }
            }
            float teb = VueDecalage.tauxErreur(source.getInformationEmise(), VueDecalage.decoder(decides));
            System.out.println(d + ";" + ((float) invalides / nbPaquets) + ";" + teb);
        }
    }
}
