


import net.sf.tweety.logics.pl.PlBeliefSet;
import net.sf.tweety.logics.pl.parser.PlParser;
import net.sf.tweety.logics.pl.syntax.Conjunction;
import net.sf.tweety.logics.pl.syntax.PropositionalFormula;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

import java.io.IOException;
import java.util.ArrayList;


class Agent {
  private ArrayList<int[]> explored = new ArrayList<>();
  private static ArrayList<boolean[]> clauses = new ArrayList<>();
  private ArrayList<ArrayList<Node>> nodeMap = new ArrayList<>();
  private boolean gameOver, stuck;
  private char[][] knownMap;
  private char[][] master;
  private int numberOfTornadoes;

  private void satX(boolean helpSPX) {

    int id = 1;
    for (int x = 0; x < knownMap.length; x++) {
      ArrayList<Node> row = new ArrayList<>();
      for (int y = 0; y < knownMap[0].length; y++) {
        row.add(new Node(x, y, knownMap[x][y], Integer.toString(id)));
        id++;
      }
      nodeMap.add(row);
    }
    if (!helpSPX) {
      while (!gameOver) {
        stuck = true;
        try {
          for (int x = 0; x < knownMap.length; x++) {
            for (int y = 0; y < knownMap[0].length; y++) {
              if (knownMap[x][y] == '?') {
                ISolver solver = getCurrentKBU();
                try {
                  solver.addClause(new VecInt(new int[]{Integer.parseInt(getID(x, y).substring(1))}));
                  if (!solver.isSatisfiable()) {
                    nodeMap.get(x).get(y).setValue(probe(x, y));
                  }
                } catch (ContradictionException ce) {
                  nodeMap.get(x).get(y).setValue(probe(x, y));
                }
              }
            }
          }
          if (!gameOver) {
            for (int x = 0; x < knownMap.length; x++) {
              for (int y = 0; y < knownMap[0].length; y++) {
                if (knownMap[x][y] == '?') {
                  ISolver solver = getCurrentKBU();
                  try {
                    solver.addClause(new VecInt(new int[]{Integer.parseInt(getID(x, y).substring(1)) * -1}));
                    if (!solver.isSatisfiable()) {
                      flag(x, y);
                      nodeMap.get(x).get(y).setValue('f');
                    }
                  } catch (ContradictionException ce) {
                    flag(x, y);
                    nodeMap.get(x).get(y).setValue('f');
                  }

                }
              }
            }
          }
        } catch (TimeoutException e) {
          e.printStackTrace();
        }
        if (stuck) {
          singlePointStrategy(true);
        }
      }
    } else {
      stuck = true;
      try {
        for (int x = 0; x < knownMap.length; x++) {
          for (int y = 0; y < knownMap[0].length; y++) {
            if (knownMap[x][y] == '?') {
              ISolver solver = getCurrentKBU();
              try {
                solver.addClause(new VecInt(new int[]{Integer.parseInt(getID(x, y).substring(1))}));
                if (!solver.isSatisfiable()) {
                  nodeMap.get(x).get(y).setValue(probe(x, y));
                }
              } catch (ContradictionException ce) {
                nodeMap.get(x).get(y).setValue(probe(x, y));
              }
            }
          }
        }
        if (!gameOver) {
          for (int x = 0; x < knownMap.length; x++) {
            for (int y = 0; y < knownMap[0].length; y++) {
              if (knownMap[x][y] == '?') {
                ISolver solver = getCurrentKBU();
                try {
                  solver.addClause(new VecInt(new int[]{Integer.parseInt(getID(x, y).substring(1)) * -1}));
                  if (!solver.isSatisfiable()) {
                    flag(x, y);
                    nodeMap.get(x).get(y).setValue('f');
                  }
                } catch (ContradictionException ce) {
                  flag(x, y);
                  nodeMap.get(x).get(y).setValue('f');
                }

              }
            }
          }
        }
      } catch (TimeoutException e) {
        e.printStackTrace();
      }
      if (stuck) {
        int[] randomCoords = getRandomCoords();
        int x = randomCoords[0];
        int y = randomCoords[1];
        probe(x, y);
      }
    }
  }

  private ISolver getCurrentKBU() {
    int MAXVAR = 1000000;
    int NBCLAUSES = 500000;
    ISolver solver = SolverFactory.newDefault();
    solver.newVar(MAXVAR);
    solver.setExpectedNumberOfClauses(NBCLAUSES);
    ArrayList<String> formula = new ArrayList<String>();
    for (int x = 0; x < nodeMap.size(); x++) {
      for (int y = 0; y < nodeMap.get(0).size(); y++) {
        if (knownMap[x][y] != '?' && knownMap[x][y] != 'f' && getUnknownNeighboursNum(x, y) > 0) {
          String currentClause = getKB(x, y);
          if (currentClause !=null) {
            formula.add("("+currentClause+")");
          }
        }
      }
    }
    StringBuilder finalFormula  = new StringBuilder();
    for (int i = 0; i < formula.size(); i++) {
      if (i < formula.size() - 1) {
        finalFormula.append(formula.get(i)).append(" && ");
      } else {
        finalFormula.append(formula.get(i));
      }
    }
    try {
      PlBeliefSet kb = new PlBeliefSet();
      PlParser parser = new PlParser();
      kb.add((PropositionalFormula) parser.parseFormula(finalFormula.toString()));
      Conjunction conj = kb.toCnf();
      for (PropositionalFormula clause : conj) {
        String[] clauseStrings = clause.toString().split("\\|\\|");
        int[] codedClause = new int[clauseStrings.length];
        int index = 0;
        for (String clauseString : clauseStrings) {
          clauseString = clauseString.replace("C", "");
          if (clauseString.charAt(0) == '!') {
            int code = Integer.parseInt(clauseString.substring(1, clauseString.length())) * -1;
            codedClause[index] = code;
          } else {
            int code = Integer.parseInt(clauseString);
            codedClause[index] = code;
          }
          index++;
        }
        solver.addClause(new VecInt(codedClause));
      }
    } catch (IOException | ContradictionException e) {
      e.printStackTrace();
    }
    return solver;
  }

  private String getKB(int x, int y) {
    clauses.clear();
    int n = getUnknownNeighboursNum(x, y);
    int c = Character.getNumericValue(knownMap[x][y]) - getFlaggedNeighboursNum(x, y);
    boolean[] clausesArr = new boolean[n];
    StringBuilder clauseString = null;
    generateAllBinaryClauses(n, clausesArr, 0, c);
    for (int i = 0; i < clauses.size(); i++) {
      if (clauseString == null) {
        clauseString = new StringBuilder("(");
      } else {
        clauseString.append("(");
      }
      int index = 0;
      for (int xMod = -1; xMod < 2; xMod++) {
        for (int yMod = -1; yMod < 2; yMod++) {
          if (!(xMod == 0 && yMod == 0)) {
            if (!(xMod == -1 && yMod == 1) && !(xMod == 1 && yMod == -1)) {
              int neighbourX = x + xMod;
              int neighbourY = y + yMod;
              if (!(neighbourX < 0 || neighbourY < 0 || neighbourX >= knownMap[0].length
                  || neighbourY >= knownMap[0].length)){
                if (knownMap[neighbourX][neighbourY] == '?') {
                  if (clauses.get(i)[index]) {
                    clauseString.append(getID(neighbourX, neighbourY)).append(" && ");
                  } else{
                    clauseString.append("!").append(getID(neighbourX, neighbourY)).append(" && ");
                  }
                  index++;
              } else if (knownMap[neighbourX][neighbourY] == 'f') {
                  clauseString.append(getID(neighbourX, neighbourY)).append(" && ");
                }
              }
            }
          }
        }
      }
      clauseString = new StringBuilder(clauseString.substring(0, clauseString.length() - 4));
      clauseString.append(")");
      if (i != clauses.size() - 1) {
        clauseString.append(" || ");
      }
    }
    if (clauseString != null) {
      return clauseString.toString();
    } else {
      return null;
    }
  }

  private String getID(int x, int y) {
    for (ArrayList<Node> row : nodeMap) {
      for (Node node : row) {
        if (node.getX() == x && node.getY() == y) {
          return node.getID();
        }
      }
    }
    return null;
  }

  private int weightOf(boolean[] arr) {
    int sum = 0;
    for (boolean val : arr) {
      if (val) {
        sum++;
      }
    }
    return sum;
  }

  private void generateAllBinaryClauses(int n, boolean[] arr, int i, int c) {
    if (i == n) {
      if (weightOf(arr) == c) {
        clauses.add(arr.clone());
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

  private int probe(int x, int y) {
    stuck = false;
    char value = master[x][y];
    knownMap[x][y] = value;
    System.out.println("--> probe " + x + " " + y);
    new Board(knownMap).printBoard();
    if (value == 't') {
      gameOver = true;
      System.out.println("Tornado hit! You lose");
      return -1;
    } else {
      explored.add(new int[] { x, y });
      if (explored.size() == (knownMap.length*knownMap.length)-numberOfTornadoes) {
        gameOver = true;
        System.out.println("You win!");
      }
      return value;
    }
  }

  private void flag(int x, int y) {
    stuck = false;
    System.out.println("--> flag " + x + " " + y);
    knownMap[x][y] = 'f';
    new Board(knownMap).printBoard();
  }

  private int[] getRandomCoords() {
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
        probe(x, y);
      }
    } else if (playType == 2) {
      // SPX
      singlePointStrategy(false);
    } else if (playType == 3) {
      satX(false);
      // SATX
    }
  }

  public void singlePointStrategy(boolean helpSAT) {
    if (!helpSAT) {
      while (!gameOver) {
        stuck = true;
        checkAllFreeNodes();
        checkAllMarkedNodes();
        new Board(knownMap).printBoard();
        if (stuck) {
          System.out.println("random");
          int[] randomCoords = getRandomCoords();
          int x = randomCoords[0];
          int y = randomCoords[1];
          probe(x, y);
        }
      }
    } else {
      stuck = true;
      checkAllFreeNodes();
      checkAllMarkedNodes();
      new Board(knownMap).printBoard();
      if (stuck) {
        System.out.println("random");
        int[] randomCoords = getRandomCoords();
        int x = randomCoords[0];
        int y = randomCoords[1];
        probe(x, y);
      }
    }
  }

  public void checkAllFreeNodes() {
    for (int x = 0; x < knownMap.length; x++) {
      for (int y = 0; y < knownMap[0].length; y++) {
        if (knownMap[x][y] != '?' && knownMap[x][y] != 'f') {
          int val = knownMap[x][y] - '0';
          if (val == getFlaggedNeighboursNum(x, y) && getUnknownNeighboursNum(x, y) > 0) {
            probeNeighbours(x, y);
          }
        }
      }
    }
  }

  public void checkAllMarkedNodes() {
    for (int x = 0; x < knownMap.length; x++) {
      for (int y = 0; y < knownMap[0].length; y++) {
        if (knownMap[x][y] != '?' && knownMap[x][y] != 'f') {
          int val = knownMap[x][y] - '0';
          if (val - getFlaggedNeighboursNum(x, y) == getUnknownNeighboursNum(x, y)
              && getUnknownNeighboursNum(x, y) > 0) {
            flagNeighbours(x, y);
          }
        }
      }
    }
  }

  public void probeNeighbours(int x, int y) {
    for (int xMod = -1; xMod < 2; xMod++) {
      for (int yMod = -1; yMod < 2; yMod++) {
        if (!(xMod == 0 && yMod == 0)) {
          if (!(xMod == -1 && yMod == 1) && !(xMod == 1 && yMod == -1)) {
            int neighbourX = x + xMod;
            int neighbourY = y + yMod;
            if (!(neighbourX < 0 || neighbourY < 0 || neighbourX >= knownMap[0].length
                || neighbourY >= knownMap[0].length) && knownMap[neighbourX][neighbourY] == '?') {
              probe(neighbourX, neighbourY);
            }
          }
        }
      }
    }
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
