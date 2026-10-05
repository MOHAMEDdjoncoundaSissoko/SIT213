package information;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Information de Float dont les valeurs sont rangées dans un tableau de float.
 *
 * Information range ses éléments dans une ArrayList : chaque échantillon d'un
 * signal analogique y est un objet Float (16 octets) plus une référence
 * (4 octets), soit 20 octets au lieu des 4 d'un float. Cette sous-classe range
 * les valeurs dans un float[] : elle divise par trois environ la mémoire d'une
 * simulation et réduit le temps de calcul, qui était dominé par la création des
 * objets Float.
 *
 * Elle s'utilise partout où une Information de Float est attendue (comparaison,
 * affichage, parcours « for each », sondes). Seule différence : elle refuse la
 * valeur null, qu'un float ne peut pas représenter.
 *
 * @author ziani
 * @author sissoko
 * @author nanda
 * @author blombou
 * @author bouaboud
 */
public class InformationFlottante extends Information<Float> {

    /** taille maximale d'un tableau Java (marge laissée par la machine virtuelle) */
    private static final int TAILLE_MAX = Integer.MAX_VALUE - 8;

    /** les valeurs ; seules les nbElements() premières cases sont utilisées */
    private float[] valeurs;

    /** nombre de valeurs effectivement rangées */
    private int taille;

    /**
     * Construit une information vide.
     * @param capacite nombre de valeurs prévues ; le tableau s'agrandit si besoin
     */
    public InformationFlottante(int capacite) {
        super();
        this.valeurs = new float[Math.max(capacite, 0)];
        this.taille = 0;
    }

    /**
     * Valeur de rang i, sans conversion en objet Float.
     * @param i le rang de la valeur (à partir de 0)
     * @return la valeur de rang i
     * @throws IndexOutOfBoundsException si i n'est pas compris entre 0 et nbElements() - 1
     */
    public float valeur(int i) {
        return valeurs[Objects.checkIndex(i, taille)];
    }

    /**
     * Ajoute une valeur à la fin de l'information, sans conversion en objet Float.
     * @param valeur la valeur à ajouter
     */
    public void ajouter(float valeur) {
        if (taille == valeurs.length) {
            agrandir(taille + 1);
        }
        valeurs[taille++] = valeur;
    }

    /**
     * Ajoute un bloc de valeurs à la fin de l'information.
     * @param bloc les valeurs à ajouter, dans l'ordre
     */
    public void ajouter(float[] bloc) {
        if ((long) taille + bloc.length > valeurs.length) {
            agrandir((long) taille + bloc.length);
        }
        System.arraycopy(bloc, 0, valeurs, taille, bloc.length);
        taille += bloc.length;
    }

    /** Agrandit le tableau (au moins du double) pour contenir capaciteMin valeurs. */
    private void agrandir(long capaciteMin) {
        if (capaciteMin > TAILLE_MAX) {
            throw new OutOfMemoryError("Information trop longue : " + capaciteMin + " valeurs");
        }
        long nouvelleCapacite = Math.max(capaciteMin, Math.max(16L, 2L * valeurs.length));
        valeurs = Arrays.copyOf(valeurs, (int) Math.min(nouvelleCapacite, TAILLE_MAX));
    }

    @Override
    public int nbElements() {
        return taille;
    }

    @Override
    public Float iemeElement(int i) {
        return valeur(i);
    }

    /**
     * Modifie la valeur de rang i.
     * @param i le rang de la valeur à modifier (à partir de 0)
     * @param v la nouvelle valeur, non nulle
     * @throws NullPointerException si v est null
     */
    @Override
    public void setIemeElement(int i, Float v) {
        valeurs[Objects.checkIndex(i, taille)] = v;
    }

    /**
     * Ajoute une valeur à la fin de l'information.
     * @param v la valeur à ajouter, non nulle
     * @throws NullPointerException si v est null
     */
    @Override
    public void add(Float v) {
        ajouter((float) v);
    }

    @Override
    public Iterator<Float> iterator() {
        return new Iterator<Float>() {
            private int suivant = 0;

            @Override
            public boolean hasNext() {
                return suivant < taille;
            }

            @Override
            public Float next() {
                if (suivant >= taille) {
                    throw new NoSuchElementException();
                }
                return valeurs[suivant++];
            }
        };
    }
}
