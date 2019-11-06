public class A2main {
    static char[][] map;
    static Agent agent;
    public static void main(String[] args) {
        if (args.length==2){
            String agentType = args[0];
            String worldChoice = args[1];
            try{
                map = World.valueOf(worldChoice).map;
            } catch(IllegalArgumentException iae) {
                System.out.println("Not a valid map");
                return;            }
            int tornadoNum=0;
            switch(map.length) {
                case 3:
                    tornadoNum = 3;
                    break;
                case 5:
                    tornadoNum = 5;
                    break;
                case 7:
                    tornadoNum = 10;
                    break;
                case 11:
                    tornadoNum = 28;
                    break;
            }
            agent = new Agent(map, tornadoNum);
            switch(agentType){
                case "RPX":
                    agent.play(1);
                    break;
                case "SPX":
                    agent.play(2);
                    break;
                case "SATX":
                    agent.play(3);
                    break;
                default:
                    System.out.println("Not a valid agent type");
            }

        } else{
            System.out.println("Wrong Parameters - use java A2main <RPX|SPX|SATX> <ID>");
        }
    }
}
