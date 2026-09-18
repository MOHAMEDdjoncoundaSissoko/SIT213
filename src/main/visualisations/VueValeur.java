package visualisations;
/**
 * @author B. Prou
 * Updated by E. Cousin - 2021
 *
 */

import javax.swing.*;

public class VueValeur  extends Vue {

    private static final long serialVersionUID = 1917L;

    private JLabel jLabel;

    public  VueValeur (Object valeur, String nom) {

	super(nom);
	String s = " " + valeur;
	jLabel = new JLabel(s);

	int xPosition = Vue.getXPosition();
	int yPosition = Vue.getYPosition();
	setLocation(xPosition, yPosition);

      	add(jLabel);
	setDefaultCloseOperation(EXIT_ON_CLOSE);
	setSize(300, 100);
	setVisible(true);
	repaint();
    }
}
