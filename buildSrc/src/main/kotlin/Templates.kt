import com.fasterxml.jackson.databind.JsonNode
import nexstra.ddata.*
import java.io.*
import java.sql.ResultSet
import java.util.*
import kotlin.coroutines.experimental.buildIterator
import kotlin.reflect.KClass
import nexstra.services.config.*
import nexstra.services.toJson
import kotlin.reflect.KProperty
import nexstra.ddata.DataSource


class Template : nexstra.generated.template() {
  val client : String =""
}

fun ResultSet.asJsonObject() : JSONObject = metaData.let {meta ->
  @Suppress("UNCHECKED_CAST")
  (1..meta.columnCount).map {
    meta.getColumnLabel(it) to asJson<JSONType>(it)
  }.toMap() }
fun ResultSet.asJsonNode() : JsonNode = objectMapper.valueToTree( asJsonObject() )

// public inline operator fun <T> Lazy<T>.getValue(thisRef: Any?, property: KProperty<*>): T = value
operator fun <T> JsonNode.getValue(thisRef: Nothing?, property: KProperty<*>): T =
   objectMapper.convertValue( get(property.name), property.klass!!.java ) as T

fun source( datasource: DRef<JDBCSource> ) =  datasource.deref()

fun extractTemplates( outdir: java.io.File , _datasource: String ){
  outdir.mkdirs()
  val datasource = DRef.fromRef<JDBCSource>(_datasource)
  val jdbc = source( datasource )

  val all = jdbc.withConnection {
     query( "SELECT template.*, client.name as 'client' from template JOIN client USING(client_id)" ).toList<JsonNode> { asJsonNode() }
  }
  for( n in all ) {
     val template : Template = configure { from ( n ) }
      println(template.name)
      val fname = template.name.replace("[^a-zA-Z_0-9-]".toRegex(), "_")
      val metaname = "${fname}.template"
    val ext = template.template_type.toLowerCase()

    File( outdir,"${fname}.${ext}").writeText( template.body )
    val meta = mapOf(
       "name" to template.name,
       "type" to "template",
       "description" to template.description,
       "template_type" to template.template_type ,
       "client" to template.client )

    File( outdir,"${metaname}").writeText( meta.toJson() )
  }
}


/*
   val params = jdbc.withConnection {
       query("SELECT * from parameters WHERE template_id = ?", template.template_id).toList<JsonNode> { asJsonNode() }
     }

 */

/*
  db.run {
       fetchItems<Template>(listOf(),0).forEach {
             val  template = it as Template
            println("""${template.name} = ${template.description}""")
            val name = template.name.replace("[^a-zA-Z_0-9-]".toRegex(), "_")
            val ext = template.template_type.toLowerCase()



        File(outdir, "${name}.${ext}").writeText(template.body)

      }
    }
    */
