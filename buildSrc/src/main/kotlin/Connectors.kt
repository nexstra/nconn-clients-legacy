import Connectors.importType
import Connectors.obsoleteType
import Connectors.reportType
import Connectors.unknownType
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsFactory
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper
import nexstra.client.workflow.MailParameters
import nexstra.client.workflow.ReportParameters
import nexstra.client.workflow.SyncSFTPParameters
import nexstra.ddata.*
import nexstra.generated.connectors
import nexstra.services.AnyMapR
import nexstra.services.annotations.Config
import java.io.*
import nexstra.services.config.*

class Connector : connectors() {
  val client : String = ""
  val partner: String = ""
  val type : String = ""
  val classname : String =""
}
@Config
class FTP  {
  val user : String =""
  val password: String=""
  val root: String=""
  val passive: Boolean=true
  val secure: String="SFTP"
  val host: String=""
}
@Config
class ReportConnector {
  val ftp : FTP? = null
  val input : Map<String,String> = mutableMapOf()
  val msgtext: String= ""
  val output_file: String= ""
  val report: String= ""
  val to: String= ""
  val cc: String? = null
}
data class RunConfig(
  val type : String,
  val name : String ,
  val target  : String ,
  val input : AnyMapR  = mapOf()
  )

object Connectors {

  fun importType(connector : Connector) : RunConfig? {
    println(
        "!!!!TODO!!!!! Gonna process type: ${connector.name} ${connector.type}")
    return null
  }

  fun obsoleteType(connector : Connector) : RunConfig? {
    println("Obsolete type: ${connector.name} ${connector.type}")
    return null
  }

  fun unknownType(connector : Connector) : RunConfig? {
    println("Unknown type: ${connector.name} ${connector.type}")
    return null
  }

  fun reportType(connector : Connector, _outdir : File, stepType : String, datasource : DRef<DataSource>)  {
    println("REPORT type: ${connector.name} ${connector.type}")

    val factory = JavaPropsFactory()
    val parser = factory.createParser(connector.properties.byteInputStream())
    val props = JavaPropsMapper(factory).readTree(parser)

    val fname =connector.name.toFileName("steps")

    val rc = configure<ReportConnector> {from(props as JsonNode)}
    val tdir = File(_outdir,tenant(connector.partner,connector.client))
    val stepDir = File(tdir,"steps")
    val reportDir = File(tdir,"reports")
    val runDir = File(tdir,"run")

    stepDir.mkdirs()
    runDir.mkdirs()
    reportDir.mkdirs()
    val steps = if (rc.ftp != null) {
      nexstra.client.workflow.RunReportAndSyncSFTPParameters().apply {
        report = reportParameters(rc, datasource)
        sftp = syncParameters(rc)
      }.also {
        File(reportDir,connector.name.toFileName("report")).writeText( mapper.writeValueAsString(it.report))
      }
    }
    else {
      nexstra.client.workflow.RunReportAndEmailParameters().apply {
        report = reportParameters(rc, datasource)
        mail = mailParameters(rc)
      }.also {
        File(reportDir, connector.name.toFileName("report")).writeText(mapper.writeValueAsString(it.report))
      }
    }
    File(stepDir,fname).writeText(mapper.writeValueAsString(steps))

    val run =  RunConfig("stepfunctions",connector.name,"@steps/" + fname)
    File(runDir,connector.name.toFileName("run")).writeText( mapper.writeValueAsString(run))

  }

  fun syncParameters(rc : ReportConnector) = SyncSFTPParameters().apply {
    auth = "@FIXME:host:" + rc.ftp?.host ?:""
    //  this.dest = rc.ftp?.root
    root = (rc.ftp?.root) ?: "/"
    dest = rc.output_file
  }

  fun reportParameters(rc : ReportConnector, datasource : DRef<DataSource>) = ReportParameters(
      format = "csv",
      params = rc.input,
      query = DRef( "queries/" + rc.report.toFileName("query")),
      dataSource = datasource
  )

  fun mailParameters(rc : ReportConnector) = MailParameters().apply {
    to = rc.to
    subject = "${rc.msgtext}"
    cc = rc.cc
  }
}

  fun convertConnectors( outdir: java.io.File , _datasource: String ) {
    outdir.mkdirs()
    val datasource = DRef.fromRef<DataSource>(_datasource.removePrefix("@"))
    val jdbc = source( datasource )

    val all = jdbc.withConnection {
      query("""
      SELECT connectors.*, client.name as 'client', partners.code as 'partner', connector_types.name as 'type' , connector_types.classname
      FROM connectors JOIN client USING(client_id)
           JOIN partners USING(partner_id)
           JOIN connector_types USING(connector_type_id)
      """).toList<JsonNode> {asJsonNode()}
    }
    for (n in all) {
      val connector : Connector = configure {from(n)}
      println(connector.name)

      when(connector.classname) {
        "nexstra.cds.client.ss.Load_SSConnector",
        "nexstra.cds.client.ss.LoadServiceSourceSampleConnector",
        "nexstra.cds.client.strategy.LoadSymantecDistConnector",
        "odd.connector.ReportConnector"                        -> unknownType(connector)

        "nexstra.cds.client.strategy.LoadVeritasDistConnector" -> unknownType(connector)

        "odd.connector.NullConnector",
        "odd.connector.TestConnector"                          -> obsoleteType(connector)

        "nexstra.cds.connector.csv.AWSFTPCSVImportConnector",
        "nexstra.cds.connector.csv.CSVImportConnector",
        "nexstra.cds.connector.csv.FTPCSVImportConnector"      -> importType(connector)

        "nexstra.cds.connector.AWSReportFTPConnector",
        "nexstra.cds.connector.ReportFTPConnector"             -> reportType(connector, outdir, "ReportAndSFTP",datasource)
        "nexstra.cds.connector.ReportMailConnector"            -> reportType(connector, outdir, "ReportAndEmail",datasource)
        "nexstra.cds.connector.S3Connector",
        "nexstra.cds.connector.SQLConnector"                   -> reportType(connector, outdir, "Report",datasource)
        else -> unknownType(connector)
      }

      if( connector.run_type == "EVERY" ) {

      }


    }
  }

