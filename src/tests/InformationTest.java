package tests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import information.Information;
import information.InformationFlottante;
import transmetteurs.TransmetteurAnalogiqueBruite;
import transmetteurs.TransmetteurLogiqueAnalogique;

/**
 * Tests JUnit pour la classe Information.
 */
public class InformationTest {

    @Test
    @DisplayName("Constructeur vide - information vide")
    void testConstructeurVide() {
        Information<Boolean> info = new Information<>();
        assertEquals(0, info.nbElements());
    }

    @Test
    @DisplayName("Constructeur tableau - elements initialises")
    void testConstructeurTableau() {
        Boolean[] tab = {true, false, true, true};
        Information<Boolean> info = new Information<>(tab);
        assertEquals(4, info.nbElements());
        assertTrue(info.iemeElement(0));
        assertFalse(info.iemeElement(1));
        assertTrue(info.iemeElement(2));
        assertTrue(info.iemeElement(3));
    }

    @Test
    @DisplayName("Constructeur tableau Float")
    void testConstructeurTableauFloat() {
        Float[] tab = {1.0f, 0.5f, -1.0f};
        Information<Float> info = new Information<>(tab);
        assertEquals(3, info.nbElements());
        assertEquals(1.0f, info.iemeElement(0));
        assertEquals(0.5f, info.iemeElement(1));
        assertEquals(-1.0f, info.iemeElement(2));
    }

    @Test
    @DisplayName("add - ajout d elements")
    void testAdd() {
        Information<Boolean> info = new Information<>();
        info.add(true);
        info.add(false);
        info.add(true);
        assertEquals(3, info.nbElements());
        assertTrue(info.iemeElement(0));
        assertFalse(info.iemeElement(1));
        assertTrue(info.iemeElement(2));
    }

    @Test
    @DisplayName("setIemeElement - modification d un element")
    void testSetIemeElement() {
        Information<Boolean> info = new Information<>();
        info.add(true);
        info.add(true);
        info.setIemeElement(1, false);
        assertTrue(info.iemeElement(0));
        assertFalse(info.iemeElement(1));
    }

    @Test
    @DisplayName("equals - deux informations identiques")
    void testEqualsTrue() {
        Information<Boolean> a = new Information<>();
        Information<Boolean> b = new Information<>();
        a.add(true); a.add(false); a.add(true);
        b.add(true); b.add(false); b.add(true);
        assertTrue(a.equals(b));
    }

    @Test
    @DisplayName("equals - longueurs differentes")
    void testEqualsFausseLongueur() {
        Information<Boolean> a = new Information<>();
        Information<Boolean> b = new Information<>();
        a.add(true);
        b.add(true); b.add(false);
        assertFalse(a.equals(b));
    }

    @Test
    @DisplayName("equals - contenu different")
    void testEqualsFausseContenu() {
        Information<Boolean> a = new Information<>();
        Information<Boolean> b = new Information<>();
        a.add(true);
        b.add(false);
        assertFalse(a.equals(b));
    }

    @Test
    @DisplayName("equals - objet non Information")
    void testEqualsNonInformation() {
        Information<Boolean> info = new Information<>();
        info.add(true);
        assertFalse(info.equals("hello"));
        assertFalse(info.equals(null));
    }

    @Test
    @DisplayName("toString - representation correcte")
    void testToString() {
        Information<Boolean> info = new Information<>();
        info.add(true);
        info.add(false);
        String s = info.toString();
        assertTrue(s.contains("true"));
        assertTrue(s.contains("false"));
    }

    @Test
    @DisplayName("toString - information vide")
    void testToStringVide() {
        Information<Boolean> info = new Information<>();
        assertEquals("", info.toString());
    }

    @Test
    @DisplayName("iterator - parcours for-each")
    void testIterator() {
        Information<Boolean> info = new Information<>();
        info.add(true); info.add(false); info.add(true);
        int count = 0;
        for (Boolean b : info) {
            count++;
        }
        assertEquals(3, count);
    }

    @Test
    @DisplayName("equals - information avec elle-meme")
    void testEqualsSymetrie() {
        Information<Boolean> info = new Information<>();
        info.add(true); info.add(false);
        assertTrue(info.equals(info));
    }

    // InformationFlottante (signal analogique range dans un float[])

    /** Information de Float classique, pour comparaison. */
    private static Information<Float> classique(float... valeurs) {
        Information<Float> info = new Information<>();
        for (float v : valeurs) info.add(v);
        return info;
    }

    @Test
    @DisplayName("InformationFlottante - ajout, lecture et agrandissement du tableau")
    void testFlottanteAjoutEtLecture() {
        InformationFlottante info = new InformationFlottante(0);
        for (int i = 0; i < 100; i++) info.ajouter(i * 0.5f);
        info.add(-1.5f);
        info.ajouter(new float[] {2f, 3f});

        assertEquals(103, info.nbElements());
        assertEquals(49.5f, info.valeur(99));
        assertEquals(Float.valueOf(-1.5f), info.iemeElement(100));
        assertEquals(3f, info.valeur(102));
    }

    @Test
    @DisplayName("InformationFlottante - egale a une Information de Float classique, dans les deux sens")
    void testFlottanteEqualsClassique() {
        InformationFlottante info = new InformationFlottante(4);
        info.ajouter(new float[] {0.5f, -1f, 2f});

        assertEquals(classique(0.5f, -1f, 2f), info);
        assertEquals(info, classique(0.5f, -1f, 2f));
        assertNotEquals(classique(0.5f, -1f, 2.5f), info);
        assertNotEquals(classique(0.5f, -1f), info);
    }

    @Test
    @DisplayName("InformationFlottante - for each, toString et modification comme Information")
    void testFlottanteParcoursEtAffichage() {
        InformationFlottante info = new InformationFlottante(8);
        info.ajouter(new float[] {1f, 2f, 3f});
        info.setIemeElement(1, 5f);

        float somme = 0;
        for (float v : info) somme += v;
        assertEquals(9f, somme);
        assertEquals(classique(1f, 5f, 3f).toString(), info.toString());

        java.util.Iterator<Float> it = info.iterator();
        it.next(); it.next(); it.next();
        assertFalse(it.hasNext());
        assertThrows(java.util.NoSuchElementException.class, it::next);
    }

    @Test
    @DisplayName("InformationFlottante - rang hors des valeurs rangees et valeur null refuses")
    void testFlottanteErreurs() {
        // capacite 10 mais 2 valeurs : les rangs 2 a 9 n'existent pas
        InformationFlottante info = new InformationFlottante(10);
        info.ajouter(new float[] {1f, 2f});

        assertThrows(IndexOutOfBoundsException.class, () -> info.valeur(2));
        assertThrows(IndexOutOfBoundsException.class, () -> info.iemeElement(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> info.setIemeElement(5, 1f));
        assertThrows(NullPointerException.class, () -> info.add(null));
        assertEquals(2, info.nbElements());
    }

    @Test
    @DisplayName("InformationFlottante - utilisee par l'emetteur et le canal bruite")
    void testFlottanteDansLaChaine() throws Exception {
        TransmetteurLogiqueAnalogique emetteur = new TransmetteurLogiqueAnalogique("NRZ", 10, -1f, 1f);
        TransmetteurAnalogiqueBruite canal = new TransmetteurAnalogiqueBruite(10, 5f, 1);
        emetteur.connecter(canal);
        Information<Boolean> bits = new Information<>();
        bits.add(true); bits.add(false); bits.add(true);

        emetteur.recevoir(bits);

        assertInstanceOf(InformationFlottante.class, emetteur.getInformationEmise());
        assertInstanceOf(InformationFlottante.class, canal.getInformationEmise());
        assertEquals(30, canal.getInformationEmise().nbElements());
    }
}
