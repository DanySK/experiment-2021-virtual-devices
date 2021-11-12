package it.unibo.alchemist.loader.deployments

import it.unibo.alchemist.model.interfaces.GeoPosition
import it.unibo.alchemist.model.interfaces.MapEnvironment
import it.unibo.alchemist.nextDouble
import org.apache.commons.math3.random.RandomGenerator
import org.danilopianini.util.FlexibleQuadTree
import java.util.stream.Stream
import kotlin.math.abs

class OnKeyMapPoints  @JvmOverloads constructor(
    val randomGenerator: RandomGenerator,
    val environment: MapEnvironment<*, *, *>,
    val nodeCount: Int,
    val range: Double,
    val tolerance: Double,
    val minLat: Double,
    val maxLat: Double,
    val minLon: Double,
    val maxLon: Double,
    val maxAttempts: Int = 10
) : Deployment<GeoPosition> {

    private val positions: List<GeoPosition>

    init {
        require(maxAttempts >= 1) {
            "Attempts must be more than one, $maxAttempts is not a valid value"
        }
        val points = FlexibleQuadTree<GeoPosition>()
        var size = 0
        while (size < nodeCount) {
            var attempt = 0
            val currentSize = size
            while (currentSize == size) {
                attempt++
                val candidate = environment.makePosition(
                        randomGenerator.nextDouble(minLat, maxLat),
                        randomGenerator.nextDouble(minLon, maxLon),
                    ).let { environment.getRoutingService().allowedPointClosestTo(it) ?: it }
                if (attempt == maxAttempts) {
                    points.insert(candidate, *candidate.coordinates)
//                    println("attempt $attempt for node $currentSize, candidate $candidate")
                    size++
                } else {

                    val distances = points.query(*candidate.boundingBox(range).map { it.coordinates }.toTypedArray())
                        .map { it to candidate.distanceTo(it) }
                        .asSequence()
                    if (
                        distances.all { it.second > range } ||
                        distances.all { (position, los) ->
                            val route = environment.computeRoute(candidate, position).length()
//                            println("$candidate to $position los $los route $route diff ${abs(route - los)} threshold ${los * tolerance}")
                            abs(route - los) > los * tolerance
                        }
                    ) {
                        points.insert(candidate, *candidate.coordinates)
//                        println("attempt $attempt for node $currentSize, candidate $candidate")
                        size++
                    }
                }
            }
        }
        positions = points.query(-Double.MAX_VALUE, -Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE)
    }

    override fun stream(): Stream<GeoPosition> = positions.stream()
}
