package visualisations;

import information.Information;
import information.InformationNonConformeException;
import transmetteurs.TransmetteurAnalogiqueLogique;
import transmetteurs.TransmetteurDecodeurCanal;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Fenetre de simulation de la desynchronisation recepteur.
 * Le curseur, ou la molette de la souris au-dessus du curseur, deplace la
 * fenetre de decision et recalcule le TEB obtenu.
 */
public class VueDesynchronisation extends Vue {

    private static final long serialVersionUID = 1917L;

    private Information<Float> signal;
    private Information<Boolean> reference;
    private String forme;
    private int nbEch;
    private float aMin;
    private float aMax;
    private boolean codageCanal;
    private JLabel resultat;
    private CourbeBitsPanel courbeBitsBruts;
    private CourbeBitsPanel courbeBitsRecus;

    public VueDesynchronisation(String nom, Information<Float> signal, Information<Boolean> reference,
                                String forme, int nbEch, float aMin, float aMax,
                                boolean codageCanal) {
        super(nom);
        this.signal = signal;
        this.reference = reference;
        this.forme = forme;
        this.nbEch = nbEch;
        this.aMin = aMin;
        this.aMax = aMax;
        this.codageCanal = codageCanal;

        int xPosition = Vue.getXPosition();
        int yPosition = Vue.getYPosition();
        setLocation(xPosition, yPosition);

        JSlider curseur = new JSlider(-(nbEch - 1), nbEch - 1, 0);
        curseur.setMajorTickSpacing(Math.max(1, nbEch / 2));
        curseur.setMinorTickSpacing(1);
        curseur.setPaintTicks(true);
        curseur.setPaintLabels(true);
        curseur.addMouseWheelListener(event -> {
            int nouvelleValeur = curseur.getValue() - event.getWheelRotation();
            curseur.setValue(Math.max(curseur.getMinimum(), Math.min(curseur.getMaximum(), nouvelleValeur)));
        });

        courbeBitsBruts = new CourbeBitsPanel();
        courbeBitsRecus = new CourbeBitsPanel();
        resultat = new JLabel("", SwingConstants.CENTER);
        resultat.setFont(resultat.getFont().deriveFont(Font.BOLD));
        mettreAJour(curseur.getValue());

        curseur.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent event) {
                mettreAJour(curseur.getValue());
            }
        });

        JPanel panneau = new JPanel(new BorderLayout());
        panneau.add(new JLabel("Decalage de la fenetre de decision (echantillons)", SwingConstants.CENTER),
            BorderLayout.NORTH);
        panneau.add(curseur, BorderLayout.CENTER);

        JPanel centre = new JPanel(new GridLayout(codageCanal ? 2 : 1, 1));
        if (codageCanal) {
            centre.add(panneauCourbe("Bits recus avant decodage canal", courbeBitsBruts));
        }
        centre.add(panneauCourbe("Bits remis a la destination", courbeBitsRecus));

        add(panneau, BorderLayout.NORTH);
        add(centre, BorderLayout.CENTER);
        add(resultat, BorderLayout.SOUTH);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(640, 320);
        setVisible(true);
    }

    public static Information<Boolean> recevoirBrutAvecDecalage(Information<Float> signal,
                                                                String forme, int nbEch, float aMin, float aMax,
                                                                int decalageFenetre)
        throws InformationNonConformeException {

        TransmetteurAnalogiqueLogique recepteur =
            new TransmetteurAnalogiqueLogique(forme, nbEch, aMin, aMax, decalageFenetre);
        recepteur.recevoir(signal);
        return recepteur.getInformationEmise();
    }

    public static Information<Boolean> recevoirAvecDecalage(Information<Float> signal,
                                                            String forme, int nbEch, float aMin, float aMax,
                                                            boolean codageCanal, int decalageFenetre)
        throws InformationNonConformeException {

        Information<Boolean> bitsRecus =
            recevoirBrutAvecDecalage(signal, forme, nbEch, aMin, aMax, decalageFenetre);

        if (codageCanal) {
            TransmetteurDecodeurCanal decodeur = new TransmetteurDecodeurCanal();
            decodeur.recevoir(bitsRecus);
            bitsRecus = decodeur.getInformationEmise();
        }

        return bitsRecus;
    }

    public static float calculerTebAvecDecalage(Information<Float> signal, Information<Boolean> reference,
                                                String forme, int nbEch, float aMin, float aMax,
                                                boolean codageCanal, int decalageFenetre)
        throws InformationNonConformeException {

        Information<Boolean> bitsRecus = recevoirAvecDecalage(signal, forme, nbEch, aMin, aMax,
            codageCanal, decalageFenetre);
        return calculerTeb(reference, bitsRecus);
    }

    private static float calculerTeb(Information<Boolean> reference, Information<Boolean> recue) {
        if (reference.nbElements() == 0) {
            return 0.0f;
        }

        int nbComparaisons = Math.min(reference.nbElements(), recue.nbElements());
        int erreurs = Math.abs(reference.nbElements() - recue.nbElements());
        for (int i = 0; i < nbComparaisons; i++) {
            if (!reference.iemeElement(i).equals(recue.iemeElement(i))) {
                erreurs++;
            }
        }
        return (float) erreurs / (float) reference.nbElements();
    }

    private void mettreAJour(int decalageFenetre) {
        try {
            Information<Boolean> bitsBruts = recevoirBrutAvecDecalage(signal, forme, nbEch, aMin, aMax,
                decalageFenetre);
            Information<Boolean> bitsRecus = recevoirAvecDecalage(signal, forme, nbEch, aMin, aMax,
                codageCanal, decalageFenetre);
            courbeBitsBruts.setValeurs(convertir(bitsBruts));
            courbeBitsRecus.setValeurs(convertir(bitsRecus));
            float teb = calculerTeb(reference, bitsRecus);
            resultat.setText("Decalage : " + decalageFenetre + " echantillons    TEB : " + teb);
        } catch (Exception e) {
            courbeBitsBruts.setValeurs(new boolean[0]);
            courbeBitsRecus.setValeurs(new boolean[0]);
            resultat.setText("Decalage : " + decalageFenetre + " echantillons    " + e.getMessage());
        }
    }

    private JPanel panneauCourbe(String titre, CourbeBitsPanel courbe) {
        JPanel panneau = new JPanel(new BorderLayout());
        panneau.add(new JLabel(titre, SwingConstants.CENTER), BorderLayout.NORTH);
        panneau.add(courbe, BorderLayout.CENTER);
        return panneau;
    }

    private static boolean[] convertir(Information<Boolean> information) {
        boolean[] valeurs = new boolean[information.nbElements()];
        for (int i = 0; i < information.nbElements(); i++) {
            valeurs[i] = information.iemeElement(i);
        }
        return valeurs;
    }

    private static class CourbeBitsPanel extends JPanel {
        private static final long serialVersionUID = 1917L;
        private boolean[] valeurs = new boolean[0];

        CourbeBitsPanel() {
            setPreferredSize(new Dimension(580, 140));
            setBackground(Color.WHITE);
        }

        void setValeurs(boolean[] valeurs) {
            this.valeurs = valeurs;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int marge = 16;
            int largeur = getWidth() - 2 * marge;
            int hauteur = getHeight() - 2 * marge;
            int yBas = marge + hauteur;
            int yHaut = marge;

            g.setColor(Color.BLACK);
            g.drawLine(marge, yBas, marge + largeur, yBas);
            g.drawLine(marge, yHaut, marge, yBas);

            if (valeurs.length == 0) {
                return;
            }

            float pas = largeur / (float) valeurs.length;
            g.setColor(new Color(0, 90, 180));
            int xPrecedent = marge;
            int yPrecedent = valeurs[0] ? yHaut : yBas;
            for (int i = 0; i < valeurs.length; i++) {
                int xDebut = marge + Math.round(i * pas);
                int xFin = marge + Math.round((i + 1) * pas);
                int y = valeurs[i] ? yHaut : yBas;
                g.drawLine(xPrecedent, yPrecedent, xDebut, y);
                g.drawLine(xDebut, y, xFin, y);
                xPrecedent = xFin;
                yPrecedent = y;
            }
        }
    }
}
