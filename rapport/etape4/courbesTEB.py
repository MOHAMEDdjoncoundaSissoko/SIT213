#!/usr/bin/env python3
"""
Courbes TEB du rapport de l'étape 4 (SIT213, groupe B4).

Lance le simulateur (./simulateur) sur des balayages de paramètres, calcule le
TEB théorique exact de la chaîne et produit les figures PDF du rapport.

    python3 rapport/etape4/courbesTEB.py              # réutilise les CSV existants
    python3 rapport/etape4/courbesTEB.py --recalculer # relance toutes les simulations

Pré-requis : projet compilé (./compile), Python 3 avec numpy et matplotlib.
La comparaison avec le récepteur de l'étape 3 recompile la version du commit
REF_ETAPE3 (git archive) dans un répertoire temporaire.

Théorie exacte : pour chaque suite possible des bits voisins (le bit précédent
pour la rampe NRZT, les bits atteints par les échos), on reconstruit la fenêtre
reçue du bit courant sans bruit. Le bruit en sortie du récepteur est gaussien :
  - filtre adapté : y = somme r[k] g[k], écart-type sigma_b * ||g|| ;
  - étape 3 : y = r[N/2], écart-type sigma_b.
Le TEB est la moyenne de Q(marge / écart-type) sur toutes ces suites, avec
sigma_b^2 = Ps N / (2 Eb/N0) et Ps mesurée sur le signal avec échos, comme dans
TransmetteurAnalogiqueBruite.
"""

import argparse
import csv
import itertools
import math
import os
import re
import shutil
import subprocess
import sys
import tempfile
from concurrent.futures import ThreadPoolExecutor

import numpy as np
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

ICI = os.path.dirname(os.path.abspath(__file__))
RACINE = os.path.abspath(os.path.join(ICI, "..", ".."))
FIGURES = os.path.join(ICI, "figures")
DONNEES = os.path.join(ICI, "donnees")

# dernier commit dont le récepteur décide sur l'échantillon central (étape 3)
REF_ETAPE3 = "82e4660"

N = 30          # nombre d'échantillons par bit
SEED = 1

# Limites mémoire : à l'étape 4, une simulation de 200 000 bits occupait ~400 Mo (signal en objets Float) ;
# depuis le stockage en float[] de l'étape 5 (InformationFlottante), elle n'occupe plus que ~150 Mo.
# Les points à faible TEB sont découpés en plusieurs simulations de BITS_PAR_RUN bits
# (semences SEED, SEED+1, ...) dont on cumule les erreurs, plutôt qu'un seul run géant.
BITS_PAR_RUN = 200000
NB_RUNS_MAX = 5
NB_PROCESSUS = 2
JAVA = ["nice", "-n", "10", "java", "-Xmx600m"]

# palette catégorielle validée (dataviz) ; un marqueur distinct par série pour l'impression N&B
COULEURS = ["#2a78d6", "#eb6834", "#1baf7a", "#eda100"]
MARQUEURS = ["o", "s", "^", "D"]
ENCRE = "#3d3d3a"
GRILLE = "#e4e3dc"

plt.rcParams.update({
    "font.family": "serif",
    "font.size": 10,
    "axes.edgecolor": ENCRE,
    "axes.labelcolor": ENCRE,
    "xtick.color": ENCRE,
    "ytick.color": ENCRE,
    "axes.grid": True,
    "grid.color": GRILLE,
    "grid.linewidth": 0.6,
    "axes.spines.top": False,
    "axes.spines.right": False,
    "legend.frameon": False,
    "lines.linewidth": 1.6,
    "savefig.bbox": "tight",
})


# --------------------------------------------------------------------------
# Modèle de la chaîne (identique à TransmetteurLogiqueAnalogique.formeBit)
# --------------------------------------------------------------------------

def forme_bit(forme, n, a_min, a_max, bit, bit_precedent):
    niveau = a_max if bit else a_min
    debut = a_max if bit_precedent else a_min
    t1 = n // 3
    t2 = 2 * t1
    e = np.arange(n, dtype=float)
    if forme == "RZ":
        return np.where((e >= t1) & (e < t2), niveau, a_min).astype(float)
    if forme == "NRZT":
        return np.where(e < t1, debut + (e / t1) * (niveau - debut), niveau).astype(float)
    return np.full(n, niveau, dtype=float)


def filtre_adapte(forme, n, a_min, a_max):
    """g = s1 - s0 et seuil, s1 et s0 moyennés sur le bit précédent (cf. TransmetteurAnalogiqueLogique)."""
    s1 = sum(forme_bit(forme, n, a_min, a_max, True, p) for p in (False, True)) / 2
    s0 = sum(forme_bit(forme, n, a_min, a_max, False, p) for p in (False, True)) / 2
    g = s1 - s0
    return s1, s0, g, float(np.sum(g * (s1 + s0) / 2))


def Q(x):
    return 0.5 * math.erfc(x / math.sqrt(2))


def teb_theorique(forme, a_min, a_max, ebn0_db, echos=(), recepteur="filtre", n=N):
    nb_bits_passes = max([0] + [math.ceil(dt / n) for dt, _ in echos]) + 1
    longueur = nb_bits_passes + 1
    _, _, g, seuil_filtre = filtre_adapte(forme, n, a_min, a_max)

    fenetres = []
    for bits in itertools.product((False, True), repeat=longueur):
        s = np.concatenate([forme_bit(forme, n, a_min, a_max, bits[i], i > 0 and bits[i - 1])
                            for i in range(longueur)])
        r = s.copy()
        for dt, ar in echos:
            if dt > 0:
                r[dt:] += ar * s[:-dt]
            else:
                r += ar * s
        fenetres.append((bits[-1], r[-n:]))

    ps = np.mean([np.mean(w ** 2) for _, w in fenetres])
    sigma_b = 0.0 if math.isinf(ebn0_db) else math.sqrt(ps * n / (2 * 10 ** (ebn0_db / 10)))

    total = 0.0
    for bit, w in fenetres:
        if recepteur == "filtre":
            y, seuil, sigma_y = float(np.sum(w * g)), seuil_filtre, sigma_b * math.sqrt(np.sum(g * g))
        else:
            y, seuil, sigma_y = float(w[n // 2]), (a_min + a_max) / 2, sigma_b
        if sigma_y == 0:
            total += float((y > seuil) != bit)
        else:
            marge = (y - seuil) if bit else (seuil - y)
            total += Q(marge / sigma_y)
    return total / len(fenetres)


# --------------------------------------------------------------------------
# Exécution du simulateur
# --------------------------------------------------------------------------

def construire_etape3(dossier):
    """Recompile le simulateur de l'étape 3 (récepteur à échantillon central)."""
    archive = subprocess.run(["git", "-C", RACINE, "archive", REF_ETAPE3, "src"],
                             capture_output=True, check=True).stdout
    subprocess.run(["tar", "-x", "-C", dossier], input=archive, check=True)
    sources = [os.path.join(d, f) for d, _, fs in os.walk(os.path.join(dossier, "src"))
               for f in fs if f.endswith(".java") and "/tests/" not in os.path.join(d, f)]
    subprocess.run(["javac", "-d", os.path.join(dossier, "bin")] + sources, check=True)
    return os.path.join(dossier, "bin")


def simuler(classpath, args, nb_bits):
    """TEB cumulé sur nb_bits, en runs d'au plus BITS_PAR_RUN bits (semences consécutives)."""
    erreurs = 0.0
    for i, debut in enumerate(range(0, nb_bits, BITS_PAR_RUN)):
        n = min(BITS_PAR_RUN, nb_bits - debut)
        commande = args + ["-mess", str(n), "-seed", str(SEED + i)]
        sortie = subprocess.run(JAVA + ["-cp", classpath, "simulateur.Simulateur"] + commande,
                                capture_output=True, text=True, cwd=RACINE).stdout
        trouve = re.search(r"TEB : ([0-9.Ee-]+)", sortie)
        if not trouve:
            raise RuntimeError("sortie inattendue pour " + " ".join(commande) + " : " + sortie)
        erreurs += float(trouve.group(1)) * n
    return erreurs / nb_bits


def nb_bits_pour(teb_attendu):
    # viser au moins ~100 erreurs, dans la limite de NB_RUNS_MAX runs
    runs = min(NB_RUNS_MAX, max(1, math.ceil(100 / (teb_attendu * BITS_PAR_RUN))))
    return runs * BITS_PAR_RUN


def balayage(nom, points, recalculer, classpaths):
    """points : liste de dict(serie, x, args, recepteur, teb_th). Résultats mis en cache en CSV."""
    chemin = os.path.join(DONNEES, nom + ".csv")
    if os.path.exists(chemin) and not recalculer:
        with open(chemin) as f:
            return [dict(r, x=float(r["x"]), teb=float(r["teb"]), teb_th=float(r["teb_th"]),
                         nb_bits=int(r["nb_bits"])) for r in csv.DictReader(f, delimiter=";")]

    def executer(p):
        return dict(p, teb=simuler(classpaths[p["recepteur"]], p["args"], p["nb_bits"]))

    with ThreadPoolExecutor(max_workers=NB_PROCESSUS) as pool:
        resultats = list(pool.map(executer, points))

    os.makedirs(DONNEES, exist_ok=True)
    with open(chemin, "w", newline="") as f:
        w = csv.writer(f, delimiter=";")
        w.writerow(["serie", "recepteur", "x", "teb", "teb_th", "nb_bits", "commande"])
        for r in resultats:
            w.writerow([r["serie"], r["recepteur"], r["x"], r["teb"], r["teb_th"], r["nb_bits"],
                        "./simulateur " + " ".join(r["args"])])
    return resultats


def point(serie, x, forme, a_min, a_max, ebn0, echos=(), recepteur="filtre", nb_bits=None):
    th = teb_theorique(forme, a_min, a_max, ebn0, echos, recepteur)
    nb = nb_bits or nb_bits_pour(th)
    args = ["-form", forme, "-nbEch", str(N), "-ampl", f"{a_min:g}", f"{a_max:g}"]
    if not math.isinf(ebn0):
        args += ["-snrpb", f"{ebn0:g}"]
    if echos:
        args += ["-ti"] + [v for dt, ar in echos for v in (str(dt), f"{ar:g}")]
    return dict(serie=serie, x=x, args=args, recepteur=recepteur, teb_th=th, nb_bits=nb)


# --------------------------------------------------------------------------
# Figures
# --------------------------------------------------------------------------

def tracer_teb(ax, resultats, series, theorie, x_fin):
    """Points simulés (marqueurs) et théorie exacte (traits) pour chaque série."""
    for i, (serie, etiquette) in enumerate(series):
        pts = sorted((r["x"], r["teb"]) for r in resultats if r["serie"] == serie and r["teb"] > 0)
        xs = np.linspace(min(r["x"] for r in resultats if r["serie"] == serie), x_fin, 200)
        ax.semilogy(xs, [theorie(serie, x) for x in xs], color=COULEURS[i], lw=1.4)
        # 4e série en marqueur creux : elle peut se superposer exactement à une autre (NRZ/RZ 0/1)
        creux = dict(mfc="none", mec=COULEURS[i], ms=8, mew=1.4) if i == 3 else dict(mec="white", ms=6, mew=1.0)
        ax.semilogy([p[0] for p in pts], [p[1] for p in pts], MARQUEURS[i], color=COULEURS[i],
                    ls="none", label=etiquette, **creux)
    ax.set_ylabel("TEB")
    ax.set_ylim(1e-5, 0.6)


def legende_theorie(ax, dessous=False):
    handles, labels = ax.get_legend_handles_labels()
    handles.append(plt.Line2D([], [], color=ENCRE, lw=1.4))
    labels.append("théorie exacte (traits)")
    if dessous:
        ax.legend(handles, labels, loc="upper center", bbox_to_anchor=(0.5, -0.14), ncol=2, fontsize=9)
    else:
        ax.legend(handles, labels, loc="lower left", fontsize=9)


def fig_filtres():
    fig, axes = plt.subplots(1, 3, figsize=(10, 2.8), sharey=True)
    k = np.arange(N)
    for ax, forme in zip(axes, ("NRZ", "NRZT", "RZ")):
        s1, s0, g, seuil = filtre_adapte(forme, N, 0.0, 1.0)
        ax.step(k, s1, where="post", color=COULEURS[0], label=r"$s_1$")
        ax.step(k, s0, where="post", color=COULEURS[1], label=r"$s_0$")
        ax.fill_between(k, g, step="post", color=COULEURS[2], alpha=0.25, lw=0,
                        label=r"$g = s_1 - s_0$")
        ax.set_title(f"{forme}  (seuil = {seuil:.2f})", fontsize=10, color=ENCRE)
        ax.set_xlabel("échantillon $k$ du temps bit")
    axes[0].set_ylabel("amplitude")
    axes[0].legend(loc="upper left", fontsize=8)
    fig.savefig(os.path.join(FIGURES, "fig_filtres.pdf"))
    plt.close(fig)


def fig_teb_filtre(recalculer, classpaths):
    configs = {
        "NRZ ±1": ("NRZ", -1.0, 1.0),
        "NRZT ±1": ("NRZT", -1.0, 1.0),
        "NRZ 0/1": ("NRZ", 0.0, 1.0),
        "RZ 0/1": ("RZ", 0.0, 1.0),
    }
    points = [point(nom, e, f, a0, a1, e) for nom, (f, a0, a1) in configs.items()
              for e in range(-4, 15) if teb_theorique(f, a0, a1, e) > 3e-5]
    res = balayage("teb_filtre", points, recalculer, classpaths)
    fig, ax = plt.subplots(figsize=(7, 4.3))
    tracer_teb(ax, res, [
        ("NRZ ±1", r"NRZ antipodal $(-1,1)$ : $Q(\sqrt{2E_b/N_0})$"),
        ("NRZT ±1", r"NRZT antipodal $(-1,1)$"),
        ("NRZ 0/1", r"NRZ unipolaire $(0,1)$ : $Q(\sqrt{E_b/N_0})$"),
        ("RZ 0/1", r"RZ unipolaire $(0,1)$ : $Q(\sqrt{E_b/N_0})$"),
    ], lambda s, x: teb_theorique(configs[s][0], configs[s][1], configs[s][2], x), 14)
    ax.set_xlabel(r"$E_b/N_0$ (dB)")
    legende_theorie(ax)
    fig.savefig(os.path.join(FIGURES, "fig_teb_filtre.pdf"))
    plt.close(fig)
    return res


def fig_avant_apres(recalculer, classpaths):
    configs = {
        "NRZ ±1 étape 3": ("NRZ", -1.0, 1.0, "etape3"),
        "NRZ ±1 filtre": ("NRZ", -1.0, 1.0, "filtre"),
        "RZ 0/1 étape 3": ("RZ", 0.0, 1.0, "etape3"),
        "RZ 0/1 filtre": ("RZ", 0.0, 1.0, "filtre"),
    }
    points = [point(nom, e, f, a0, a1, e, recepteur=rec) for nom, (f, a0, a1, rec) in configs.items()
              for e in range(-4, 27, 2) if teb_theorique(f, a0, a1, e, recepteur=rec) > 3e-5]
    res = balayage("avant_apres", points, recalculer, classpaths)
    fig, ax = plt.subplots(figsize=(7, 4.3))
    tracer_teb(ax, res, [
        ("NRZ ±1 filtre", "NRZ ±1, filtre adapté (étape 4)"),
        ("NRZ ±1 étape 3", "NRZ ±1, échantillon central (étape 3)"),
        ("RZ 0/1 filtre", "RZ 0/1, filtre adapté (étape 4)"),
        ("RZ 0/1 étape 3", "RZ 0/1, échantillon central (étape 3)"),
    ], lambda s, x: teb_theorique(*configs[s][:3], x, recepteur=configs[s][3]), 26)
    ax.annotate("", xy=(8.0 + 14.77, 1.9e-4), xytext=(8.0, 1.9e-4),
                arrowprops=dict(arrowstyle="<->", color=ENCRE, lw=1))
    ax.text(8.0 + 14.77 / 2, 2.8e-4, r"$10\log_{10}30 \approx 14{,}8$ dB", ha="center",
            color=ENCRE, fontsize=9)
    ax.set_xlabel(r"$E_b/N_0$ (dB)")
    legende_theorie(ax, dessous=True)
    fig.savefig(os.path.join(FIGURES, "fig_avant_apres.pdf"))
    plt.close(fig)
    return res


def exporter_signal(dossier_outil, message, forme, a_min, a_max, ebn0, echos):
    args = [message, forme, str(N), f"{a_min:g}", f"{a_max:g}", ebn0, str(SEED)]
    args += [v for dt, ar in echos for v in (str(dt), f"{ar:g}")]
    cp = os.pathsep.join([dossier_outil, os.path.join(RACINE, "bin")])
    sortie = subprocess.run(JAVA + ["-cp", cp, "ExportSignal"] + args,
                            capture_output=True, text=True, check=True).stdout
    valeurs = np.array([[float(v) for v in ligne.split(";")] for ligne in sortie.split()])
    return valeurs[:, 0], valeurs[:, 1]


def fig_signal_echo(dossier_outil):
    message = "0110100101"
    fig, axes = plt.subplots(2, 1, figsize=(8, 4.2), sharex=True)
    cas = [("inf", r"écho $dt = 15$, $ar = 0{,}5$, sans bruit (étape 4a)"),
           ("10", r"même écho, $E_b/N_0 = 10$ dB (étape 4b)")]
    for ax, (ebn0, titre) in zip(axes, cas):
        emis, recu = exporter_signal(dossier_outil, message, "NRZ", -1.0, 1.0, ebn0, [(15, 0.5)])
        k = np.arange(len(emis))
        ax.plot(k, recu, color=COULEURS[0], lw=1.1, label="signal reçu")
        ax.step(k, emis, where="post", color=COULEURS[1], lw=1.4, label="signal émis")
        for b in range(1, len(message)):
            ax.axvline(b * N, color=GRILLE, lw=0.8, zorder=0)
        ax.set_title(titre, fontsize=10, color=ENCRE, loc="left")
        ax.set_ylabel("amplitude")
    axes[0].legend(loc="lower right", bbox_to_anchor=(1.0, 1.0), fontsize=8, ncol=2)
    axes[1].set_xlabel(f"indice d'échantillon (message {message}, NRZ, nbEch = {N})")
    fig.savefig(os.path.join(FIGURES, "fig_signal_echo.pdf"))
    plt.close(fig)


def fig_trajets_alpha(recalculer, classpaths):
    # grille décalée de 0,025 pour ne jamais tomber sur une égalité exacte avec le seuil
    alphas = np.round(np.arange(0.025, 1.2, 0.05), 3)
    retards = [10, 20, 30]
    points = [point(f"dt={dt} {rec}", a, "NRZ", 0.0, 1.0, math.inf, [(dt, a)], rec, nb_bits=20000)
              for dt in retards for rec in ("filtre", "etape3") for a in alphas]
    res = balayage("trajets_alpha", points, recalculer, classpaths)
    fig, axes = plt.subplots(1, 2, figsize=(10, 3.4), sharey=True)
    for ax, rec, titre in zip(axes, ("etape3", "filtre"),
                              ("échantillon central (étape 3)", "filtre adapté (étape 4)")):
        for i, dt in enumerate(retards):
            pts = sorted((r["x"], r["teb"]) for r in res if r["serie"] == f"dt={dt} {rec}")
            xs = np.linspace(0.0, 1.2, 241) + 0.0025
            ax.plot(xs, [teb_theorique("NRZ", 0.0, 1.0, math.inf, [(dt, a)], rec) for a in xs],
                    color=COULEURS[i], lw=1.4)
            ax.plot([p[0] for p in pts], [p[1] for p in pts], MARQUEURS[i], color=COULEURS[i], ms=5,
                    mec="white", mew=0.8, ls="none", label=f"$dt = {dt}$ échantillons")
        ax.set_title(titre, fontsize=10, color=ENCRE)
        ax.set_xlabel("amplitude relative $ar$ de l'écho")
    axes[0].set_ylabel("TEB (sans bruit)")
    axes[0].legend(loc="center left", fontsize=8)
    fig.savefig(os.path.join(FIGURES, "fig_trajets_alpha.pdf"))
    plt.close(fig)
    return res


def fig_trajets_ebn0(recalculer, classpaths):
    amplitudes = [0.0, 0.3, 0.5, 0.7]
    points = [point(f"ar={a}", e, "NRZ", -1.0, 1.0, e, [(15, a)] if a else ())
              for a in amplitudes for e in range(-4, 15)
              if teb_theorique("NRZ", -1.0, 1.0, e, [(15, a)] if a else ()) > 3e-5]
    res = balayage("trajets_ebn0", points, recalculer, classpaths)
    fig, ax = plt.subplots(figsize=(7, 4.3))
    tracer_teb(ax, res, [(f"ar={a}", "sans écho" if a == 0 else f"écho $dt = 15$, $ar = {a:g}$")
                         for a in amplitudes],
               lambda s, x: teb_theorique("NRZ", -1.0, 1.0, x,
                                          [(15, float(s[3:]))] if float(s[3:]) else ()), 14)
    ax.set_xlabel(r"$E_b/N_0$ (dB)")
    legende_theorie(ax)
    fig.savefig(os.path.join(FIGURES, "fig_trajets_ebn0.pdf"))
    plt.close(fig)
    return res


def resume(nom, res):
    print(f"\n{nom} : {len(res)} points")
    ecarts = [abs(r["teb"] - r["teb_th"]) / r["teb_th"] for r in res
              if r["teb_th"] > 0 and r["teb"] * r["nb_bits"] >= 100]
    if ecarts:
        print(f"  écart relatif simulation/théorie (points à >= 100 erreurs) : "
              f"médian {np.median(ecarts):.1%}, max {max(ecarts):.1%}")
    nuls = [r for r in res if r["teb_th"] == 0 and r["teb"] > 0]
    faux = [r for r in res if r["teb_th"] in (0.0, 0.25, 0.5) and r["nb_bits"] == 20000
            and abs(r["teb"] - r["teb_th"]) > 0.02]
    if nuls or faux:
        print(f"  ATTENTION : {len(nuls) + len(faux)} points sans bruit en désaccord avec la théorie")


def main():
    parser = argparse.ArgumentParser(description=__doc__.split("\n\n")[0])
    parser.add_argument("--recalculer", action="store_true", help="relancer toutes les simulations")
    args = parser.parse_args()

    if not os.path.isdir(os.path.join(RACINE, "bin", "simulateur")):
        sys.exit("Compiler d'abord le projet : ./compile")
    os.makedirs(FIGURES, exist_ok=True)

    temporaire = tempfile.mkdtemp(prefix="sit213_courbes_")
    try:
        classpaths = {"filtre": os.path.join(RACINE, "bin")}
        besoin_etape3 = args.recalculer or not all(
            os.path.exists(os.path.join(DONNEES, f + ".csv")) for f in ("avant_apres", "trajets_alpha"))
        if besoin_etape3:
            print(f"Compilation du récepteur de l'étape 3 ({REF_ETAPE3})...")
            classpaths["etape3"] = construire_etape3(temporaire)

        outil = os.path.join(temporaire, "outil")
        os.makedirs(outil)
        subprocess.run(["javac", "-cp", os.path.join(RACINE, "bin"), "-d", outil,
                        os.path.join(ICI, "outils", "ExportSignal.java")], check=True)

        fig_filtres()
        fig_signal_echo(outil)
        resume("TEB filtre adapté", fig_teb_filtre(args.recalculer, classpaths))
        resume("Étape 3 / filtre adapté", fig_avant_apres(args.recalculer, classpaths))
        resume("Trajets sans bruit", fig_trajets_alpha(args.recalculer, classpaths))
        resume("Trajets avec bruit", fig_trajets_ebn0(args.recalculer, classpaths))
    finally:
        shutil.rmtree(temporaire, ignore_errors=True)
    print(f"\nFigures dans {FIGURES}, données dans {DONNEES}")


if __name__ == "__main__":
    main()
