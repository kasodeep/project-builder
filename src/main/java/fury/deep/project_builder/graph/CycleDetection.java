package fury.deep.project_builder.graph;

import fury.deep.project_builder.entity.task.TaskDependency;

import java.util.*;

public class CycleDetection {

    public static Map<String, List<String>> buildGraph(List<TaskDependency> existing, String taskId, List<String> newDependencies) {
        Map<String, List<String>> graph = new HashMap<>();

        for (TaskDependency td : existing) {
            graph.put(td.getTaskId(), new ArrayList<>(td.getDependencies()));
        }

        graph.put(taskId, new ArrayList<>(newDependencies));
        return graph;
    }

    public static void checkForCycle(Map<String, List<String>> graph) {
        Set<String> visited = new HashSet<>();
        Set<String> recStack = new HashSet<>();

        for (String node : graph.keySet()) {
            if (!visited.contains(node)) {
                if (dfs(node, graph, visited, recStack)) {
                    throw new IllegalStateException("Cyclic dependency detected");
                }
            }
        }
    }

    private static boolean dfs(
            String node,
            Map<String, List<String>> graph,
            Set<String> visited,
            Set<String> recStack
    ) {
        visited.add(node);
        recStack.add(node);

        for (String neighbor : graph.getOrDefault(node, List.of())) {
            if (!visited.contains(neighbor)) {
                if (dfs(neighbor, graph, visited, recStack)) {
                    return true;
                }
            } else if (recStack.contains(neighbor)) {
                return true;
            }
        }

        recStack.remove(node);
        return false;
    }
}
