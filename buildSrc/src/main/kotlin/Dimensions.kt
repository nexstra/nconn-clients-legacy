import nexstra.services.config.DimensionName
import nexstra.services.resource.withExtension
import java.io.File

val dimensions = DimensionName.values()
fun extractDimensions( dir: File) {

   val alld =    dimensions.map {
        it.dim to
     mapOf(
           "dimension" to it.dim ,
           "type" to it.dimensionType.name,
           "open" to it.dimensionType.open ,
           "ordinal" to it.ordinal,
           "values"  to it.values.map { it.value.toString()  }.toList()
     )}.toMap()

  File( dir, "dimensions.json" ).writeText( mapper.writeValueAsString(alld) )

}

