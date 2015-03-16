package org.iotengine.seed

import scala.concurrent.duration._

import akka.actor.{ActorSystem}
import akka.util.{Timeout}
import akka.stream.{ActorFlowMaterializer}
import akka.io.IO


import akka.http.Http
import akka.http.model.{HttpRequest, HttpResponse}



object HttpServer {
  implicit val askTimeout: Timeout = 1000.millis

  def bindServer(port: Int)(handler: (HttpRequest) => HttpResponse)(implicit system: ActorSystem, materializer: ActorFlowMaterializer) {
    implicit val ec = system.dispatcher
    val bindingFuture = Http().bind(interface = "localhost", port = port)
    bindingFuture.connections foreach {
      case Http.ServerBinding(localAddress) =>
      Flow(connectionStream).foreach({
        case Http.IncomingConnection(remoteAddress, requestProducer, responseConsumer) => 
           println("Accepted new connection from " + remoteAddress)
           Flow(requestProducer).map(handler).produceTo(responseConsumer)
        })
    }
  }
}