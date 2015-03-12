package org.iotengine.seed

import scala.collection.mutable.{Queue => MQueue}
import scala.concurrent.{Future, Await}
import scals.async.Async.{async, await}

import akka.util.{ByteString}
import akka.actor.{ActorSystem, ActorLogging, Props}
import akka.stream.actor._
import akka.http.model.HttpEntity
import akka.reactivestreams.Publisher

import HttpEntity._

object StreamClientPublisher {

  def apply(port: Int)(implicit system: ActorSystem, materializer: FlowMaterializer): Publisher[ChunkStreamPart] = {
    implicit val ex = system.dispatcher
    val publisherFuture = async {
      val response = await(HttpClient.makeRequest(port, "/"))
      val processor = system.actorOf(Props[DataChunkProcesser])
      val processorSubscriber = ActorSubscriber[ByteString](processor)
      val processPublisher = ActionPublisher[ChunkStreamPart](processor)
      response.entity.dataBytes(materializer).subscribe(processorSubscriber)
      processPublisher
    }
  }
}

import ActorSubscribeMessage._
import ActorPublisherMessage._

class DataChunkProcesser extends ActorSubscriber with ActorPublisher[ChunkStreamPart] with ActorLogging {
  log.info("Data Chunk Stream Subscription Started")
  val queue = MQueue[ByteString]()
  var requested = 0L

  def receive = {
    case Request(cnt) => requested += cnt; sendDataChunks()
    case Cancel => onComplete(); context.stop(self)
    case OnNext(bytes: ByteString) => queue.enqueue(bytes); sendDataChunks()
    case OnComplete => onComplete()
    case OnError(err) => OnError(err)
    case _ =>
  }

  def sendDataChunks() {
    while(requested > 0 && queue.nonEmpty && isActive && totalDemand > 0) {
      println("Sending Data Chunk -- DataChunkProcesser")
      onNext(ChunkStreamPart(queue.dequeue()))
      requested -= 1
    }
  }

  val requestStrategy = new MaxInFlightRequestStrategy(50) {
    def inFlightInternally(): Int = { println("In flignt internally: " + queue.size); queue.size }
  }
}