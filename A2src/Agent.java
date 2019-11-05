import java.util.ArrayList;

public class Agent {
    ArrayList<int[]> explored = new ArrayList<>();
    boolean gameOver;
    char[][] knownMap;
    char[][] master;
    int numberOfTornadoes;

    int probe(int x, int y) {
        char value = master[x][y];
        knownMap[x][y] = value;
        if (value =='t'){
            return -1;
        } else {
            explored.add(new int[]{x,y});
            return value;
        }
    }

    void flag(int x, int y) {
        knownMap[x][y] = 'f';
    }

    int[] getRandomCoords(){
        int x = 0;
        int y = 0;
        boolean unexplored = false;
        while (!unexplored) {
            x = (int)(knownMap.length * Math.random());
            y = (int)(knownMap[0].length * Math.random());
            unexplored = (knownMap[x][y]=='?');
        }
        return new int[]{x,y};
    }

    void play(int playType){
        if (playType ==1){
            //RPX
            while (!gameOver){
                int[] randomCoords = getRandomCoords();
                int x = randomCoords[0];
                int y = randomCoords[1];
                int value = probe(x, y);
                if (value < 0 || explored.size() == knownMap.length*knownMap[0].length - numberOfTornadoes) {
                    gameOver = true;
                }
            }
        } else if (playType==2){
            //SPX
            singlePointStrategy();
        } else if (playType==3){
            //SATX
        }
    }

    public void singlePointStrategy() {
        ArrayList<Node> knownNodes = new ArrayList<>();
        ArrayList<Node> neighbours = new ArrayList<>();
        knownNodes.add(new Node(0, 0, knownMap[0][0]));
        knownNodes.add(new Node(knownMap.length/2, knownMap[0].length/2, knownMap[knownMap.length/2][knownMap[0].length/2]));
        for (Node node : knownNodes) {
            if (node.getValue() == '0') {
                neighbours = getNeighbours(node.getX(), node.getY());
                for (Node neighbour : neighbours) {
                  if (neighbour.getValue() == '?') {
                    neighbour.setValue(probe(neighbour.getX(), neighbour.getY()));
                    knownNodes.add(neighbour);
                  }
                }
                neighbours.clear();
            } else {
                neighbours = getNeighbours(node.getX(), node.getY());
                for (Node neighbour : neighbours) {
                  if (!knownNodes.contains(neighbour)) {
                    knownNodes.add(neighbour);
                  }
                }
                neighbours.clear();
            }
        }
        for (Node node : knownNodes) {
          if (node.getValue() == '?') {
            neighbours = getNeighbours(node.getX(), node.getY());
            for (Node neighbour : neighbours) {
              ArrayList<Node> surroundingNeighbours = new ArrayList<Node>();
              if (getFlaggedNeighbours(node.getX(), node.getY()) == neighbour.getValue()) {
                surroundingNeighbours = getNeighbours(neighbour.getX(), neighbour.getY());
                for (Node surroundingNeighbour : surroundingNeighbours) {
                  if (surroundingNeighbour.getValue()=='?'){
                    surroundingNeighbour.setValue(probe(surroundingNeighbour.getX(), surroundingNeighbour.getY()));
                    //replace(knownNodes, surroundingNeighbour);
                  }
                }
              }
            }
          }
        }

        //Uncover all free neighbours
        //If value = 0 click all neighbors
        //Repeat for all 0's in neighbours

        //For each ?
        //  For all nighbours
        //      All Free
        //      If f in neighbours == value click all ?

        //      All marked
        //      If ? in neighbours == value flagg all ?

    }

    public boolean probeNeighbours(int x, int y) {
        ArrayList<Node> neighbours = new ArrayList<>();
        for (int xMod = -1; xMod < 2; xMod++) {
            for (int yMod = -1; yMod < 2; yMod++) {
              if (!(xMod == -1 && yMod == 1) || !(xMod == 1 && yMod == -1)) {
                int neighbourX = x+xMod;
                int neighbourY = y+yMod;
                if (!(neighbourX < 0 || neighbourY < 0 || neighbourX >= knownMap[0].length || neighbourY >= knownMap[0].length) && knownMap[neighbourX][neighbourY] =='?') {
                  int num = probe(neighbourX, neighbourY);
                  if (num != -1){
                    neighbours.add(new Node(neighbourX, neighbourY, (char) num));
                  } else {
                    return false;
                  }
                }
              }
            }
        }
        return true;
    }

    public int getFlaggedNeighbours(int x, int y) {
        int count = 0;
        for (int xMod = -1; xMod < 2; xMod++) {
            for (int yMod = -1; yMod < 2; yMod++) {
              if (!(xMod == 0 && yMod == 0)) {
                int neighbourX = x+xMod;
                int neighbourY = y+yMod;
                if (!(neighbourX < 0 || neighbourY < 0 || neighbourX >= knownMap[0].length || neighbourY >= knownMap[0].length) &&  knownMap[neighbourX][neighbourY]=='f') {
                  count++;
                }
              }
            }
        }
        return count;
    }

    public Agent(char[][] masterMap, int t) {
        master = masterMap;
        numberOfTornadoes = t;
        knownMap = new char[master.length][master[0].length];
        for(int x = 0; x < knownMap.length; x++){
            for(int y = 0; y < knownMap[0].length; y++){
                knownMap[x][y] = '?';
            }
        }
        knownMap[0][0] = master[0][0];
        knownMap[knownMap.length/2][knownMap[0].length/2] = master[master.length/2][master[0].length/2];
    }
}
