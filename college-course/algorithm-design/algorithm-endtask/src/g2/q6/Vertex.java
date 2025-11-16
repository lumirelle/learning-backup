package g2.q6;

import java.util.ArrayList;
import java.util.List;

public class Vertex {

  private final List<Vertex> nexts = new ArrayList<>();
  private int id;

  public void addNext(Vertex v) {
    nexts.add(v);
  }

  public List<Vertex> getNexts() {
    return nexts;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String toString() {
    return String.valueOf(id);
  }

}
