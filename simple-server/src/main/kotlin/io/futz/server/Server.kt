package io.futz.server

import com.sun.net.httpserver.HttpServer
import com.sun.net.httpserver.HttpServer.create
import java.net.InetSocketAddress
import java.util.concurrent.Executors.newCachedThreadPool

fun main(args: Array<String>) {
  val port = 9000
  val server: HttpServer = create(InetSocketAddress(port), 0)
  server.executor = newCachedThreadPool()
  server.createContext("/foo", {
    val responseBody = "bar"
    it.sendResponseHeaders(200, responseBody.length.toLong())
    val os = it.responseBody
    os.write(responseBody.toByteArray())
    os.close()
  })
  server.start()
  println("Server started on port $port")
}

