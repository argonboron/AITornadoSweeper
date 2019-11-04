public class Node {
    int x, y;
    char value;

    int getX() {
      return this.x;
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
    public Node(int x, int y, char value){
        this.x=x;
        this.y=y;
        this.value=value;
    }
    // Overriding equals() to compare two Complex objects
 @Override
 public boolean equals(Object o) {

     // If the object is compared with itself then return true
     if (o == this) {
         return true;
     }

     /* Check if o is an instance of Complex or not
       "null instanceof [type]" also returns false */
     if (!(o instanceof Node)) {
         return false;
     }

     // typecast o to Complex so that we can compare data members
    Node node = (Node) o;

     // Compare the data members and return accordingly
     return Double.compare(x, node.x) == 0
             && Double.compare(y, node.y) == 0;
 }
}
