#!/usr/bin/env python3
"""
Courbes TEB du rapport de l'étape 5 (SIT213, groupe B4) : codage de canal.

Compare le simulateur avec et sans l'option -codeur et calcule le TEB théorique
exact après décodage. Réutilise le modèle et le style de rapport/etape4/courbesTEB.py.

    python3 rapport/etape5/courbesCodage.py              # réutilise les CSV existants
    python3 rapport/etape5/courbesCodage.py --recalculer # relance toutes les simulations

Pré-requis : projet compilé (./compile), Python 3 avec numpy et matplotlib.

Théorie avec codage : le bruit est indépendant d'un bit codé à l'autre, donc,
pour une suite de bits donnée, les erreurs sur les trois bits d'un paquet sont
indépendantes, de probabilités p1, p2, p3. Le décodeur se trompe si au moins
deux bits sont faux :
    P = p1 p2 + p1 p3 + p2 p3 - 2 p1 p2 p3      (3p^2 - 2p^3 si p1 = p2 = p3)
Les p_i dépendent des bits voisins (rampe NRZT, échos) : on énumère les suites
de trois bits d'information, on les code et on reconstruit la fenêtre reçue de
chaque bit du dernier paquet, comme pour la théorie de l'étape 4. Ps est la
puissance du signal codé (avec échos), qui règle le bruit.
"""

import argparse
import csv
import importlib.util
import itertools
import math
import os
import re
import subprocess
import sys
from concurrent.futures import ThreadPoolExecutor

import numpy as np

ICI = os.path.dirname(os.path.abspath(__file__))
RACINE = os.path.abspath(os.path.join(ICI, "..", ".."))
FIGURES = os.path.join(ICI, "figures")
DONNEES = os.path.join(ICI, "donnees")

# modèle (formes d'onde, filtre adapté, théorie sans codage) et style des figures de l'étape 4
_spec = importlib.util.spec_from_file_location(
    "courbesTEB", os.path.join(RACINE, "rapport", "etape4", "courbesTEB.py"))
e4 = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(e4)
plt = e4.plt

N = e4.N
SEED = e4.SEED
LONGUEUR_MOT = 3

# Limites mémoire (cf. étape 4) : avec le codeur le signal est 3 fois plus long,
# une tranche de 60 000 bits codée occupe autant qu'une tranche de 200 000 bits non codée (~400 Mo).
BITS_PAR_RUN = {False: 200000, True: 60000}
NB_RUNS_MAX = {False: 5, True: 10}
GAIN_DEBIT_DB = 10 * math.log10(LONGUEUR_MOT)   # énergie par bit utile = 3 x énergie par bit codé


# --------------------------------------------------------------------------
# Théorie
# --------------------------------------------------------------------------

def coder(bits):
    return [v for b in bits for v in (b, not b, b)]


def teb_theorique_code(forme, a_min, a_max, ebn0_db, echos=(), n=N):
    """TEB exact après décodage, pour -snrpb = ebn0_db (Eb/N0 par bit codé)."""
    if any(dt > 5 * n for dt, _ in echos):
        raise ValueError("retard trop grand pour l'énumération sur trois paquets")
    _, _, g, seuil = e4.filtre_adapte(forme, n, a_min, a_max)

    cas = []   # pour chaque suite : fenêtres reçues des 3 bits du dernier paquet
    for message in itertools.product((False, True), repeat=3):
        code = coder(message)
        s = np.concatenate([e4.forme_bit(forme, n, a_min, a_max, code[i], i > 0 and code[i - 1])
                            for i in range(len(code))])
        r = s.copy()
        for dt, ar in echos:
            if dt > 0:
                r[dt:] += ar * s[:-dt]
            else:
                r += ar * s
        cas.append([(code[i], r[i * n:(i + 1) * n]) for i in range(len(code) - LONGUEUR_MOT, len(code))])

    ps = np.mean([np.mean(w ** 2) for paquet in cas for _, w in paquet])
    sigma_y = 0.0 if math.isinf(ebn0_db) else \
        math.sqrt(ps * n / (2 * 10 ** (ebn0_db / 10))) * math.sqrt(np.sum(g * g))

    total = 0.0
    for paquet in cas:
        p = []
        for bit, w in paquet:
            y = float(np.sum(w * g))
            if sigma_y == 0:
                p.append(float((y > seuil) != bit))
            else:
                p.append(e4.Q(((y - seuil) if bit else (seuil - y)) / sigma_y))
        total += p[0] * p[1] + p[0] * p[2] + p[1] * p[2] - 2 * p[0] * p[1] * p[2]
    return total / len(cas)


def theorie(forme, a_min, a_max, ebn0_db, echos, codeur):
    if codeur:
        return teb_theorique_code(forme, a_min, a_max, ebn0_db, echos)
    return e4.teb_theorique(forme, a_min, a_max, ebn0_db, echos)


# --------------------------------------------------------------------------
# Simulations
# --------------------------------------------------------------------------

def simuler(args, nb_bits, codeur):
    erreurs = 0.0
    tranche = BITS_PAR_RUN[codeur]
    for i, debut in enumerate(range(0, nb_bits, tranche)):
        n = min(tranche, nb_bits - debut)
        commande = args + ["-mess", str(n), "-seed", str(SEED + i)]
        sortie = subprocess.run(e4.JAVA + ["-cp", os.path.join(RACINE, "bin"), "simulateur.Simulateur"]
                                + commande, capture_output=True, text=True, cwd=RACINE).stdout
        trouve = re.search(r"TEB : ([0-9.Ee-]+)", sortie)
        if not trouve:
            raise RuntimeError("sortie inattendue pour " + " ".join(commande) + " : " + sortie)
        erreurs += float(trouve.group(1)) * n
    return erreurs / nb_bits


def point(serie, x, forme, a_min, a_max, ebn0, echos=(), codeur=False, nb_bits=None):
    th = theorie(forme, a_min, a_max, ebn0, echos, codeur)
    if nb_bits is None:
        runs = min(NB_RUNS_MAX[codeur], max(1, math.ceil(100 / (max(th, 1e-12) * BITS_PAR_RUN[codeur]))))
        nb_bits = runs * BITS_PAR_RUN[codeur]
    args = ["-form", forme, "-nbEch", str(N), "-ampl", f"{a_min:g}", f"{a_max:g}"]
    if not math.isinf(ebn0):
        args += ["-snrpb", f"{ebn0:g}"]
    if echos:
        args += ["-ti"] + [v for dt, ar in echos for v in (str(dt), f"{ar:g}")]
    if codeur:
        args += ["-codeur"]
    return dict(serie=serie, x=x, args=args, codeur=codeur, teb_th=th, nb_bits=nb_bits)


def balayage(nom, points, recalculer):
    chemin = os.path.join(DONNEES, nom + ".csv")
    if os.path.exists(chemin) and not recalculer:
        with open(chemin) as f:
            return [dict(r, x=float(r["x"]), teb=float(r["teb"]), teb_th=float(r["teb_th"]),
                         nb_bits=int(r["nb_bits"])) for r in csv.DictReader(f, delimiter=";")]

    def executer(p):
        return dict(p, teb=simuler(p["args"], p["nb_bits"], p["codeur"]))

    with ThreadPoolExecutor(max_workers=e4.NB_PROCESSUS) as pool:
        resultats = list(pool.map(executer, points))

    os.makedirs(DONNEES, exist_ok=True)
    with open(chemin, "w", newline="") as f:
        w = csv.writer(f, delimiter=";")
        w.writerow(["serie", "x", "teb", "teb_th", "nb_bits", "commande"])
        for r in resultats:
            w.writerow([r["serie"], r["x"], r["teb"], r["teb_th"], r["nb_bits"],
                        "./simulateur " + " ".join(r["args"])])
    return resultats


def plage(forme, a_min, a_max, echos, codeur, debut=-4, fin=14):
    """Valeurs entières de -snrpb dont le TEB théorique reste mesurable."""
    seuil_mesurable = 5e-5 if codeur else 3e-5
    return [e for e in range(debut, fin + 1)
            if theorie(forme, a_min, a_max, e, echos, codeur) > seuil_mesurable]


# --------------------------------------------------------------------------
# Figures
# --------------------------------------------------------------------------

FORMES = [("NRZ", -1.0, 1.0, "NRZ antipodal $(-1, 1)$"),
          ("NRZT", -1.0, 1.0, "NRZT antipodal $(-1, 1)$"),
          ("RZ", 0.0, 1.0, "RZ unipolaire $(0, 1)$")]


def donnees_formes(recalculer):
    points = [point(f"{forme} {'avec' if codeur else 'sans'}", e, forme, a0, a1, e, codeur=codeur)
              for forme, a0, a1, _ in FORMES for codeur in (False, True)
              for e in plage(forme, a0, a1, (), codeur)]
    return balayage("codage_formes", points, recalculer)


def fig_codage_formes(res):
    fig, axes = plt.subplots(1, 3, figsize=(11, 3.7), sharey=True)
    for ax, (forme, a0, a1, titre) in zip(axes, FORMES):
        sous = [r for r in res if r["serie"].startswith(forme + " ")]
        e4.tracer_teb(ax, sous, [(f"{forme} sans", "sans codeur"), (f"{forme} avec", "avec codeur")],
                      lambda s, x: theorie(forme, a0, a1, x, (), s.endswith("avec")), 14)
        ax.set_title(titre, fontsize=10, color=e4.ENCRE)
        ax.set_xlabel("-snrpb (dB)")
        ax.set_ylabel("")
    axes[0].set_ylabel("TEB")
    # légende dans le dernier graphique, en haut à droite : zone sans courbe
    poignees, etiquettes = axes[2].get_legend_handles_labels()
    poignees.append(plt.Line2D([], [], color=e4.ENCRE, lw=1.4))
    etiquettes.append("théorie exacte (traits)")
    axes[2].legend(poignees, etiquettes, loc="upper right", fontsize=9)
    fig.savefig(os.path.join(FIGURES, "fig_codage_formes.pdf"))
    plt.close(fig)


def fig_codage_equitable(res):
    """NRZ antipodal : même énergie par bit d'information avec et sans codeur."""
    sous = []
    for r in res:
        if r["serie"] == "NRZ sans":
            sous.append(dict(r, serie="sans"))
        elif r["serie"] == "NRZ avec":
            sous.append(dict(r, serie="avec", x=r["x"] + GAIN_DEBIT_DB))
    fig, ax = plt.subplots(figsize=(7, 4.3))
    e4.tracer_teb(ax, sous, [("sans", "sans codeur"),
                             ("avec", "avec codeur (3 bits émis par bit utile)")],
                  lambda s, x: theorie("NRZ", -1.0, 1.0, x - (GAIN_DEBIT_DB if s == "avec" else 0), (),
                                       s == "avec"), 14)
    ax.set_xlabel(r"$E_b/N_0$ par bit d'information (dB)")
    e4.legende_theorie(ax)
    fig.savefig(os.path.join(FIGURES, "fig_codage_equitable.pdf"))
    plt.close(fig)


def fig_codage_trajets(res_formes, recalculer):
    echo = ((15, 0.5),)
    points = [point(f"echo {'avec' if codeur else 'sans'}", e, "NRZ", -1.0, 1.0, e, echo, codeur)
              for codeur in (False, True) for e in plage("NRZ", -1.0, 1.0, echo, codeur)]
    res = balayage("codage_trajets", points, recalculer)
    tous = res + [dict(r, serie=r["serie"].replace("NRZ", "direct")) for r in res_formes
                  if r["serie"].startswith("NRZ ")]
    fig, ax = plt.subplots(figsize=(7, 4.3))
    e4.tracer_teb(ax, tous, [("direct sans", "sans écho, sans codeur"),
                             ("direct avec", "sans écho, avec codeur"),
                             ("echo sans", "écho $dt = 15$, $ar = 0{,}5$, sans codeur"),
                             ("echo avec", "écho $dt = 15$, $ar = 0{,}5$, avec codeur")],
                  lambda s, x: theorie("NRZ", -1.0, 1.0, x, echo if s.startswith("echo") else (),
                                       s.endswith("avec")), 14)
    ax.set_xlabel("-snrpb (dB)")
    e4.legende_theorie(ax)
    fig.savefig(os.path.join(FIGURES, "fig_codage_trajets.pdf"))
    plt.close(fig)
    return res


def table_echos_sans_bruit(recalculer):
    """Étape 4a avec codeur : NRZ unipolaire, un écho, sans bruit."""
    cas = [(30, 0.4), (30, 0.6), (20, 0.6), (20, 0.9), (10, 0.9)]
    points = [point(f"dt={dt} ar={ar:g} {'avec' if codeur else 'sans'}", ar, "NRZ", 0.0, 1.0, math.inf,
                    ((dt, ar),), codeur, nb_bits=20000)
              for dt, ar in cas for codeur in (False, True)]
    return balayage("codage_echos_sans_bruit", points, recalculer)


def resume(nom, res):
    print(f"\n{nom} : {len(res)} points")
    zs = [(r["teb"] - r["teb_th"]) / math.sqrt(r["teb_th"] * (1 - r["teb_th"]) / r["nb_bits"])
          for r in res if 0 < r["teb_th"] < 1]
    if zs:
        print(f"  écart simulation/théorie : max {max(abs(z) for z in zs):.1f} écart-type, "
              f"{sum(abs(z) > 3 for z in zs)} point(s) au-delà de 3")
    for r in res:
        if r["teb_th"] in (0.0, 1.0) and abs(r["teb"] - r["teb_th"]) > 0.02:
            print(f"  ATTENTION : {r['serie']} simulé {r['teb']:.3g}, théorie {r['teb_th']:.3g}")


def main():
    parser = argparse.ArgumentParser(description=__doc__.split("\n\n")[0])
    parser.add_argument("--recalculer", action="store_true", help="relancer toutes les simulations")
    args = parser.parse_args()
    if not os.path.isdir(os.path.join(RACINE, "bin", "simulateur")):
        sys.exit("Compiler d'abord le projet : ./compile")
    os.makedirs(FIGURES, exist_ok=True)

    formes = donnees_formes(args.recalculer)
    fig_codage_formes(formes)
    fig_codage_equitable(formes)
    resume("Codage, trois formes d'onde", formes)
    resume("Codage et trajets multiples", fig_codage_trajets(formes, args.recalculer))
    sans_bruit = table_echos_sans_bruit(args.recalculer)
    resume("Codage et échos sans bruit", sans_bruit)
    for r in sans_bruit:
        print(f"  {r['serie']:24s} simulé {r['teb']:.4f}  théorie {r['teb_th']:.4f}")
    print(f"\nFigures dans {FIGURES}, données dans {DONNEES}")


if __name__ == "__main__":
    main()
