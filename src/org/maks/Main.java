package org.maks;

import org.maks.model.DecisionNode;
import org.maks.model.KeyWrapper;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws FileNotFoundException {
        Map<KeyWrapper, List<DecisionNode>> knowledgeDatabase = KnowledgeDatabaseUtils.createKnowledgeDatabase("knowledgeDB.data");
        Scanner sc = new Scanner(System.in);
        Visualizer.showSymptoms();
        System.out.println();
        System.out.println("Norādiet simptoma identifikatorus, atdalot tos ar komatiem: ");
        List<Integer> symptomIds = Visualizer.getSymptomsFromTheUser(sc);
        Visualizer.displayPossibleDisease(symptomIds, new ArrayList<>(), knowledgeDatabase, sc);
        sc.close();
    }

}
