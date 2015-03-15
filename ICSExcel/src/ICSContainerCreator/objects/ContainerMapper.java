package ICSContainerCreator.objects;

import java.util.HashMap;
import java.util.Map;

public class ContainerMapper {
    Map<String, Map<String, Map<Integer, Double>>> containerMap;

    public ContainerMapper() {
        containerMap = new HashMap<>();
    }

    public synchronized void addContainer(String product, String branch, double weight, int week) {
        if (containerMap.containsKey(branch)) {
            Map<String, Map<Integer, Double>> productMap = containerMap.get(branch);
            if (productMap.containsKey(product)) {
                Map<Integer, Double> weekMap = productMap.get(product);

                if (weekMap.containsKey(week)) {
                    Double value = weekMap.remove(week);
                    value = value + weight;
                    weekMap.put(week, value);
                } else {
                    weekMap.put(week, weight);
                }
            } else {
                productMap.put(product, new HashMap<>());
                addContainer(product, branch, weight, week);
            }
        } else {
            containerMap.put(branch, new HashMap<>());
            addContainer(product, branch, weight, week);
        }
    }

    public Map<String, Map<String, Map<Integer, Double>>> getContainerMap() {
        return containerMap;
    }
}
