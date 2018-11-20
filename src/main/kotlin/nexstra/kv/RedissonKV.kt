
package nexstra.kv


import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import nexstra.ddata.*
import nexstra.services.config.copyAsProperites
import org.redisson.Redisson
import org.redisson.api.RBucket
import org.redisson.client.codec.JsonJacksonMapCodec
import org.redisson.client.protocol.Decoder
import org.redisson.codec.JsonJacksonCodec
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
    class simplek( val i: Int , val s: String ) {
      constructor() : this(1,"foo")
    }


fun main() {
    val kjackson =  jacksonObjectMapper()
    val kcodec = JsonJacksonCodec(kjackson)
    val config = Config().apply {
      transportMode = TransportMode.NIO
      useSingleServer().setAddress("redis://localhost:32770")
      codec = kcodec // JsonJacksonCodec(jacksonObjectMapper())
    }

    val client = Redisson.create(config)
/*
    val longObject = client.getAtomicLong("myLong")
    longObject.set(3)
    val ok = longObject.compareAndSet(3,100)
    val newval = longObject.get()
    println("ok: $ok newval: $newval  should be 100")

    val mappy = client.getMap<String,String>("myMap",JsonJacksonMapCodec(String::class.java, String::class.java,kjackson))
    mappy["key"] = "value"
    println( mappy["key"] )
  //  val fullCodec = JsonJacksonCodec(kjackson.copy().enableDefaultTyping())
*/
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
/*

  val mapk = client.getMap<String,simplek>("mySimpleMap",JsonJacksonMapCodec(String::class.java, simplek::class.java))
  mapk["key"] = simplek(2,"simple")
  println( mapk["key"] )

*/

   // client.liveObjectService.

   client.shutdown()

}
