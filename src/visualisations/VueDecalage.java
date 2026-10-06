package visualisations;

import information.Information;
import information.InformationNonConformeException;
import transmetteurs.DecodeurCanal;
import transmetteurs.TransmetteurAnalogiqueLogique;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;

/**
 * Fenêtre interactive de désynchronisation du récepteur (options -s et -decalage).
 *
 * Un curseur (ou la molette de la souris) fait varier le décalage d de la
 * fenêtre de décision, de -(nbEch - 1) à nbEch - 1 échantillons. Pour chaque
 * valeur, le signal reçu est de nouveau décidé par un récepteur à filtre adapté
 * décalé de d (puis décodé si le codage de canal est actif), et :
 * <ul>
 *   <li>les premiers bits sont tracés : signal émis, signal reçu, limites des
 *       bits, fenêtres de décision, fenêtres mal décidées sur fond rouge ;</li>
 *   <li>le TEB du message complet est recalculé et affiché.</li>
 * </ul>
 *
 * @author ziani
 * @author sissoko
 * @author nanda
 * @author blombou
 * @author bouaboud
 */
public class VueDecalage extends Vue {

    private static final long serialVersionUID = 1917L;

    /** nombre de bits tracés (le TEB porte toujours sur le message complet) */
    private static final int NB_BITS_TRACES = 16;

    /** signal en sortie du canal */
    private final Information<Float> signalRecu;
    /** signal en sortie de l'émetteur */
    private final Information<Float> signalEmis;
    /** bits émis sur le canal (bits codés avec le codage de canal) */
    private final Information<Boolean> bitsEmis;
    /** message de la source, référence du TEB */
    private final Information<Boolean> message;
    /** forme d'onde */
    private final String forme;
    /** nombre d'échantillons par bit */
    private final int nbEch;
    /** amplitude du bit 0 */
    private final float aMin;
    /** amplitude du bit 1 */
    private final float aMax;
    /** true si le message a été codé (option -codeur) */
    private final boolean codageCanal;

    /** décalage courant de la fenêtre de décision, en échantillons */
    private int decalage;
    /** bits émis tels que décidés par le récepteur décalé */
    private Information<Boolean> bitsDecides = new Information<>();

    /** ligne de résultat : décalage et TEB */
    private final JLabel resultat = new JLabel(" ", SwingConstants.CENTER);
    /** tracé des signaux et des fenêtres */
    private final Trace trace = new Trace();

    /**
     * Construit et affiche la fenêtre.
     * @param signalRecu       signal en sortie du canal
     * @param signalEmis       signal en sortie de l'émetteur (tracé pour comparaison)
     * @param bitsEmis         bits émis sur le canal (bits codés avec le codage de canal)
     * @param message          message de la source, référence du TEB
     * @param forme            forme d'onde ("NRZ", "NRZT" ou "RZ")
     * @param nbEch            nombre d'échantillons par bit
     * @param aMin             amplitude du bit 0
     * @param aMax             amplitude du bit 1
     * @param codageCanal      true si le message a été codé (option -codeur)
     * @param decalageInitial  décalage affiché à l'ouverture, en échantillons
     */
    public VueDecalage(Information<Float> signalRecu, Information<Float> signalEmis,
                       Information<Boolean> bitsEmis, Information<Boolean> message,
                       String forme, int nbEch, float aMin, float aMax,
                       boolean codageCanal, int decalageInitial) {
        super("Desynchronisation du recepteur");
        this.signalRecu = signalRecu;
        this.signalEmis = signalEmis;
        this.bitsEmis = bitsEmis;
        this.message = message;
        this.forme = forme;
        this.nbEch = nbEch;
        this.aMin = aMin;
        this.aMax = aMax;
        this.codageCanal = codageCanal;
        setLocation(Vue.getXPosition(), Vue.getYPosition());

        JSlider curseur = new JSlider(-(nbEch - 1), nbEch - 1, decalageInitial);
        curseur.setMajorTickSpacing(Math.max(1, nbEch / 3));
        curseur.setMinorTickSpacing(1);
        curseur.setPaintTicks(true);
        curseur.setPaintLabels(true);
        curseur.addChangeListener(evenement -> mettreAJour(curseur.getValue()));
        curseur.addMouseWheelListener(evenement ->
            curseur.setValue(curseur.getValue() - evenement.getWheelRotation()));

        JPanel haut = new JPanel(new BorderLayout());
        haut.add(new JLabel("Decalage de la fenetre de decision du recepteur (echantillons) :"
            + " curseur ou molette", SwingConstants.CENTER), BorderLayout.NORTH);
        haut.add(curseur, BorderLayout.CENTER);
        add(haut, BorderLayout.NORTH);
        add(trace, BorderLayout.CENTER);
        add(resultat, BorderLayout.SOUTH);

        mettreAJour(decalageInitial);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(980, 470);
        setVisible(true);
    }

    /**
     * Bits décidés par un récepteur à filtre adapté dont la fenêtre est décalée.
     * @param signalRecu  signal reçu
     * @param forme       forme d'onde
     * @param nbEch       nombre d'échantillons par bit
     * @param aMin        amplitude du bit 0
     * @param aMax        amplitude du bit 1
     * @param decalage    décalage de la fenêtre, en échantillons
     * @return les bits décidés
     * @throws InformationNonConformeException si le signal est mal formé
     */
    public static Information<Boolean> decider(Information<Float> signalRecu, String forme, int nbEch,
                                               float aMin, float aMax, int decalage)
        throws InformationNonConformeException {
        TransmetteurAnalogiqueLogique recepteur =
            new TransmetteurAnalogiqueLogique(forme, nbEch, aMin, aMax, decalage);
        recepteur.recevoir(signalRecu);
        return recepteur.getInformationEmise();
    }

    /**
     * Message obtenu après décodage de canal des bits décidés.
     * @param bitsDecides  bits décidés (bits codés)
     * @return le message décodé
     * @throws InformationNonConformeException si les bits sont mal formés
     */
    public static Information<Boolean> decoder(Information<Boolean> bitsDecides)
        throws InformationNonConformeException {
        DecodeurCanal decodeur = new DecodeurCanal();
        decodeur.recevoir(bitsDecides);
        return decodeur.getInformationEmise();
    }

    /**
     * Proportion de bits différents, rang par rang ; un bit absent compte comme une erreur.
     * @param reference  bits de référence
     * @param recue      bits à comparer
     * @return le taux d'erreur, rapporté au nombre de bits de référence
     */
    public static float tauxErreur(Information<Boolean> reference, Information<Boolean> recue) {
        if (reference.nbElements() == 0) {
            return 0.0f;
        }
        int erreurs = 0;
        for (int i = 0; i < reference.nbElements(); i++) {
            if (i >= recue.nbElements() || !reference.iemeElement(i).equals(recue.iemeElement(i))) {
                erreurs++;
            }
        }
        return (float) erreurs / (float) reference.nbElements();
    }

    /** Décide de nouveau le signal avec le décalage donné, met à jour le TEB et le tracé. */
    private void mettreAJour(int nouveauDecalage) {
        decalage = nouveauDecalage;
        try {
            bitsDecides = decider(signalRecu, forme, nbEch, aMin, aMax, decalage);
            Information<Boolean> messageRecu = codageCanal ? decoder(bitsDecides) : bitsDecides;
            String texte = "Decalage : " + decalage + " echantillon(s), soit "
                + Math.round(100.0 * decalage / nbEch) + " % d'un bit   -   TEB : "
                + tauxErreur(message, messageRecu);
            if (codageCanal) {
                texte += "   (bits codes avant decodage : " + tauxErreur(bitsEmis, bitsDecides) + ")";
            }
            resultat.setText(texte);
        } catch (InformationNonConformeException e) {
            bitsDecides = new Information<>();
            resultat.setText("Decalage : " + decalage + " - " + e.getMessage());
        }
        trace.repaint();
    }

    /** Zone de tracé de la fenêtre. */
    private class Trace extends JPanel {

        private static final long serialVersionUID = 1917L;

        Trace() {
            setPreferredSize(new Dimension(960, 330));
            setBackground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics graphique) {
            super.paintComponent(graphique);
            tracer((Graphics2D) graphique, getWidth(), getHeight(), signalRecu, signalEmis, bitsEmis,
                bitsDecides, nbEch, aMin, aMax, decalage);
        }
    }

    /** marges du tracé, en pixels */
    private static final int MARGE_GAUCHE = 70;
    private static final int MARGE_DROITE = 20;
    private static final int MARGE_HAUTE = 46;
    private static final int MARGE_BASSE = 34;

    /**
     * Dessine les premiers bits : signal émis et reçu, limites des bits, fenêtres
     * de décision décalées, bits émis et décidés (fond rouge : bit mal décidé).
     * Utilisée par la fenêtre ; utilisable aussi pour dessiner dans une image.
     * @param g            surface de dessin
     * @param largeurTotale largeur disponible, en pixels
     * @param hauteurTotale hauteur disponible, en pixels
     * @param signalRecu   signal en sortie du canal
     * @param signalEmis   signal en sortie de l'émetteur
     * @param bitsEmis     bits émis sur le canal
     * @param bitsDecides  bits décidés par le récepteur décalé
     * @param nbEch        nombre d'échantillons par bit
     * @param aMin         amplitude du bit 0
     * @param aMax         amplitude du bit 1
     * @param decalage     décalage de la fenêtre de décision, en échantillons
     */
    public static void tracer(Graphics2D g, int largeurTotale, int hauteurTotale,
                              Information<Float> signalRecu, Information<Float> signalEmis,
                              Information<Boolean> bitsEmis, Information<Boolean> bitsDecides,
                              int nbEch, float aMin, float aMax, int decalage) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int nbBits = Math.min(NB_BITS_TRACES, Math.min(bitsEmis.nbElements(), bitsDecides.nbElements()));
        int nbPoints = Math.min(nbBits * nbEch, Math.min(signalRecu.nbElements(), signalEmis.nbElements()));
        if (nbBits == 0 || nbPoints == 0) {
            return;
        }
        int largeur = largeurTotale - MARGE_GAUCHE - MARGE_DROITE;
        int hauteur = hauteurTotale - MARGE_HAUTE - MARGE_BASSE;

        // échelle verticale : amplitudes et extrêmes du signal reçu tracé
        float min = Math.min(aMin, aMax);
        float max = Math.max(aMin, aMax);
        for (int j = 0; j < nbPoints; j++) {
            min = Math.min(min, signalRecu.iemeElement(j));
            max = Math.max(max, signalRecu.iemeElement(j));
        }
        float ecart = (max > min) ? (max - min) : 1.0f;
        double pixelsParEchantillon = largeur / (double) nbPoints;

        // fenêtres de décision mal décidées : fond rouge
        g.setColor(new Color(255, 210, 210));
        for (int i = 0; i < nbBits; i++) {
            if (!bitsDecides.iemeElement(i).equals(bitsEmis.iemeElement(i))) {
                int debut = Math.max(0, i * nbEch + decalage);
                int fin = Math.min(nbPoints, (i + 1) * nbEch + decalage);
                if (fin > debut) {
                    int x1 = abscisse(debut, pixelsParEchantillon);
                    g.fillRect(x1, MARGE_HAUTE, abscisse(fin, pixelsParEchantillon) - x1, hauteur);
                }
            }
        }

        // limites des bits émis (gris, pointillés) et des fenêtres de décision (rouge)
        Stroke trait = g.getStroke();
        g.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f,
            new float[] {4f, 4f}, 0f));
        g.setColor(Color.GRAY);
        for (int i = 0; i <= nbBits; i++) {
            int x = abscisse(i * nbEch, pixelsParEchantillon);
            g.drawLine(x, MARGE_HAUTE, x, MARGE_HAUTE + hauteur);
        }
        g.setStroke(new BasicStroke(1.5f));
        g.setColor(new Color(200, 30, 30));
        for (int i = 0; i <= nbBits; i++) {
            int limite = i * nbEch + decalage;
            if (limite >= 0 && limite <= nbPoints) {
                int x = abscisse(limite, pixelsParEchantillon);
                g.drawLine(x, MARGE_HAUTE, x, MARGE_HAUTE + hauteur);
            }
        }

        // signal reçu (bleu), puis signal émis par-dessus (gris semi-transparent, visible malgré le bruit)
        tracerSignal(g, signalRecu, nbPoints, pixelsParEchantillon, min, ecart, hauteur,
            new Color(30, 90, 200), 1.2f);
        tracerSignal(g, signalEmis, nbPoints, pixelsParEchantillon, min, ecart, hauteur,
            new Color(60, 60, 60, 150), 2f);
        g.setStroke(trait);

        // bits émis au-dessus de chaque bit, bits décidés sous chaque fenêtre
        g.setColor(Color.BLACK);
        g.drawString("emis :", 8, MARGE_HAUTE - 10);
        g.drawString("decide :", 8, MARGE_HAUTE + hauteur + 22);
        for (int i = 0; i < nbBits; i++) {
            g.setColor(Color.BLACK);
            int xBit = abscisse(i * nbEch + nbEch / 2, pixelsParEchantillon) - 3;
            g.drawString(bitsEmis.iemeElement(i) ? "1" : "0", xBit, MARGE_HAUTE - 10);
            boolean juste = bitsDecides.iemeElement(i).equals(bitsEmis.iemeElement(i));
            g.setColor(juste ? new Color(0, 120, 0) : new Color(200, 30, 30));
            int xFenetre = abscisse(Math.max(0, Math.min(nbPoints, i * nbEch + decalage + nbEch / 2)),
                pixelsParEchantillon) - 3;
            g.drawString(bitsDecides.iemeElement(i) ? "1" : "0", xFenetre, MARGE_HAUTE + hauteur + 22);
        }

        g.setColor(Color.DARK_GRAY);
        g.drawString("gris : signal emis   bleu : signal recu   rouge : fenetres de decision"
            + " (fond rouge : bit mal decide)   " + nbBits + " premiers bits", MARGE_GAUCHE, 16);
    }

    private static int abscisse(int echantillon, double pixelsParEchantillon) {
        return MARGE_GAUCHE + (int) Math.round(echantillon * pixelsParEchantillon);
    }

    private static void tracerSignal(Graphics2D g, Information<Float> signal, int nbPoints,
                                     double pixelsParEchantillon, float min, float ecart, int hauteur,
                                     Color couleur, float epaisseur) {
        g.setColor(couleur);
        g.setStroke(new BasicStroke(epaisseur));
        int xPrecedent = abscisse(0, pixelsParEchantillon);
        int yPrecedent = ordonnee(signal.iemeElement(0), min, ecart, hauteur);
        for (int j = 1; j < nbPoints; j++) {
            int x = abscisse(j, pixelsParEchantillon);
            int y = ordonnee(signal.iemeElement(j), min, ecart, hauteur);
            g.drawLine(xPrecedent, yPrecedent, x, y);
            xPrecedent = x;
            yPrecedent = y;
        }
    }

    private static int ordonnee(float valeur, float min, float ecart, int hauteur) {
        return MARGE_HAUTE + (int) Math.round((1.0 - (valeur - min) / ecart) * hauteur);
    }
}
