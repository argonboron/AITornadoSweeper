import java.util.ArrayList;

public class Agent {
  ArrayList<int[]> explored = new ArrayList<>();
  static ArrayList<boolean[]> clauses = new ArrayList<>();
  ArrayList<ArrayList<Node>> nodeMap = new ArrayList<>();
  boolean gameOver;
  char[][] knownMap;
  char[][] master;
  int numberOfTornadoes;

  void satX() {
    int id = 0;
    for (int x = 0; x < knownMap.length; x++) {
      ArrayList<Node> row = new ArrayList<>();
      for (int y = 0; y < knownMap[0].length; y++) {
        row.add(new Node(x, y, knownMap[x][y], id));
        id++;
      }
      nodeMap.add(row);
    }
    //Get uncovered with covered neighbours num
    //Loop through them
    ArrayList<String> formula = new ArrayList<String>();
    for (int x = 0; x < knownMap.length; x++) {
      for (int y = 0; y < knownMap[0].length; y++) {
        if (knownMap[x][y] != '?' && knownMap[x][y] != 'f' && getUnknownNeighboursNum(x, y) > 0) {
          formula.add(getKB(x,y));
        }
      }
    }




    // PlBeliefSet kb = new PlBeliefSet();
    // PlParser parser = new PlParser();
    // kb.add((PlFormula) parser.parseFormula("(A || B)&& !(A || !B)"));
    // Conjunction conj=kb.toCnf();
  }

  String getKB(int x, int y) {
    clauses.clear();
    int n = getUnknownNeighboursNum(x, y);
    int c = knownMap[x][y];
    boolean[] clausesArr = new boolean[n];
    generateAllBinaryClauses(n, clausesArr, 0, c);
    for(boolean[] clause : clauses) {
      
    }
  }

  static int weightOf(boolean[] arr) {
    int sum = 0;
    for (boolean val : arr) {
        if (val) {
          sum++;
        }
    }
    return sum;
  }

  static void generateAllBinaryClauses(int n, boolean arr[], int i, int c) { 
    if (i == n) { 
      if (weightOf(arr) == c){
        clauses.add(arr);
      }
        return; 
    } 
  
    // First assign "0" at ith position 
    // and try for all other permutations 
    // for remaining positions 
    arr[i] = false; 
    generateAllBinaryClauses(n, arr, i + 1, c); 
  
    // And then assign "1" at ith position 
    // and try for all other permutations 
    // for remaining positions 
    arr[i] = true; 
    generateAllBinaryClauses(n, arr, i + 1, c); 
} 

  int probe(int x, int y) {
    char value = master[x][y];
    knownMap[x][y] = value;
    System.out.println("--> probe " + x + " " + y);
    new Board(knownMap).printBoard();
    if (value == 't') {
      return -1;
    } else {
      explored.add(new int[] { x, y });
      return value;
    }
  }

  void flag(int x, int y) {
    System.out.println("--> flag " + x + " " + y);
    knownMap[x][y] = 'f';
  }

  int[] getRandomCoords() {
    int x = 0;
    int y = 0;
    boolean unexplored = false;
    while (!unexplored) {
      x = (int) (knownMap.length * Math.random());
      y = (int) (knownMap[0].length * Math.random());
      unexplored = (knownMap[x][y] == '?');
    }
    return new int[] { x, y };
  }

  void play(int playType) {

    new Board(knownMap).printBoard();
    if (playType == 1) {
      // RPX
      while (!gameOver) {
        int[] randomCoords = getRandomCoords();
        int x = randomCoords[0];
        int y = randomCoords[1];
        int value = probe(x, y);
        if (value < 0) {
          gameOver = true;
          System.out.println("GAME OVER - Tornado Probed");
        } else if (explored.size() == knownMap.length * knownMap[0].length - numberOfTornadoes) {
          gameOver = true;
          System.out.println("YOU WIN!");
        }
      }
    } else if (playType == 2) {
      // SPX
      singlePointStrategy();
    } else if (playType == 3) {
      // SATX
    }
  }

  public void singlePointStrategy() {
    int allMarked = 0;
    int allFree = 0;
    while (!gameOver & (allMarked + allFree < 2)) {
      System.out.println("-------------");
      allFree = checkAllFreeNodes();
      if (allFree == -1) {
        gameOver = true;
        System.out.println("GAME OVER");
        break;
      }
      allMarked = checkAllMarkedNodes();
      new Board(knownMap).printBoard();
      if (explored.size() == knownMap.length * knownMap[0].length - numberOfTornadoes) {
        gameOver = true;
        System.out.println("YOU WIN!");
      }
    }
    if (!gameOver) {
      System.out.println("Stuck");
    }
  }

  public int checkAllFreeNodes() {
    int stuck = 1;
    for (int x = 0; x < knownMap.length; x++) {
      for (int y = 0; y < knownMap[0].length; y++) {
        if (knownMap[x][y] != '?' && knownMap[x][y] != 'f') {
          int val = knownMap[x][y] - '0';
          // System.out.println("afn "+x+","+y+" " + (val == getFlaggedNeighboursNum(x,
          // y)));
          if (val == getFlaggedNeighboursNum(x, y) && getUnknownNeighboursNum(x, y) > 0) {
            stuck = 0;
            if (!probeNeighbours(x, y)) {
              return -1;
            }
          }
        }
      }
    }
    return stuck;
  }

  public int checkAllMarkedNodes() {
    int stuck = 1;
    for (int x = 0; x < knownMap.length; x++) {
      for (int y = 0; y < knownMap[0].length; y++) {
        if (knownMap[x][y] != '?' && knownMap[x][y] != 'f') {
          int val = knownMap[x][y] - '0';
          if (val - getFlaggedNeighboursNum(x, y) == getUnknownNeighboursNum(x, y)
              && getUnknownNeighboursNum(x, y) > 0) {
            stuck = 0;
            flagNeighbours(x, y);
          }
        }
      }
    }
    return stuck;
  }

  public boolean probeNeighbours(int x, int y) {
    for (int xMod = -1; xMod < 2; xMod++) {
      for (int yMod = -1; yMod < 2; yMod++) {
        if (!(xMod == 0 && yMod == 0)) {
          if (!(xMod == -1 && yMod == 1) && !(xMod == 1 && yMod == -1)) {
            int neighbourX = x + xMod;
            int neighbourY = y + yMod;
            if (!(neighbourX < 0 || neighbourY < 0 || neighbourX >= knownMap[0].length
                || neighbourY >= knownMap[0].length) && knownMap[neighbourX][neighbourY] == '?') {
              if (probe(neighbourX, neighbourY) == -1) {
                return false;
              }
            }
          }
        }
      }
    }
    return true;
  }

  public void flagNeighbours(int x, int y) {
    for (int xMod = -1; xMod < 2; xMod++) {
      for (int yMod = -1; yMod < 2; yMod++) {
        if (!(xMod == 0 && yMod == 0)) {
          if (!(xMod == -1 && yMod == 1) && !(xMod == 1 && yMod == -1)) {
            int neighbourX = x + xMod;
            int neighbourY = y + yMod;
            if (!(neighbourX < 0 || neighbourY < 0 || neighbourX >= knownMap[0].length
                || neighbourY >= knownMap[0].length) && knownMap[neighbourX][neighbourY] == '?') {
              flag(neighbourX, neighbourY);
            }
          }
        }
      }
    }
  }

  public int getFlaggedNeighboursNum(int x, int y) {
    int count = 0;
    for (int xMod = -1; xMod < 2; xMod++) {
      for (int yMod = -1; yMod < 2; yMod++) {
        if (!(xMod == 0 && yMod == 0)) {
          if (!(xMod == -1 && yMod == 1) && !(xMod == 1 && yMod == -1)) {
            int neighbourX = x + xMod;
            int neighbourY = y + yMod;
            if (!(neighbourX < 0 || neighbourY < 0 || neighbourX >= knownMap[0].length
                || neighbourY >= knownMap[0].length) && knownMap[neighbourX][neighbourY] == 'f') {
              count++;
            }
          }
        }
      }
    }
    return count;
  }

  public int getUnknownNeighboursNum(int x, int y) {
    int count = 0;
    for (int xMod = -1; xMod < 2; xMod++) {
      for (int yMod = -1; yMod < 2; yMod++) {
        if (!(xMod == -1 && yMod == 1) && !(xMod == 1 && yMod == -1)) {
          if (!(xMod == 0 && yMod == 0)) {
            int neighbourX = x + xMod;
            int neighbourY = y + yMod;
            if (!(neighbourX < 0 || neighbourY < 0 || neighbourX >= knownMap[0].length
                || neighbourY >= knownMap[0].length) && knownMap[neighbourX][neighbourY] == '?') {
              count++;
            }
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
    for (int x = 0; x < knownMap.length; x++) {
      for (int y = 0; y < knownMap[0].length; y++) {
        knownMap[x][y] = '?';
      }
    }
    knownMap[0][0] = master[0][0];
    explored.add(new int[] { 0, 0 });
    knownMap[knownMap.length / 2][knownMap[0].length / 2] = master[master.length / 2][master[0].length / 2];
    explored.add(new int[] { knownMap.length / 2, knownMap[0].length / 2 });
  }
}
