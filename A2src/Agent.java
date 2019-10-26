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
            unexplored = (knownMap[x][y]=='x');
        }
        return new int[]{x,y};
    }

    void play(int playType){
        if (playType ==1){
            while (!gameOver){
                int[] randomCoords = getRandomCoords();
                int x = randomCoords[0];
                int y = randomCoords[1];
                int value = probe(x, y);
                if (value < 0 || explored.size() == knownMap.length*knownMap[0].length - numberOfTornadoes) {
                    gameOver = true;
                } 
            }
            //RPX
        } else if (playType==2){
            //SPX
        } else if (playType==3){
            //SATX
        }
    }

    public Agent(char[][] masterMap, int t) {
        master = masterMap;
        numberOfTornadoes = t;
        knownMap = new char[master.length][master[0].length];
        for(int x = 0; x < knownMap.length; x++){
            for(int y = 0; y < knownMap[0].length; y++){
                knownMap[x][y] = 'x';
            }
        }
        knownMap[0][0] = master[0][0];
        knownMap[knownMap.length/2][knownMap[0].length/2] = master[master.length/2][master[0].length/2];
    }
}