package g2.q1;

public class Edge implements Comparable<Edge> {

  int src;
  int dest;
  double weight;

  public Edge(int src, int dest, double weight) {
    this.src = src;
    this.dest = dest;
    this.weight = weight;
  }

  @Override
  public int compareTo(Edge other) {
    return Double.compare(this.weight, other.weight);
  }

}
