import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.treeToValue
import nexstra.ddata.*
import java.io.*
import nexstra.services.config.*
import nexstra.services.toJson
import nexstra.generated.*
import org.gradle.internal.impldep.com.google.gson.JsonObject
import java.sql.ResultSet
import java.util.*

inline fun <reified BEAN> ResultSet.asBean() : BEAN = objectMapper.convertValue<BEAN>( asJsonObject() )


fun extractReports( outdir: java.io.File , datasource: File  ){
  val mapper = objectMapper.writerWithDefaultPrettyPrinter()
  outdir.mkdirs()
  val jdbc = source( datasource )
  val reportMap = mutableMapOf<String,Map<String,Any>>()
  jdbc.withConnection {
    val all = query( "SELECT * from reports" ).toList<JsonNode> { asJsonNode() }

  for( n in all ) {
    val report : reports = configure { from ( n ) }
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
    reportMap[metaname] = meta
    File( outdir,"${metaname}").writeText( mapper.writeValueAsString(meta) )
  }

  val connReports = query("SELECT * from connectors WHERE name like '%report%'").toList<connectors>{  asBean() }
  val typeMap = query("SELECT connector_type_id , name FROM connector_types").toList {  getInt(1) to getString(2) }.toMap()
    val clientMap = query("SELECT * from client").toList<client>{ asBean() }.map{ it.client_id to it }.toMap()
    val partnerMap = query("SELECT partner_id, code from partners").toList { getInt(1) to getString(2) }.toMap()
println( mapper.writeValueAsString(clientMap ) )
    println( mapper.writeValueAsString(partnerMap ) )


    for( c : connectors in connReports) {
     val props = propertyMapper.readTree( c.properties.byteInputStream())
     val meta =  objectMapper.valueToTree(c) as ObjectNode
     println("client: ${c.client_id}, ${clientMap[c.client_id.toString()]} ")
     val client =clientMap[ c.client_id.toString() ]

      meta["properties"] = props
     meta.remove("log_data")
     meta.remove("connector_id")
     meta["connector_type"] = nodeFactory.textNode( typeMap[ c.connector_type_id.toString() ] )
     meta.remove("connector_type_id")
     meta["client" ] = nodeFactory.textNode(client ?.let {  partnerMap[ it.partner_id.toString()] + " / " + it.name  } ?: c.client_id.toString())
     meta.remove("state")
     meta.remove("running")
      val metaname = "${c.name}.rpt"
      meta.remove("client_id")


      File( outdir,"${metaname}").writeText( mapper.writeValueAsString(meta) )


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
