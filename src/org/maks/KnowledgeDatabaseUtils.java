package org.maks;

import org.maks.model.DecisionNode;
import org.maks.model.KeyWrapper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class KnowledgeDatabaseUtils {

    private static final String SPLIT_REGEX_BY_COMMA = ",";
    private static final String SPLIT_REGEX_BY_ARROW = "->";

    public static Map<KeyWrapper, List<DecisionNode>> createKnowledgeDatabase(String fileName) throws FileNotFoundException {
        Map<KeyWrapper, List<DecisionNode>> knowledgeDatabase = new HashMap<>();
        Path filePath = Paths.get(fileName);
        if (Files.exists(filePath)) {
            try (Stream<String> lines = Files.lines(Paths.get(fileName), Charset.defaultCharset())) {
                lines.forEach(line -> processDBFileLines(knowledgeDatabase, line));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            throw new FileNotFoundException("File with the name '" + fileName + "' does not exists");
        }

        return knowledgeDatabase;
    }

    public static List<Map.Entry<KeyWrapper, List<DecisionNode>>> findPossibleDiseases(Map<KeyWrapper, List<DecisionNode>> knowledgeDatabase, List<Integer> positiveSymptomIds) {
        return knowledgeDatabase.entrySet()
                .stream()
//                .collect(Collectors.toMap(Map.Entry::getKey, e -> findMostAccurateListOfNodes(e.getValue(), positiveSymptomIds)))
//                .entrySet().stream()
                .collect(Collectors.toMap(e -> e, e -> findMatchCountAcrossNodes(positiveSymptomIds, e.getValue())))
                .entrySet().stream()
                .collect(maxList(Map.Entry.comparingByValue()))
                .stream().map(Map.Entry::getKey)
                .collect(toList());
    }

    static <T> Collector<T, ?, List<T>> maxList(Comparator<? super T> comp) {
        return Collector.of(
                ArrayList::new,
                (list, t) -> {
                    int c;
                    if (list.isEmpty() || (c = comp.compare(t, list.get(0))) == 0) {
                        list.add(t);
                    } else if (c > 0) {
                        list.clear();
                        list.add(t);
                    }
                },
                (list1, list2) -> {
                    if (list1.isEmpty()) {
                        return list2;
                    }
                    if (list2.isEmpty()) {
                        return list1;
                    }
                    int r = comp.compare(list1.get(0), list2.get(0));
                    if (r < 0) {
                        return list2;
                    } else if (r > 0) {
                        return list1;
                    } else {
                        list1.addAll(list2);
                        return list1;
                    }
                });
    }

    private static List<DecisionNode> findMostAccurateListOfNodes(List<List<DecisionNode>> lists, List<Integer> positiveSymptomIds) {
        return lists.stream()
                .collect(Collectors.toMap(l -> l, l -> findMatchCountAcrossNodes(positiveSymptomIds, l)))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElseThrow()
                .getKey();
    }

    private static long findMatchCountAcrossNodes(List<Integer> positiveSymptomIds, List<DecisionNode> decisionNodes) {
        return decisionNodes.stream()
                .filter(n -> n.getFlag() && positiveSymptomIds.stream().anyMatch(id -> id.equals(n.getValue())))
                .count();
    }

    private static void processDBFileLines(Map<KeyWrapper, List<DecisionNode>> knowledgeDatabase, String line) {
        String[] tmpArray = line.split(SPLIT_REGEX_BY_ARROW);
        List<DecisionNode> nodeIds = new ArrayList<>(List.of(tmpArray[1].split(SPLIT_REGEX_BY_COMMA)))
                .stream()
                .map(KnowledgeDatabaseUtils::decisionNodeMapper)
                .collect(toList());
        KeyWrapper diseaseId = new KeyWrapper(Integer.parseInt(tmpArray[0]));
        knowledgeDatabase.computeIfAbsent(diseaseId, k -> nodeIds);
    }

    private static DecisionNode decisionNodeMapper(String strNode) {
        String parsedStr = strNode.replaceAll("[()]", "").trim();
        return new DecisionNode(Integer.valueOf(String.valueOf(
                parsedStr.length() == 2 ? parsedStr.charAt(0) : parsedStr.substring(0, 2))),
                parsedStr.charAt(parsedStr.length() - 1) == '+');
    }
}
