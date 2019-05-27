//#!/usr/bin/env kotlinc -script
//INCLUDE  Main.kt
//INCLUDE imports.kts
@file:MavenRepository("docstore","file:/nexstra/docstore/build")
@file:DependsOn("org.jetbrains.exposed:exposed:0.9.1")
@file:DependsOn("mysql:mysql-connector-java:5.1.33")
@file:DependsOn("content-services:config:1.4.36")
@file:Include("imports.kts")
