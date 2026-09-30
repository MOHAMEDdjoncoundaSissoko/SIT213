# Tests ajoutés par Laurent

Ce document résume les nouveaux tests ajoutés dans `TransmetteurTest.java` et `SimulateurTest.java`.

## Transmetteur logique vers analogique

- `testTLANRZTRampesEntreBits` : vérifie les rampes NRZT montante et descendante entre des bits opposés, avec des amplitudes de -2 à 2.
- `testTLAFormeInconnue` : vérifie qu'une forme d'onde non reconnue émet directement le niveau correspondant au bit.
- `testTLASignalVide` : vérifie qu'un signal vide est émis et transmis à la destination connectée.

## Canal analogique bruité

- `testTABruitReproductible` : vérifie qu'une même seed produit le même signal bruité, que le bruit modifie le signal et que la destination reçoit la sortie.
- `testTABruitSignalNul` : vérifie qu'un signal de puissance nulle reste nul après transmission.
- `testTABruitSignalVide` : vérifie qu'un signal vide est accepté et transmis à la destination.
- `testTABruitSansSeed` : vérifie le chemin sans seed, la conservation du nombre d'échantillons et la génération de valeurs finies.

## Canal à trajets multiples

- `testTATMEchoRetarde` : vérifie qu'un écho d'atténuation 0,5 est absent avant son retard, puis ajouté aux échantillons suivants; vérifie aussi le transfert à la destination.
- `testTATMBruitReproductible` : vérifie qu'une même seed produit le même bruit et que la longueur du signal est conservée.
- `testTATMSansSeedSignalVide` : vérifie le chemin sans seed avec un signal vide et sa transmission à la destination.
- `testTATMTrajetsInvalides` : vérifie le rejet de tableaux d'atténuations et de retards de tailles différentes, d'un nombre de trajets supérieur au maximum et d'un retard négatif.
- `testTATMMemeBruitQueCanalGaussien` : vérifie que, sans trajet indirect et avec la même seed, le bruit est identique à celui de `TransmetteurAnalogiqueBruite` (même formule Eb/N0).

## Simulateur et option `-ti`

- `testTrajetsActifs` : vérifie que deux échos forts (`-ti 10 0.9 20 0.9`) produisent un TEB non nul sans `-snrpb`.
- `testTrajetsSansBruit` : vérifie qu'un écho faible sans `-snrpb` donne un TEB nul (aucun bruit ajouté implicitement).
- `testTrajetsEchoNulMemeBruit` : vérifie qu'un écho d'amplitude nulle donne le même TEB que `-snrpb` seul.
- `testTrajetsSansSeed` : vérifie le chemin sans seed et la lecture de plusieurs couples suivis d'autres options.
- `testNombreTrajetsInvalide` : vérifie le rejet de `-ti` sans couple et de plus de 5 couples.
- `testValeursTrajetsInvalides` : vérifie le rejet d'un `ar` non numérique ou manquant, d'un `dt` négatif ou trop grand.

## Corrections associées

- Le branchement du simulateur vérifiait deux fois l'option `canalBruite`. La première condition sélectionne maintenant `canalTrajetsMultiples`.
- L'option `-trajets n α τ ...` est remplacée par `-ti dt ar [dt ar ...]`, conformément à la commande unique.
- `TransmetteurAnalogiqueTrajetsMultiples` hérite de `TransmetteurAnalogiqueBruite` : la formule du bruit (qui oubliait le facteur `nbEch`) n'est plus dupliquée.
- Sans `-snrpb`, le canal à trajets multiples n'ajoute plus de bruit (Eb/N0 infini) au lieu d'utiliser Eb/N0 = 0 dB par défaut.
