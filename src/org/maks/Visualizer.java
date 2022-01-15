package org.maks;

import org.maks.model.DecisionNode;
import org.maks.model.KeyWrapper;

import java.util.*;
import java.util.stream.Collectors;

public class Visualizer {
    public static void showSymptoms() {
        System.out.println(Constants.terminology.entrySet().stream()
                .filter(e -> !List.of(15, 16, 17, 18, 19, 20, 21, 22, 23, 24).contains(e.getKey()))
                .map(e -> e.getKey() + (e.getKey().toString().length() == 1 ? "  " : " ")
                        + ": " + e.getValue())
                .collect(Collectors.joining("\n"))
        );
    }

    public static List<Integer> getSymptomsFromTheUser(Scanner sc) {
        String userInput = sc.nextLine();
        return Arrays.stream(userInput.split(","))
                .map(id -> Integer.parseInt(id.trim()))
                .collect(Collectors.toList());
    }

    public static void displayPossibleDisease(List<Integer> symptomIds, List<Integer> symptomsToCheck, Map<KeyWrapper, List<DecisionNode>> knowledgeDatabase, Scanner sc) {
        List<Map.Entry<KeyWrapper, List<DecisionNode>>> possibleDiseases = KnowledgeDatabaseUtils.findPossibleDiseases(knowledgeDatabase, symptomIds);

        if (possibleDiseases.size() > 1) {
            if (symptomsToCheck.isEmpty()) {
                symptomsToCheck = possibleDiseases.stream()
                        .map(Map.Entry::getValue)
                        .flatMap(Collection::stream)
                        .filter(n -> n.getFlag() && !symptomIds.contains(n.getValue()))
                        .map(DecisionNode::getValue)
                        .distinct()
                        .sorted()
                        .collect(Collectors.toList());
            }

            List<KeyWrapper> diseasesWithLowPossibilities = new ArrayList<>(); // list for diseases that can be excluded
            while (possibleDiseases.size() > 1 && !symptomsToCheck.isEmpty()) {
                for (Integer symptom : new ArrayList<>(symptomsToCheck)) {
                    if (!symptomsToCheck.contains(symptom)) {
                        continue;
                    }
                    System.out.println("Vai šāds simptoms ir? (rakstiet '+' ja ir): " + (Constants.terminology.get(symptom)));
                    if (sc.nextLine().equals("+")) {
                        symptomIds.add(symptom);
                        // check for symptoms that can be skipped and if found delete it from symptomToCheck list
                        for (Map.Entry<KeyWrapper, List<DecisionNode>> e : possibleDiseases) {
                            if (e.getValue().stream().anyMatch(n -> n.getValue().equals(symptom) && !n.getFlag())) {
                                diseasesWithLowPossibilities.add(e.getKey());
                                List<Integer> listWithPossibleNodeIdsToSkip = e.getValue().stream()
                                        .map(DecisionNode::getValue)
                                        .collect(Collectors.toList());
                                listWithPossibleNodeIdsToSkip.removeAll(symptomIds);
                                symptomsToCheck.removeAll(listWithPossibleNodeIdsToSkip);
                            }
                        }
                        symptomsToCheck.remove(symptom);
                        break;
                    } else {
                        // check for symptoms that can be skipped and if found delete it from symptomToCheck list
                        for (Map.Entry<KeyWrapper, List<DecisionNode>> e : possibleDiseases) {
                            if (e.getValue().stream().anyMatch(n -> n.getValue().equals(symptom) && n.getFlag())) {
                                diseasesWithLowPossibilities.add(e.getKey());
                                List<Integer> listWithPossibleNodeIdsToSkip = e.getValue().stream()
                                        .map(DecisionNode::getValue)
                                        .collect(Collectors.toList());
                                listWithPossibleNodeIdsToSkip.removeAll(symptomIds);
                                symptomsToCheck.removeAll(listWithPossibleNodeIdsToSkip);
                            }
                        }
                        symptomsToCheck.remove(symptom);
                    }
                    if (symptomsToCheck.isEmpty()) {
                        break;
                    }
                }

                possibleDiseases = KnowledgeDatabaseUtils.findPossibleDiseases(knowledgeDatabase, symptomIds).stream()
                        .filter(e -> !diseasesWithLowPossibilities.contains(e.getKey()))
                        .collect(Collectors.toList());
            }
        }

        // display final results
        if (possibleDiseases.size() == 1 && possibleDiseases.get(0).getValue().stream()
                .filter(n -> symptomIds.contains(n.getValue()) && n.getFlag())
                .map(DecisionNode::getValue)
                .collect(Collectors.toList()).containsAll(symptomIds)) {
            System.out.println("Iespejāma slimība: " + Constants.terminology.get(possibleDiseases.get(0).getKey().getKey()));
        } else {
            System.out.println("Nav iespējas noteikt slimību.");
        }
    }
}
