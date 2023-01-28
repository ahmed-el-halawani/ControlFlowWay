import java.util.*
import kotlin.collections.HashMap

class Graph<T>() {
    // We use Hashmap to store the edges in the graph
    private val map: MutableMap<T, MutableList<T>> = HashMap()

    // This function adds a new vertex to the graph
    fun addVertex(s: T) {
        map[s] = LinkedList()
    }

    // This function adds the edge
    // between source to destination
    fun addEdge(
        source: T,
        destination: T,
        bidirectional: Boolean
    ) {
        if (!map.containsKey(source)) addVertex(source)
        if (!map.containsKey(destination)) addVertex(destination)
        map[source]!!.add(destination)
        if (bidirectional == true) {
            map[destination]!!.add(source)
        }
    }

    val vertexCount: Unit
        // This function gives the count of vertices
        get() {
            println(
                "The graph has "
                        + map.keys.size
                        + " vertex"
            )
        }

    // This function gives the count of edges
    fun getEdgesCount(bidirection: Boolean) {
        var count = 0
        for (v: T in map.keys) {
            count += map[v]!!.size
        }
        if (bidirection == true) {
            count = count / 2
        }
        println(
            ("The graph has "
                    + count
                    + " edges.")
        )
    }

    // This function gives whether
    // a vertex is present or not.
    fun hasVertex(s: T) {
        if (map.containsKey(s)) {
            println(
                ("The graph contains "
                        + s + " as a vertex.")
            )
        } else {
            println(
                ("The graph does not contain "
                        + s + " as a vertex.")
            )
        }
    }

    fun getVertex(s: T) {
        val builder = StringBuilder();
        builder.append(s.toString() + ": ")
        for (w: T in map[s]!!) {
            builder.append(w.toString() + " ")
        }
        println(builder.toString())
    }

    // This function gives whether an edge is present or not.
    fun hasEdge(s: T, d: T) {
        if (map[s]!!.contains(d)) {
            println(
                ("The graph has an edge between "
                        + s + " and " + d + ".")
            )
        } else {
            println(
                ("The graph has no edge between "
                        + s + " and " + d + ".")
            )
        }
    }

    // Prints the adjancency list of each vertex.
    override fun toString(): String {
        val builder = java.lang.StringBuilder()
        for (v: T in map.keys) {
            builder.append(v.toString() + ": ")
            for (w: T in map[v]!!) {
                builder.append(w.toString() + " ")
            }
            builder.append("\n")
        }
        return (builder.toString())
    }
}