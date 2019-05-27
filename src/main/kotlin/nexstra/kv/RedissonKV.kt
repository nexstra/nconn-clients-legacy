
package nexstra.kv


import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import nexstra.ddata.*
import nexstra.kv.mysqlKVS.copyTo
import nexstra.kv.mysqlKVS.insert
import nexstra.services.config.copyAsProperites
import nexstra.services.config.pretty
import org.redisson.Redisson
import org.redisson.api.RBucket
import org.redisson.api.RFuture
import org.redisson.api.RedissonClient
import org.redisson.client.codec.JsonJacksonMapCodec
import org.redisson.client.protocol.Decoder
import org.redisson.codec.JsonJacksonCodec
import org.redisson.codec.KryoCodec
import org.redisson.config.Config
import org.redisson.config.TransportMode
import java.io.File
import java.math.BigDecimal
import java.net.URI
import java.net.URL
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import javax.print.attribute.IntegerSyntax
import kotlin.system.measureNanoTime
import kotlin.system.measureTimeMillis
//@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
    data class simplek( val i: Int , val s: String ) {
      constructor() : this(0,"")
    }

    class komplex( var s1 :  simplek, val s2: simplek ) {
    constructor() : this( simplek(2,"two"),simplek(3,"three"))
      var list : MutableList<simplek>
      init {
          list = mutableListOf(s1,s2)
      }
      fun and(i:Int,s:String) = list.add(simplek(i,s))
    }

object redisKVS {

  fun KVS.insert(client: RedissonClient) : RFuture<Void> {
    val rkey = "kv_$id"
    val rmap = client.getMap<String,String>(rkey)
   return rmap.putAllAsync( values )
  }


  fun ResultSet.copyTo(ps : PreparedStatement) {

    for (i in 1..metaData.columnCount) {
      ps.setObject(i, getObject(i))
    }
    ps.addBatch()
  }

  fun loadKVS(table : String, dsSource : String,  sql : String) {

    val jdbcSrc = DRef.fromRef<JDBCSource>(dsSource.removePrefix("@")).deref()


    jdbcSrc.withConnection {
      val s = this
      val config = Config().apply {
   //    codec = KryoCodec()
        useSingleServer().setAddress("redis://redis:6379")

        transportMode = TransportMode.NIO
      }
      val client = Redisson.create(config)

        var i = 0
        val ms = measureTimeMillis {
          var tns : Long = 0L
          val BS = 1000
          // s.query(sql).forEach {
          s.prepareStatement(sql).run {
            this.fetchSize = Integer.MIN_VALUE

            this.executeQuery().forEach {
              val newBatch = (++i % BS) == 0
              val ns = measureNanoTime {
                  val kvs = toKVS()
                  kvs.insert(client).await()
              }
              tns += ns
              if (newBatch) {
                println("${i} ${(tns / BS).toDouble() / 1_000_000.0} ms/rec")
                tns = 0
              }
            }
            println("Finalizing")
            client.shutdown()
          }
        }
        println("${ms} ms total  ${ms.toDouble() / i.toDouble()} ms/rec")
      }
  }
}

fun main() {

/*
    val kjackson =  jacksonObjectMapper()
    val kcodec = JsonJacksonCodec(kjackson)
    val config = Config().apply {
      transportMode = TransportMode.NIO
      useSingleServer().setAddress("redis://localhost:32770")
      codec = kcodec // JsonJacksonCodec(jacksonObjectMapper())
    }

    val client = Redisson.create(config)

    val longObject = client.getAtomicLong("myLong")
    longObject.set(3)
    val ok = longObject.compareAndSet(3,100)
    val newval = longObject.get()
    println("ok: $ok newval: $newval  should be 100")

    val mappy = client.getMap<String,String>("myMap",JsonJacksonMapCodec(String::class.java, String::class.java,kjackson))
    mappy["key"] = "value"
    println( mappy["key"] )
  //  val fullCodec = JsonJacksonCodec(kjackson.copy().enableDefaultTyping())

    val s  = client.getBucket<simplek>("mySimple",
      object : JsonJacksonMapCodec(String::class.java, simplek::class.java) {
        override fun getValueDecoder() : Decoder<Any> {
          return super.getMapValueDecoder()
        }
      }

      )
   s.set( simplek(1,"hi" ) )
    val simple2 = s.get()
       println( simple2 )

  val mapk = client.getMap<String,simplek>("mySimpleMap",JsonJacksonMapCodec(String::class.java, simplek::class.java))
  mapk["key"] = simplek(2,"simple")
  println( mapk["key"] )


   // client.liveObjectService.
*/
   val config = Config().apply {
      codec = KryoCodec()
     useSingleServer().setAddress("redis://redis:6379")

    transportMode = TransportMode.NIO
   }
   val client = Redisson.create(config)

   val sk = client.getBucket<komplex>("myBucket")
   val ss = simplek(100,"x")
   val cx = komplex(ss,simplek(200,"y"))

   cx.and(300,"z")
   sk.set(cx)
   val s = sk.get()
   println(s.pretty())
  client.shutdown()

  redisKVS.loadKVS( "kv_elem" , "@file:/nexstra/nconn-legacy/source-prod.ds",
      "SELECT * from dist_elem_item" )


}
