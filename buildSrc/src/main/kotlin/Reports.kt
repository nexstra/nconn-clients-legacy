import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.convertValue
import nexstra.ddata.*
import java.io.*
import nexstra.services.config.*
import nexstra.generated.*
import java.sql.ResultSet
import nexstra.ddata.DRef


val mapper = objectMapper.configure( SerializationFeature.INDENT_OUTPUT ,true )

inline fun <reified BEAN> ResultSet.asBean() : BEAN = mapper.convertValue<BEAN>( asJsonObject() )

fun String.toFileName(ext: String) = replace("[^a-zA-Z_0-9-]".toRegex(), "_") + ".${ext}"
fun tenant( partnerCode: String? , clientName: String? ) = if( partnerCode != null && clientName != null )
   partnerCode + "-" + clientName.replace("[^a-zA-Z_0-9-]".toRegex(), "_")
   else "system"

fun extractReports( outdir: java.io.File , _datasource: String  ){
  val queryDir = File(outdir,"queries")
  queryDir.mkdirs()
  val datasource = DRef.fromRef<DataSource>(_datasource.removePrefix("@"))
  val jdbc = source( datasource )
  val queryMap = mutableMapOf<String,Map<String,Any>>()
  val sqlMap = mutableMapOf<String,Pair<String,String>>()
  val all = jdbc.withConnection {
    query("SELECT * from reports WHERE report_type = 'SQL'").toList<JsonNode> {asJsonNode()}
  }

  for( n in all ) {
    val report : reports = configure { from ( n ) }
    println(report.name)


    println( report.sql_query )
    val parsed = NamedParameters.parse(report.sql_query)
    println(parsed.pretty())

//    File( outdir,queryFile).writeText( parsed.reparse() )
    /*
      * Attempt to parse query on its data source
     */


    var allp = jdbc.withConnection {
      query("SELECT * from reports_input WHERE report_id = ?",
          report.report_id).toList {asBean<reports_input>()}.sortedBy {it.col}.toMutableList()
    }

    if( parsed.orderedParameters.size != allp.size ) {
      println(
          "Mismatch between declared parameters and detected parameters in ${report.name}\nDetected: ${parsed.orderedParameters} ${allp}\n${report.sql_query} ")
      while (allp.size < parsed.orderedParameters.size)
        allp.add(reports_input().apply {
          col = allp.size + 1L
          name = parsed.orderedParameters[allp.size]
          description = "autogenerated"
          type = reports_input.typeEnum.STRING
        })
     while( allp.size > parsed.orderedParameters.size ) {
      println("Dropping extraneous import column ${allp[allp.size-1]}")
      allp.removeAt(allp.size-1)
     }

      //     println( StopActionException("Mismatch between declared parameters and detected parameters in ${report.name}\nDetected: ${parsed.orderedParameters} ${allp}\n${report.sql_query} ").pretty())
    }
    allp.forEachIndexed  { i , a ->

      if( a.col.toInt() != i+1 ) {
        println(
            "Mismatch between declared parameters and detected parameters in ${report.name}\nInput column mismatch: ${a.col} expected ${i + 1}")
      }
      parsed.orderedParameters [i] = a.name
    }
    //File( queryDir,queryFile).writeText( parsed.reparse() )
    val ext = report.report_type.name.toLowerCase()
    val queryFile =  report.name.toFileName(ext)
    sqlMap[report.name] = queryFile to parsed.reparse()

    val meta = mapOf(
        "name" to report.name,
        "type" to "query",
        "description" to report.description,
        "query_type" to report.report_type.name.toLowerCase() ,
        "dbname" to report.dbname ,
        "query" to "@queries/" + queryFile ,
        "datasource" to  datasource ,
        "parameters" to allp.map { mapper.convertValue<Map<String,String>>( it ) - listOf("report_input_id","report_id","fillin" ) }

        )
    queryMap[report.name] = meta
    // File( queryDir,"${metaname}").writeText( mapper.writeValueAsString(meta) )


  }

  val connReports = jdbc.withConnection { query("SELECT * from connectors WHERE name like '%report%'").toList<connectors>{  asBean() } }

  val typeMap = jdbc.withConnection {query("SELECT connector_type_id , name FROM connector_types").toList {  getLong(1) to getString(2) }.toMap<Long,String>() }

  val clientMap = jdbc.withConnection {query("SELECT * from client order by client_id").toList<client>{ asBean() }.map{ it.client_id to it }.toMap()}
    val partnerMap = jdbc.withConnection {query("SELECT partner_id, code from partners order by partner_id").toList { getLong(1) to getString(2) }.toMap<Long,String>()}
//println( mapper.writeValueAsString(clientMap ) )
 // println( mapper.writeValueAsString(partnerMap ) )
   /*   jdbc.withConnection {
      val rs = this.createStatement().executeQuery("SELECT count(*) from hpe.client")
      while( rs.next() )
        println(" client ${rs.getInt(1)}")


    }
*/
    for( c : connectors in connReports) {
      val props = propertyMapper.readTree(c.properties.byteInputStream())
      val meta = mapper.valueToTree(c) as ObjectNode
      //    println("client: ${c.client_id}, ${clientMap[c.client_id]?.name} ")
      val client = clientMap[c.client_id.toLong()]
      val partnerName = client?.partner_id?.let {partnerMap[it.toLong()]} ?: null
      val clientName = client?.name
/*
      meta["properties"] = props
      meta.remove("log_data")
      meta.remove("connector_id")
      meta["connector_type"] = nodeFactory.textNode(typeMap[c.connector_type_id])
      meta.remove("connector_type_id")
      meta["client"] = nodeFactory.textNode(partnerName ?: ""+"/"+clientName ?: "")

      meta.remove("state")
      meta.remove("running")
      val dir = File(outdir, partnerName?.let {p-> clientName?.let {"$p/$it"}} ?: "system")
      dir.mkdirs()
      meta.remove("client_id")
      File(dir, c.name.toFileName("rpt")).writeText(mapper.writeValueAsString(meta))

      */
      val pdir = File(outdir, tenant(partnerName , clientName) )

      val report : String? by props
      if (report != null) {
         val dir = File(pdir,"queries")
         dir.mkdirs()

        queryMap[report]?.let {rpt->
          File(dir, report.toFileName("query")).writeText(mapper.writeValueAsString(rpt))
          queryMap.remove(report)
        }
        sqlMap[report]?.let {(fname, sql)->
          File(dir, fname).writeText(sql)
          sqlMap.remove(report)
        }

      }

    }
      val sysdir = File(outdir, "system")
    val dir = File(sysdir,"queries")
    dir.mkdirs()
    queryMap.entries.forEach {(key, rpt)->
        File(dir, key.toFileName("query")).writeText(mapper.writeValueAsString(rpt))
      }
      sqlMap.entries.forEach {(key, q)->
        File(dir, q.first).writeText(q.second)
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
