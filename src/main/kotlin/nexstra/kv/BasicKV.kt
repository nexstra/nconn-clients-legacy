package nexstra.kv

import com.amazonaws.services.dynamodbv2.xspec.BS
import nexstra.ddata.*
import java.io.File
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import javax.print.attribute.IntegerSyntax
import kotlin.system.measureNanoTime
import kotlin.system.measureTimeMillis

fun source( secret: String  , database: String ) =
  DataSources.getJDBCSource(DataSource("mysql" , "hpe" , secret ))

fun source( datasource: DRef<DataSource> ) =
  DataSources.getJDBCSource(  DataSource.fromSource("@" + datasource.ref ) )

class KV( val id: Long , val key: String , val value: String )

class KVS(val id: Long, val values: MutableMap<String,String> = mutableMapOf<String,String>()) {
  fun toKV() = values.entries.map {  (k,v) -> KV(id , k, v) }
  constructor(kvs: List<KV>) : this(kvs.first().id ) {
    kvs.forEach {add(it.key, it.value)}
  }
  fun add(key: String, value: String) = values.put(key,value)
}

fun ResultSet.toKVS() = KVS(getLong(1)).also { kvs ->
    for (i in 1..metaData.columnCount) {
      getString(i)?.let {  value ->
        value.trim().takeIf { ! it.isEmpty() }?.let { v ->
          kvs.add(metaData.getColumnLabel(i), v)
        }
      }
    }
  }


fun KVS.insert( ps: PreparedStatement  ) {
  values.forEach {
    ps.clearParameters()
    ps.setLong(1, id)
    ps.setString(2, it.key)
    ps.setString(3, it.value)
    ps.addBatch()
  }
}


fun ResultSet.copyTo( ps: PreparedStatement ) {

    for( i in 1..metaData.columnCount) {
      ps.setObject(i, getObject(i))
    }
    ps.addBatch()
}
fun loadKVS( table: String , dsSource: String , dsTarget: String , sql : String ) {

  val src = DRef.fromRef<DataSource>(dsSource.removePrefix("@"))
  val target = DRef.fromRef<DataSource>(dsTarget.removePrefix("@"))
  val jdbcSrc = source(src)
  val jdbcTarget = source(target)
  jdbcSrc.withConnection {
    val s = this
    jdbcTarget.withConnection {
      val t = this
      var i = 0
      val ms = measureTimeMillis {
        var tns : Long = 0L
        val BS = 1000
        // s.query(sql).forEach {
        s.prepareStatement(sql).run {
          this.fetchSize = Integer.MIN_VALUE
          val psInsert = t.prepareStatement("INSERT IGNORE INTO ${table} (`id`,`key`,`value`) VALUES(?,?,?)")
         //  val psInsert = t.prepareStatement("INSERT INTO ${table} VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)")
          t.autoCommit = false
          this.executeQuery().forEach {
            val newBatch = (++i % BS ) == 0
            val ns = measureNanoTime {
              if( true ) {
                val kvs = toKVS()
                kvs.insert(psInsert)
              } else {
                copyTo(psInsert)
              }
                if (newBatch) {
                  psInsert.executeBatch()
                  t.commit()
                }
            }
            tns += ns
            if (newBatch) {
              println("${i} ${(tns / BS).toDouble() / 1_000_000.0 } ms/rec")
              tns = 0
              psInsert.clearBatch()
            }
          }
          println("Finalizing")
          psInsert.executeBatch()
          psInsert.clearBatch()
          psInsert.close()
        }
      }
      println("${ms} ms total  ${ms.toDouble() / i.toDouble()} ms/rec" )
    }
  }
}

fun main(args:Array<String>) {
  loadKVS( "kv_elem" , "@file:/nexstra/nconn-legacy/source-prod.ds", "@file:/nexstra/nconn-legacy/target.ds" ,
     "SELECT * from dist_elem_item" )
}

