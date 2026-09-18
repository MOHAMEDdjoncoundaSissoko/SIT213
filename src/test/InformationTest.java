package tests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import information.Information;

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
}
