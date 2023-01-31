import java.lang.Exception
import java.util.*

fun main(args: Array<String>) {
//    println("Hello World!")
//    ccx()



    val controller = GraphControlFlow(1)
        .flowBuilder()
        .setOnError("") {

        }
        .setOnTerminate("") {

        }
        .then("then 1") {
            println("1")
        }
        .then("then 1") {
            println("1")
        }
        .thenAndNext("then 2") {
            println("2")
        }.whenCondition {
            println("default when")
        }.case("case 1", { false }) {
            it.thenAndNext("1") {
                println("case 1 then 1")
            }
        }.case("case 2", { true }) {
            it.thenAndNext("1") {
                println("case2 then1")
            }
        }.build()


}
//
//object Main {
//    @JvmStatic
//    fun main(args: Array<String>) {
//
//        // Object of graph is created.
//        val g = Graph<Int>()
//
//        // edges are added.
//        // Since the graph is bidirectional,
//        // so boolean bidirectional is passed as true.
//
//        var latestNode = 0;
//
//
//        g.addEdge(latestNode, ++latestNode, false)
//        g.addEdge(latestNode, ++latestNode, false)
//        g.addEdge(latestNode, ++latestNode, false)
//
//
//        g.addEdge(latestNode, latestNode + 1, false)
//        g.addEdge(latestNode, latestNode + 2, false)
//        g.addEdge(latestNode, latestNode + 3, false)
//        g.addEdge(latestNode, latestNode + 4, false)
//
//
//
//
//
//        g.addEdge(latestNode + 2, latestNode + 3, false)
//
//
////
////
////        g.addEdge(0, 1, true)
////        g.addEdge(0, 4, true)
////        g.addEdge(1, 2, true)
////        g.addEdge(1, 3, true)
////        g.addEdge(1, 4, true)
////        g.addEdge(2, 3, true)
////        g.addEdge(3, 4, true)
//
//        // Printing the graph
////        println(
////            """
////                Graph:
////                $g
////                """.trimIndent()
////        )
//
//
//        g.getVertex(3)
//
//        // Gives the no of vertices in the graph.
//        g.vertexCount
//
//        // Gives the no of edges in the graph.
//        g.getEdgesCount(true)
//
//        // Tells whether the edge is present or not.
//        g.hasEdge(3, 4)
//
//        // Tells whether vertex is present or not
//        g.hasVertex(5)
//    }
//}


class ccx() {

    private var controller: GraphControlFlow<StringBuilder>.Controller;

    init {
        controller = GraphControlFlow(StringBuilder("ahmed"), ::println)
            .flowBuilder()
            .thenAndNext("step1befWhen") {
                it?.append("1")
            }
            .thenAndNext("step2befWhen") {
                it?.append("2")
            }
            .thenAndNext("step3befWhen") {
                it?.append("3")
            }
            .whenCondition("whenCondition1") { it.then("default when then 1") {} }
            .case("case 1", { false }) {
                it.then("c1 then 1") {
                }.thenAndNext("c1 then 2") {
                }.thenAndNext("c1 then 3") {
                }
            }
            .case("case 2", { true }) {
                it.thenAndNext("c2 then 1") {
                }.thenAndNext("c2 then 2") {
                }.thenAndNext("c2 then 3") {
                }.whenCondition("case 2 whenCondition") { it2 -> it2.thenAndNext("default when then 1") {} }
                    .case("c1", { false }) {
                        it.thenAndNext("c1 then1") {}
                    }
            }
            .case("case 3", { false }) {
                it.thenAndNext("then 1") {
                }.thenAndNext("then 2") {
                }.thenAndNext("then 3") {
                }
            }
            .case("4", { false }) {
                it.thenAndNext("then 1") {
                }.thenAndNext("then 2") {
                }.thenAndNext("then 3") {
                }
            }
            .build()


        controller.next()
//        controller.next()
//        controller.next()
    }

}

