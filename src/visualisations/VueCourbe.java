package visualisations;
/**
 * @author B. Prou
 * Updated by E. Cousin - 2021
 *
 */

import java.awt.*;
import java.awt.geom.*;

public class VueCourbe  extends Vue {

    private static final long serialVersionUID = 1917L;

    private Point2D.Float [] coordonnees;
    private float yMax = 0;
    private float yMin = 0;
    private float pixelsParUniteX = 1.0f;
    private float decalageX = 0.0f;


    public  VueCourbe (boolean [] valeurs, int nbPixels, String nom) {

	super(nom);

	int xPosition = Vue.getXPosition();
	int yPosition = Vue.getYPosition();
	setLocation(xPosition, yPosition);

	this.coordonnees = new Point2D.Float [(2 * valeurs.length) + 1];
	this.pixelsParUniteX = nbPixels;
	this.decalageX = 0.0f;
	yMax = 1;
	yMin = 0;

	coordonnees[0] = new Point2D.Float(0, 0);

	for (int i = 0, j = 0; i < valeurs.length; i++, j+= 2) {
            if (valeurs[i]) {
		coordonnees[j+1] = new Point2D.Float(i, 1);
		coordonnees[j+2] = new Point2D.Float(i+1, 1);
            }
            else {
		coordonnees[j+1] = new Point2D.Float(i, 0);
		coordonnees[j+2] = new Point2D.Float(i+1, 0);
            }
	}

	setDefaultCloseOperation(EXIT_ON_CLOSE);
	int largeur = (valeurs.length * nbPixels) + 10;
	if (largeur > 1000)
            largeur = 1000;
	setSize(largeur, 200);
	activerMolette();
	setVisible(true);
	repaint();
    }


    public  VueCourbe (float [] valeurs, String nom) {

	super(nom);

	int xPosition = Vue.getXPosition();
	int yPosition = Vue.getYPosition();
	setLocation(xPosition, yPosition);

	this.coordonnees = new Point2D.Float [valeurs.length];
	this.pixelsParUniteX = 8.0f;
	this.decalageX = 0.0f;
	yMax = 0;
	yMin = 0;

	for (int i = 0; i < valeurs.length; i++) {
            if (valeurs[i] > yMax)
		yMax = valeurs[i];
            if (valeurs[i] < yMin)
		yMin = valeurs[i];
            coordonnees[i] = new Point2D.Float(i, valeurs[i]);
	}

	setDefaultCloseOperation(EXIT_ON_CLOSE);
	int largeur = (int) (valeurs.length * pixelsParUniteX) + 10;
	if (largeur > 1000)
            largeur = 1000;
	setSize(largeur, 200);
	activerMolette();
	setVisible(true);
	repaint();
    }


    public  void changer (boolean [] valeurs) {

	this.coordonnees = new Point2D.Float [(2 * valeurs.length) + 1];
	yMax = 1;
	yMin = 0;

	coordonnees[0] = new Point2D.Float(0, 0);

	for (int i = 0, j = 0; i < valeurs.length; i++, j+= 2) {
            if (valeurs[i]) {
		coordonnees[j+1] = new Point2D.Float(i, 1);
		coordonnees[j+2] = new Point2D.Float(i+1, 1);
            }
            else {
		coordonnees[j+1] = new Point2D.Float(i, 0);
		coordonnees[j+2] = new Point2D.Float(i+1, 0);
            }
	}

	this.decalageX = limiterDecalage(decalageX);
	paint();
    }

    public  void changer (float [] valeurs) {

	this.coordonnees = new Point2D.Float [valeurs.length];
	yMax = 0;
	yMin = 0;

	for (int i = 0; i < valeurs.length; i++) {
            if (valeurs[i] > yMax)
		yMax = valeurs[i];
            if (valeurs[i] < yMin)
		yMin = valeurs[i];
            coordonnees[i] = new Point2D.Float(i, valeurs[i]);
	}

	this.decalageX = limiterDecalage(decalageX);
	paint();
    }


    public void paint() {
	paint(getGraphics());
    }


    public void paint(Graphics g) {
	if (g == null) {
            return;
	}
	if ((coordonnees == null) || (coordonnees.length == 0)) {
            return;
	}
	g.setColor(Color.white);
	g.fillRect(0, 0, getWidth(), getHeight());
	g.setColor(Color.black);

	int x0Axe = 10;
	float deltaX = getContentPane().getWidth() - (2 * x0Axe);

	int y0Axe = 10;
	float deltaY = getContentPane().getHeight() - (2 * y0Axe);

	if ((yMax > 0) && (yMin <= 0)) {
            y0Axe += (int) (deltaY * (yMax / (yMax - yMin)));
	}
	else if ((yMax > 0) && (yMin > 0)) {
            y0Axe += deltaY;
	}
	else if (yMax <= 0) {
            y0Axe += 0;
	}
	// Contexte graphique récupéré une seule fois : un appel par drawLine est très lent sur les longs messages.
	Graphics gc = getContentPane().getGraphics();
	if (gc == null) {
            return;
	}

	gc.drawLine(x0Axe, y0Axe, x0Axe + (int) deltaX + x0Axe, y0Axe);
	gc.drawLine(x0Axe + (int) deltaX + x0Axe - 5, y0Axe - 5, x0Axe + (int) deltaX + x0Axe, y0Axe);
	gc.drawLine(x0Axe + (int) deltaX + x0Axe - 5, y0Axe + 5, x0Axe + (int) deltaX + x0Axe, y0Axe);

	gc.drawLine(x0Axe, y0Axe, x0Axe, y0Axe - (int) deltaY - y0Axe);
	gc.drawLine(x0Axe + 5, 5, x0Axe, 0);
	gc.drawLine(x0Axe - 5, 5, x0Axe, 0);

	decalageX = limiterDecalage(decalageX);
	float dx = pixelsParUniteX;
	float dy = 0.0f;
	if ((yMax >= 0) && (yMin <= 0)) {
            dy =  deltaY / (yMax-yMin);
	}
	else if (yMin > 0) {
            dy =  deltaY / yMax;
	}
	else if (yMax < 0) {
            dy =  - (deltaY / yMin);
	}

	for (int i = 1; i < coordonnees.length; i++) {
            int x1 = (int) ((coordonnees[i-1].getX() - decalageX) * dx);
            int x2 = (int) ((coordonnees[i].getX() - decalageX) * dx);
            int y1 = (int) (coordonnees[i-1].getY() * dy);
            int y2 = (int) (coordonnees[i].getY() * dy);
            gc.drawLine( x0Axe + x1, y0Axe - y1, x0Axe + x2, y0Axe - y2);
	}
    }

    private void activerMolette() {
	java.awt.event.MouseWheelListener listener = event -> {
	    float pas = Math.max(1.0f, largeurVisibleUnites() / 10.0f);
	    decalageX = limiterDecalage(decalageX + event.getWheelRotation() * pas);
	    repaint();
	};
	addMouseWheelListener(listener);
	getContentPane().addMouseWheelListener(listener);
    }

    private float largeurVisibleUnites() {
	float largeurPixels = getContentPane().getWidth() - 20;
	if (largeurPixels <= 0) {
            largeurPixels = getWidth() - 20;
	}
	return Math.max(1.0f, largeurPixels / pixelsParUniteX);
    }

    private float limiterDecalage(float decalage) {
	if ((coordonnees == null) || (coordonnees.length == 0)) {
            return 0.0f;
	}
	float xMax = (float) coordonnees[coordonnees.length - 1].getX();
	float decalageMax = Math.max(0.0f, xMax - 1.0f);
	return Math.max(0.0f, Math.min(decalage, decalageMax));
    }
}
