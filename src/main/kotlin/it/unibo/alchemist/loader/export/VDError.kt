import it.unibo.alchemist.loader.export.Extractor
import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.MapEnvironment
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.Time
import org.apache.commons.math3.stat.descriptive.StorelessUnivariateStatistic
import org.apache.commons.math3.stat.descriptive.moment.Mean
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation
import org.apache.commons.math3.stat.descriptive.rank.Max
import org.apache.commons.math3.stat.descriptive.rank.Min
import java.lang.IllegalStateException
import kotlin.math.abs

class VDError : Extractor {
    override fun <T : Any> extractData(
        environment: Environment<T, *>,
        reaction: Reaction<T>?,
        time: Time?,
        step: Long
    ): DoubleArray {
        require(environment is MapEnvironment<T, *, *>) {
            "Wroooong"
        }
        val allReal = environment.getNodes()
            .filter { it.contains(isReal) }
            .toSet()
        val center: Node<T> = environment.nodes.find { it.contains(center) }
            ?: throw IllegalStateException("No center?")
        val connected = environment.connectedNodes(center).filter { it.contains(isReal) }
        val disconnected = allReal - connected
        val errors = environment.nodes.asSequence()
            .filter { it.contains(isReal) }
            .map {
                val value = it.getConcentration(measured)
                val actual = environment.computeRoute(center, it).length()
                when (value) {
                    null -> actual
                    is Number -> abs((value.toDouble().takeIf { it.isFinite() } ?: 0.0) - actual)
                    else -> throw IllegalStateException("$value (${value::class.simpleName }) is not a number")
                }
            }
            .toList()
            .toDoubleArray()
        return doubleArrayOf(
            allReal.size.toDouble(),
            disconnected.size.toDouble(),
            mean.evaluate(errors),
            stdev.evaluate(errors),
            max.evaluate(errors),
            min.evaluate(errors),
        )
    }

    override fun getNames() = listOf("real", "disconnected", "error[mean]", "error[stdev]", "error[max]", "error[min]")

    companion object {
        private val error = SimpleMolecule("error")
        private val isReal = SimpleMolecule("target")
        private val center = SimpleMolecule("center")
        private val measured = SimpleMolecule("measured")
        private val mean: StorelessUnivariateStatistic = Mean()
        private val stdev: StorelessUnivariateStatistic = StandardDeviation()
        private val min: StorelessUnivariateStatistic = Min()
        private val max: StorelessUnivariateStatistic = Max()
        private fun <T> Environment<T, *>.connectedNodes(center: Node<T>): Set<Node<T>> {
            fun locallyConnected(node: Node<T>): Set<Node<T>> = getNeighborhood(node).neighbors.toSet()
            val toVisit = locallyConnected(center).toMutableSet()
            val visited = mutableSetOf(center)
            val result = mutableSetOf(center)
            while (toVisit.isNotEmpty()) {
                val subject = toVisit.first()
                toVisit.remove(subject)
                visited.add(subject)
                toVisit.addAll(locallyConnected(subject) - visited)
                result.add(subject)
            }
            return result
        }
    }

}