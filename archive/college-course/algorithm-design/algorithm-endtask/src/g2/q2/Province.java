package g2.q2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Province {

  private final String name;

  private final List<Province> neighbours;

  public Province(String name) {
    this.name = name;
    neighbours = new ArrayList<>();
  }

  private void addNeighbour(Province province) {
    if (!neighbours.contains(province)) {
      neighbours.add(province);
    } else {
      System.out.println("省份" + this + "已经和省份" + province + "相邻");
    }
  }

  // 将本省份和参数省份互相添加为相邻
  public void addNeighboursBoth(Province... provinces) {
    for (Province province : provinces) {
      addNeighbour(province);
      province.addNeighbour(this);
    }
  }

  public void test(List<Province> targets) {
    printAllBestPath(findAllBestPath(targets));
  }

  private Map<Province, List<Province>> findAllBestPath(List<Province> targets) {
    List<Province> path = new ArrayList<>();
    path.add(this);

    Map<Province, List<Province>> allBestPath = new HashMap<>();
    for (Province target : targets) {
      allBestPath.put(target, new ArrayList<>());
    }

    dfsFind(path, allBestPath);

    return allBestPath;
  }

  private void dfsFind(List<Province> path,
      Map<Province, List<Province>> allBestPath) {
    // 如果这个省份是目标省份之一，就更新最短路径
    if (allBestPath.containsKey(this)) {
      // 如果最短路径为空，或当前路径比最短路径还短，就更新最短路径
      // 注意需要复制一份 path
      List<Province> bestPath = allBestPath.get(this);
      if (bestPath.isEmpty() || path.size() < bestPath.size()) {
        bestPath.clear();
        bestPath.addAll(path);
      }
    }

    for (Province neighbour : neighbours) {
      // 如果已经途经过这个省份，就不再走了
      if (path.contains(neighbour)) {
        continue;
      }
      path.add(neighbour);
      neighbour.dfsFind(path, allBestPath);
      path.remove(neighbour);
    }
  }

  private void printAllBestPath(Map<Province, List<Province>> bestPaths) {
    for (Province target : bestPaths.keySet()) {
      System.out.println("目标省份：" + target);
      System.out.print("路径：");
      for (int i = 0; i < bestPaths.get(target).size(); i++) {
        System.out.print(bestPaths.get(target).get(i));
        if (i != bestPaths.get(target).size() - 1) {
          System.out.print(" -> ");
        }
      }
      System.out.println();
      System.out.println("途经省份：" + (bestPaths.get(target).size() - 2));
      System.out.println();
    }

    System.out.println("途径省份数量是否都小于等于2：" + bestPaths.values()
        .stream()
        .allMatch(path -> path.size() - 2 <= 2));
  }

  @Override
  public String toString() {
    return name;
  }

}
