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
  val target  : String ,
  val input : AnyMapR
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

  fun reportType(connector : Connector, _outdir : File, stepType : String, datasource : DRef<DataSource>) : RunConfig? {
    println("REPORT type: ${connector.name} ${connector.type}")
    val outdir = File(_outdir, stepType)
    outdir.mkdirs()
    val factory = JavaPropsFactory()
    val parser = factory.createParser(connector.properties.byteInputStream())
    val props = JavaPropsMapper(factory).readTree(parser)

    val fname = connector.name.replace("[^a-zA-Z_0-9-]".toRegex(), "_")
    val ext = ".steps"

    val rc = configure<ReportConnector> {from(props as JsonNode)}

    if (rc.ftp != null) {
      val run = nexstra.client.workflow.RunReportAndSyncSFTPParameters().apply {
        report = reportParameters(rc, datasource)
        sftp = syncParameters(rc)
      }
      File(outdir, "${fname}.${ext}").writeText(objectMapper.writeValueAsString(run))
      return null
    }
    else {
      val run = nexstra.client.workflow.RunReportAndEmailParameters().apply {
        report = reportParameters(rc, datasource)
        mail = mailParameters(rc)
      }
      File(outdir, "${fname}.${ext}").writeText(objectMapper.writeValueAsString(run))
      return null
    }
  }

  fun syncParameters(rc : ReportConnector) = SyncSFTPParameters().apply {
    auth = ""
    //  this.dest = rc.ftp?.root
    root = (rc.ftp?.root) ?: "/"
    dest = rc.output_file
  }

  fun reportParameters(rc : ReportConnector, datasource : DRef<DataSource>) = ReportParameters(
      format = "csv",
      params = rc.input,
      query = DRef(rc.report + ".report"),
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
      SELECT connectors.*, client.name as 'client' , connector_types.name as 'type' , connector_types.classname
      FROM connectors JOIN client USING(client_id)
           JOIN connector_types USING(connector_type_id)
      """).toList<JsonNode> {asJsonNode()}
    }
    for (n in all) {
      val connector : Connector = configure {from(n)}
      println(connector.name)

      val runConfig :RunConfig? = when(connector.classname) {
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

