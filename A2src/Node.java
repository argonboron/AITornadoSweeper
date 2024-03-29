public class Node {
    int x, y;
    char value;
    String id;

    int getX() {
      return this.x;
    }

    void setValue(char value) {
        this.value = value;
    }

    int getY() {
      return this.y;
    }

    char getValue() {
      return this.value;
    }

    void setValue(int val) {
      value = (char) val;
    }

    String getID() {
      return this.id;
    }

    public Node(int x, int y, char value, String id){
        this.x=x;
        this.y=y;
        this.value=value;
        this.id = "C"+id;
    }
  
  // Overriding equals() to compare two Complex objects
  @Override
  public boolean equals(Object o) {
    if (o == this) {
        return true;
    }
    if (!(o instanceof Node)) {
        return false;
    }
    Node node = (Node) o;
    return (Double.compare(x, node.x) == 0 && Double.compare(y, node.y) == 0);
  }
}
