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
    fun genVD(environment: MapEnvironment<Boolean, *, *>, thisNode: Node<Boolean>, time: Double, radius: Any) {
        val position = environment.getPosition(thisNode)
        val neighs = environment.getNeighborhood(thisNode).filter { it.contains(virtual) }
        val distances by lazy { neighs.map { n -> environment.getPosition(n).let { it to it.distanceTo(position) } } }
        val actualRadius = when(radius) {
            is Number -> radius.toDouble()
            is String -> radius.toDouble()
            else -> throw IllegalArgumentException("Illegal radius '$radius' (${radius::class.simpleName})")
        }
        val shouldGenerate = actualRadius.isFinite() && (neighs.isEmpty() || distances.none { it.second < actualRadius })
        if (shouldGenerate) {
            environment.addNode(thisNode.cloneNode(DoubleTime(time)).also { node ->
                node.setConcentration(SimpleMolecule("virtual"), true)
                node.removeConcentration(SimpleMolecule("target"))
                node.removeReaction(node.reactions.find { r -> r.actions.any { it is MoveOnMap<*, *, *> } })
            }, position)
        }
    }

}