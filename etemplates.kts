#!/home/dlee/bin/kscript
//#!/usr/bin/env kotlinc -script
//INCLUDE  Main.kt
//INCLUDE imports.kts
@file:MavenRepository("kotlinx", "https://kotlin.bintray.com/kotlinx" )
@file:MavenRepository("exposed","https://dl.bintray.com/kotlin/exposed" )
@file:MavenRepository("docstore","file:/nexstra/docstore/build")
@file:DependsOn("org.jetbrains.exposed:exposed:0.9.1")
@file:DependsOn("mysql:mysql-connector-java:5.1.33")
@file:DependsOn("content-services:config:1.4.36")
@file:Include("imports.kts")
/*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.*


object Properties: Table(name="properties") {
  val name = varchar("name",256).primaryKey()
  val value = varchar("value", 256)
}


object Template : Table(name="template"){

  var templateId =  long("template_id")
  var clientId = long("client_id")
  var distMethodId = long("dist_method_id")
  var name = varchar("name",256)
  var description = varchar("description",256)
  var body = varchar("body",256)
  var mtime = datetime("mtime")
  var templateType =varchar("template_type",256)

}


@Suppress("UNUSED_PARAMETER")
fun main(args:Array<String>){
 val out = if(args.size > 0 ) args[0] else "build/templates"
 val outdir = File(out)
 outdir.mkdirs()
  Database.connect(
      url="jdbc:mysql://cds.rds.nexstra.com:3306/ssi-new", 
       driver="org.gjt.mm.mysql.Driver" , user="nexstra", password="1derful!" )
    transaction {
      for( p in Template.selectAll() ) {
        println( "${p[Template.name]} = ${p[Template.description]}" )
        val name = p[Template.name].replace("[^a-zA-Z_0-9-]".toRegex(),"_")
        val ext = p[Template.templateType].toLowerCase()

        File(outdir,"${name}.${ext}").writeText( p[Template.body] )

      }
  }
}

main(arrayOf())
*/
