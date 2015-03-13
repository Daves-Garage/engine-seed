package org.iotengine.seed

import scala.concurrent.duration._

import akka.actor.{ActorSystem}
import akka.util.{Timeout}
import akka.pattern.ask
import akka.io.IO 
import akka.http.Http 
import akka.http.model.{HttpRequest, HttpResponse}
import akka.http.stream.scaladsl.Flow
import akka.stream.{ActorFlowMaterializer}

object HttpServer {
  implicit val askTimeout: Timeout = 1000.millis

  def bindServer(port: Int)(handler: (HttpRequest) => HttpResponse)(implicit system: ActorSystem, materializer: ActorFlowMaterializer) {
    implicit val ec = system.dispatcher
    val bindingFuture = IO(Http) ? Http.Bind(interface = "localhost", port = port)
    bindFuture foreach {
      case Http.ServerBinding(localAddress, connectStream) =>
      Flow(connectionStream).foreach({
        case Http.IncomingConnection(remoteAddress, requestProducer, responseConsumer) => 
           println("Accepted new connection from " + remoteAddress)
           Flow(requestProducer).map(handler).produceTo(responseConsumer)
        })
    }

  }
}