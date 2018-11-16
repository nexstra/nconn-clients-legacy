import nexstra.services.config.DimensionName
import nexstra.services.config.DimensionType
import nexstra.services.config.*

import nexstra.services.resource.withExtension
import java.io.File

val dimensions = StandardDimensionName.values()
fun extractDimensions( dir: File) {

   val alld =    dimensions.map {
        it.localName to
     mapOf(
           "dimension" to it.namespace + ":" + it.localName ,
           "type" to it.dimensionType.toString(),
           "open" to it.dimensionType.open ,
           "ordinal" to it.ordinal,
           "values"  to it.values.map { it.value.toString()  }.toList()
     )}.toMap()

  File( dir, "dimensions.json" ).writeText( mapper.writeValueAsString(alld) )

}

