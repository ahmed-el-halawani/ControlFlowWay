public class SSS {

    public static void main(String[] args) {
        GraphControlFlowJ<String> g = new GraphControlFlowJ<>("");
        g.flowBuilder().then(it -> {
            System.out.println("");
        }).build();
    }
}
