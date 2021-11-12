package it.unibo.alchemist

import it.unibo.alchemist.model.implementations.actions.MoveOnMap
import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule
import it.unibo.alchemist.model.implementations.times.DoubleTime
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.MapEnvironment
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Position
import kotlin.math.abs
import kotlin.math.min

object Clone {

    val virtual = SimpleMolecule("virtual")

    @JvmStatic
    fun genVD(environment: MapEnvironment<Boolean, *, *>, thisNode: Node<Boolean>, time: Double) {
        val position = environment.getPosition(thisNode)
        val neighs = environment.getNeighborhood(thisNode).filter { it.contains(virtual) }
        val neighPositions by lazy { neighs.map { environment.getPosition(it) } }
        val distances by lazy { neighs.map { n -> environment.getPosition(n).let { it to it.distanceTo(position) } } }
        val shouldGenerate = neighs.isEmpty() ||
            distances.none { it.second < 40 }
//                ||
//            distances.none { (p, d) ->
//                val route = environment.computeRoute(position, p).length()
//                abs(route - d) / min(route, d) < 0.1
//            }
        if (shouldGenerate) {
            environment.addNode(thisNode.cloneNode(DoubleTime(time)).also { node ->
                node.setConcentration(SimpleMolecule("virtual"), true)
                node.removeConcentration(SimpleMolecule("target"))
                node.removeReaction(node.reactions.find { r -> r.actions.any { it is MoveOnMap<*, *, *> } })
            }, position)
        }
    }

}