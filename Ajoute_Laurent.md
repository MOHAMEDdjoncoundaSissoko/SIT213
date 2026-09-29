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
- `testTATMTrajetsInvalides` : vérifie le rejet de tableaux d'atténuations et de retards de tailles différentes, ainsi que d'un nombre de trajets supérieur au maximum.

## Simulateur et options `-trajets`

- `testTrajetsActifs` : vérifie qu'un canal à échos est réellement activé sans option `-snrpb`, avec un TEB non nul sur un long message.
- `testTrajetsSansSeed` : vérifie que le canal à échos fonctionne aussi sans seed et renvoie un TEB fini.
- `testNombreTrajetsInvalide` : vérifie le rejet de zéro trajet et d'un nombre supérieur à la limite.
- `testValeursTrajetsInvalides` : vérifie le rejet d'une atténuation non numérique et d'un retard manquant.

## Correction associée

Le branchement du simulateur vérifiait deux fois l'option `canalBruite`. La première condition sélectionne maintenant `canalTrajetsMultiples`, ce qui rend `-trajets` fonctionnel et laisse `-snrpb` utiliser son canal bruité classique.
