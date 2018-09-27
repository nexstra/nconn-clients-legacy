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

data class Report (
  val report_id : Long,
  val name : String ,
  val description: String ?,
  val report_type: String ,
  val dbname : String ?,
  val sql_query : String
)

data class ReportsInput(
  val col: Int ,
  val name: String ,
  val description: String?,
  val type: String ,
  val def_value: String?
)


fun extractReports( outdir: java.io.File , datasource: File  ){
  outdir.mkdirs()
  val jdbc = source( datasource )

  jdbc.withConnection {
    val all = query( "SELECT * from reports" ).toList<JsonNode> { asJsonNode() }

  for( n in all ) {
    val report : Report = configure { from ( n ) }
    println(report.name)
    val fname = report.name.replace("[^a-zA-Z_0-9-]".toRegex(), "_")
    val metaname = "${fname}.report"
    val ext = report.report_type.toLowerCase()

    val queryFile = "${fname}.${ext}"
    File( outdir,queryFile).writeText( report.sql_query )
    val allp = query("SELECT * from reports_input WHERE report_id = ?",report.report_id).toList { asJsonNode() }
    val meta = mapOf(
        "name" to report.name,
        "type" to "report",
        "description" to report.description,
        "report_type" to report.report_type.toLowerCase() ,
        "dbname" to report.dbname ,
        "query" to queryFile ,
        "datasource" to datasource.nameWithoutExtension,
        "parameters" to
         allp.map {
           mapOf(
               "col" to it["col"],
               "name" to it["name"],
               "description" to it["description"],
               "type" to it["type"],
               "default" to it["def_value"]
           )
         }

        )

    File( outdir,"${metaname}").writeText( meta.toJson() )
  }
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
