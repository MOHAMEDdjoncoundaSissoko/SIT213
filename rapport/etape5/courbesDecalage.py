#!/usr/bin/env python3
"""
Courbes du rapport de l'étape 5 : désynchronisation du récepteur (option -decalage).

Mesure le TEB en fonction du décalage d de la fenêtre de décision, avec et sans
codeur, le compare à la théorie exacte, et produit les figures du rapport.
Réutilise rapport/etape5/courbesCodage.py (simulations, style) et l'étape 4.

    python3 rapport/etape5/courbesDecalage.py              # réutilise les CSV existants
    python3 rapport/etape5/courbesDecalage.py --recalculer # relance les simulations

Pré-requis : projet compilé (./compile), Python 3 avec numpy et matplotlib.

Théorie exacte : pour le bit courant, la fenêtre de décision couvre les
échantillons [iN + d, iN + d + N). Elle mord sur le bit précédent (d < 0) ou
suivant (d > 0). On énumère les suites de bits voisins (et le bit d'avant, dont
dépend la rampe NRZT), on reconstruit le signal sans bruit avec les formes de
l'émetteur, on calcule la sortie du filtre adapté dans la fenêtre décalée et
on moyenne Q(marge / écart-type) ; avec le codeur, les erreurs des trois bits
d'un paquet sont indépendantes et le décodeur se trompe si au moins deux sont
fausses (cf. courbesCodage.py).
"""

import argparse
import csv
import functools
import importlib.util
import itertools
import math
import os
import shutil
import subprocess
import sys
import tempfile

import numpy as np

ICI = os.path.dirname(os.path.abspath(__file__))
RACINE = os.path.abspath(os.path.join(ICI, "..", ".."))

_spec = importlib.util.spec_from_file_location("courbesCodage", os.path.join(ICI, "courbesCodage.py"))
cc = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(cc)
e4 = cc.e4
plt = e4.plt

N = e4.N
DECALAGES = list(range(-(N - 1), N))
EBN0_BRUIT = 4.0
FORMES = cc.FORMES


# --------------------------------------------------------------------------
# Théorie
# --------------------------------------------------------------------------

def signal(forme, a_min, a_max, bits):
    return np.concatenate([e4.forme_bit(forme, N, a_min, a_max, bits[i], i > 0 and bits[i - 1])
                           for i in range(len(bits))])


def probas_erreur(forme, a_min, a_max, ebn0_db, d, bits, indices, ps):
    """Probabilité d'erreur de chaque bit demandé, fenêtre décalée de d échantillons."""
    _, _, g, seuil = e4.filtre_adapte(forme, N, a_min, a_max)
    s = signal(forme, a_min, a_max, bits)
    sigma_y = 0.0 if math.isinf(ebn0_db) else \
        math.sqrt(ps * N / (2 * 10 ** (ebn0_db / 10))) * math.sqrt(np.sum(g * g))
    p = []
    for c in indices:
        debut = c * N + d
        w = s[debut:debut + N]
        y = float(np.sum(w * g))
        if sigma_y == 0:
            p.append(float((y > seuil) != bits[c]))
        else:
            p.append(e4.Q(((y - seuil) if bits[c] else (seuil - y)) / sigma_y))
    return p


@functools.lru_cache(maxsize=None)
def teb_theorique(forme, a_min, a_max, ebn0_db, d, codeur):
    """TEB exact (après décodage avec le codeur) pour un décalage d, |d| < N."""
    if not codeur:
        # bits [-2, -1, 0, +1, +2] autour du bit courant (indice 2)
        suites = [list(b) for b in itertools.product((False, True), repeat=5)]
        ps = np.mean([np.mean(signal(forme, a_min, a_max, b)[2 * N:3 * N] ** 2) for b in suites])
        return float(np.mean([probas_erreur(forme, a_min, a_max, ebn0_db, d, b, [2], ps)[0]
                              for b in suites]))
    # trois bits d'information codés : le paquet courant est celui du milieu (bits codés 3, 4, 5)
    suites = [cc.coder(info) for info in itertools.product((False, True), repeat=3)]
    ps = np.mean([np.mean(signal(forme, a_min, a_max, c)[k * N:(k + 1) * N] ** 2)
                  for c in suites for k in (3, 4, 5)])
    total = 0.0
    for c in suites:
        p1, p2, p3 = probas_erreur(forme, a_min, a_max, ebn0_db, d, c, [3, 4, 5], ps)
        total += p1 * p2 + p1 * p3 + p2 * p3 - 2 * p1 * p2 * p3
    return total / len(suites)


# --------------------------------------------------------------------------
# Simulations
# --------------------------------------------------------------------------

def point(serie, forme, a_min, a_max, ebn0, d, codeur, nb_bits=None):
    th = teb_theorique(forme, a_min, a_max, ebn0, d, codeur)
    if nb_bits is None:
        tranche = cc.BITS_PAR_RUN[codeur]
        runs = min(cc.NB_RUNS_MAX[codeur], max(1, math.ceil(100 / (max(th, 1e-12) * tranche))))
        nb_bits = runs * tranche
    args = ["-form", forme, "-nbEch", str(N), "-ampl", f"{a_min:g}", f"{a_max:g}", "-decalage", str(d)]
    if not math.isinf(ebn0):
        args += ["-snrpb", f"{ebn0:g}"]
    if codeur:
        args += ["-codeur"]
    return dict(serie=serie, x=d, args=args, codeur=codeur, teb_th=th, nb_bits=nb_bits)


def series():
    for forme, a0, a1, _ in FORMES:
        for codeur in (False, True):
            yield f"{forme} {'avec' if codeur else 'sans'}", forme, a0, a1, codeur


def donnees_sans_bruit(recalculer):
    points = [point(nom, f, a0, a1, math.inf, d, k, nb_bits=20000)
              for nom, f, a0, a1, k in series() for d in DECALAGES]
    return cc.balayage("decalage_sans_bruit", points, recalculer)


def donnees_bruit(recalculer):
    points = [point(nom, f, a0, a1, EBN0_BRUIT, d, k)
              for nom, f, a0, a1, k in series() for d in DECALAGES if d % 2 == 0]
    return cc.balayage("decalage_bruit", points, recalculer)


# --------------------------------------------------------------------------
# Figures
# --------------------------------------------------------------------------

def tracer(res, nom_fichier, ebn0, echelle_log):
    fig, axes = plt.subplots(1, 3, figsize=(11, 3.8), sharey=True)
    for ax, (forme, a0, a1, titre) in zip(axes, FORMES):
        # « avec codeur » d'abord : les points « sans codeur », plus petits, restent visibles par-dessus
        for codeur in (True, False):
            i = 1 if codeur else 0
            serie = f"{forme} {'avec' if codeur else 'sans'}"
            th = [teb_theorique(forme, a0, a1, ebn0, d, codeur) for d in DECALAGES]
            pts = sorted((r["x"], r["teb"]) for r in res if r["serie"] == serie)
            if not echelle_log:
                pts = [p for p in pts if int(p[0]) % 2 == 0]   # un point sur deux, pour la lisibilité
            else:
                pts = [p for p in pts if p[1] > 0]
            trace = ax.semilogy if echelle_log else ax.plot
            if echelle_log:
                trace(DECALAGES, th, color=e4.COULEURS[i], lw=1.4)
            else:
                ax.step(DECALAGES, th, where="mid", color=e4.COULEURS[i], lw=1.4)
            style = dict(mfc="none", mec=e4.COULEURS[i], ms=7, mew=1.3) if codeur else \
                dict(mec="white", ms=6, mew=1.0)
            trace([p[0] for p in pts], [p[1] for p in pts], e4.MARQUEURS[i], color=e4.COULEURS[i],
                  ls="none", label="avec codeur" if codeur else "sans codeur", **style)
        for limite in (-N / 2, N / 2):
            ax.axvline(limite, color=e4.GRILLE, lw=1.2, ls="--", zorder=0)
        ax.set_title(titre, fontsize=10, color=e4.ENCRE)
        ax.set_xlabel("décalage $d$ (échantillons)")
        ax.set_xlim(-N, N)
    axes[0].set_ylabel("TEB")
    if echelle_log:
        axes[0].set_ylim(1e-4, 1.5)
    else:
        axes[0].set_ylim(-0.03, 1.05)
    # légende dans une zone sans courbe : centre du NRZ sans bruit, bas du RZ avec bruit
    poignees, etiquettes = axes[0].get_legend_handles_labels()
    ordre = sorted(range(len(etiquettes)), key=lambda k: etiquettes[k] != "sans codeur")
    poignees = [poignees[k] for k in ordre] + [plt.Line2D([], [], color=e4.ENCRE, lw=1.4)]
    etiquettes = [etiquettes[k] for k in ordre] + ["théorie exacte (traits)"]
    if echelle_log:
        axes[2].legend(poignees, etiquettes, loc="lower right", fontsize=8)
    else:
        axes[0].legend(poignees, etiquettes, loc="center", bbox_to_anchor=(0.5, 0.62), fontsize=8)
    fig.savefig(os.path.join(cc.FIGURES, nom_fichier))
    plt.close(fig)


def outils_java(dossier):
    sortie = os.path.join(dossier, "outils")
    os.makedirs(sortie)
    subprocess.run(["javac", "-cp", os.path.join(RACINE, "bin"), "-d", sortie,
                    os.path.join(ICI, "outils", "OutilsDecalage.java")], check=True)
    return os.pathsep.join([sortie, os.path.join(RACINE, "bin")])


def capture(classpath):
    subprocess.run(e4.JAVA + ["-Djava.awt.headless=true", "-cp", classpath, "OutilsDecalage", "capture",
                              os.path.join(cc.FIGURES, "fig_decalage_vue.png")], check=True)


def paquets(classpath, recalculer):
    """Proportion de paquets reçus qui ne sont pas des mots de code (NRZ antipodal, avec codeur)."""
    chemin = os.path.join(cc.DONNEES, "decalage_paquets.csv")
    if os.path.exists(chemin) and not recalculer:
        with open(chemin) as f:
            return list(csv.DictReader(f, delimiter=";"))
    lignes = []
    for ebn0, nb_bits in (("inf", 20000), (f"{EBN0_BRUIT:g}", 60000)):
        sortie = subprocess.run(e4.JAVA + ["-Djava.awt.headless=true", "-cp", classpath, "OutilsDecalage",
                                           "paquets", "NRZ", "-1", "1", ebn0, str(nb_bits), "1",
                                           "0", "5", "10", "14", "16", "20"],
                                capture_output=True, text=True, check=True).stdout
        for ligne in sortie.split():
            d, invalides, teb = ligne.split(";")
            lignes.append(dict(ebn0=ebn0, decalage=d, paquets_invalides=invalides, teb=teb))
    with open(chemin, "w", newline="") as f:
        w = csv.DictWriter(f, fieldnames=["ebn0", "decalage", "paquets_invalides", "teb"], delimiter=";")
        w.writeheader()
        w.writerows(lignes)
    return lignes


def main():
    parser = argparse.ArgumentParser(description=__doc__.split("\n\n")[0])
    parser.add_argument("--recalculer", action="store_true", help="relancer toutes les simulations")
    args = parser.parse_args()
    if not os.path.isdir(os.path.join(RACINE, "bin", "simulateur")):
        sys.exit("Compiler d'abord le projet : ./compile")
    os.makedirs(cc.FIGURES, exist_ok=True)

    sans_bruit = donnees_sans_bruit(args.recalculer)
    tracer(sans_bruit, "fig_decalage_sans_bruit.pdf", math.inf, False)
    cc.resume("Décalage sans bruit", sans_bruit)
    bruit = donnees_bruit(args.recalculer)
    tracer(bruit, "fig_decalage_bruit.pdf", EBN0_BRUIT, True)
    cc.resume(f"Décalage, Eb/N0 = {EBN0_BRUIT:g} dB", bruit)

    temporaire = tempfile.mkdtemp(prefix="sit213_decalage_")
    try:
        classpath = outils_java(temporaire)
        capture(classpath)
        print("\nPaquets reçus qui ne sont pas des mots de code (NRZ antipodal, avec codeur) :")
        for ligne in paquets(classpath, args.recalculer):
            print(f"  Eb/N0 = {ligne['ebn0']:>3} dB  d = {ligne['decalage']:>3}  "
                  f"paquets invalides {float(ligne['paquets_invalides']):.3f}  TEB {float(ligne['teb']):.4f}")
    finally:
        shutil.rmtree(temporaire, ignore_errors=True)
    print(f"\nFigures dans {cc.FIGURES}, données dans {cc.DONNEES}")


if __name__ == "__main__":
    main()
