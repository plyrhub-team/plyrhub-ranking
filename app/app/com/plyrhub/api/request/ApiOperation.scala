/*
 * Copyright (C) 2014  Enrique Aguilar Esnaola
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License Version 3
 *     as published by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.plyrhub.api.request

import akka.actor.SupervisorStrategy.Stop
import akka.actor.{Actor, ActorRef, OneForOneStrategy, Props}
import com.plyrhub.api.utils.HttpResults._
import com.plyrhub.api.utils.{ApiDefaults, Utils}
import com.plyrhub.core.context.{ApiOperationContext, OperationContext}
import com.plyrhub.core.log.Loggable
import com.plyrhub.core.protocol._
import play.api.mvc.Result

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.{Future, Promise}
import scala.reflect.ClassTag

// TODO: review implicits
// import scala.concurrent.ExecutionContext.Implicits._

object ApiOperationDefaults {

  type initBlockType = (ApiOperationContext, ServiceMessage) => OperationProtocol
  type successBlockType = PartialFunction[ServiceSuccess, Result]

  val onInitDefault: initBlockType = (ctx, message) => StartOperation(ctx, message)

  val onSuccessDefault: successBlockType = {
    case _ => API_SIMPLE_SUCCESS
  }

}

import com.plyrhub.api.request.ApiOperationDefaults._

trait ApiOperation {

  val onInitBlock: initBlockType
  val onSuccessBlock: successBlockType

  def onInit(newOnInitBlock: initBlockType): ApiOperation

  def onSuccess(newOnSuccess: successBlockType): ApiOperation


  def launch[S <: Actor : ClassTag](octx: OperationContext, message: ServiceMessage): Future[Result] = {

    val f = ApiRequestActor[S](onInitBlock, octx, message)

    f
      .map {
      r =>
        r.fold(_ => API_GENERIC_ERROR(Seq("plyrhub.generic.error")), s => doResult(s))
    }
      // Unexpected exception, already logged in the "supervisory-strategy"
      .recover {
      case _ => API_GENERIC_ERROR(Seq("plyrhub.generic.error"))
    }

  }

  lazy val doResult = onSuccessBlock orElse doFailure
  val doFailure: successBlockType = {
    case _ => API_GENERIC_ERROR(Seq("plyrhub.generic.error"))
  }

}

class ApiOperationImpl(override val onInitBlock: initBlockType, override val onSuccessBlock: successBlockType) extends ApiOperation {

  override def onInit(newOnInitBlock: initBlockType) = new ApiOperationImpl(newOnInitBlock, onSuccessBlock)

  override def onSuccess(newOnSuccessBlock: successBlockType) = new ApiOperationImpl(onInitBlock, newOnSuccessBlock)

}

object ApiOperation extends {

  def apply() = {
    new ApiOperationImpl(onInitDefault, onSuccessDefault)
  }

}

class ApiRequestActor(targetProps: Props, init: initBlockType, octx: OperationContext, message: ServiceMessage, p: Promise[Either[ServiceFailure, ServiceSuccess]]) extends Actor with Loggable {

  lazy val target: ActorRef = context.actorOf(targetProps)

  target ! init(octx, message)

  Utils.actorWatcher ! WatchThis(self)
  Utils.actorWatcher ! WatchThis(target)

  override val supervisorStrategy =
    OneForOneStrategy() {

      // If there is an exception on the ServiceActor we just stop
      case e => {
        // Log the error
        log.error(s"There was an error processing: ${message.toString}")
        log.error(s"The exception was: ${e.getMessage}")
        p.failure(e)

        // TODO: review child-stop
        Stop
      }
    }

  override def receive = {

    case OperationCompleted(result: Either[ServiceFailure, ServiceSuccess]) => {

      p.success(result)
      context.stop(self)
    }

    // TODO: review what to do with the unhandled if anything

  }
}

object ApiRequestActor {

  def apply[S <: Actor : ClassTag](init: initBlockType, octx: OperationContext, message: ServiceMessage): Future[Either[ServiceFailure, ServiceSuccess]] = {

    val targetProps = Props(implicitly[ClassTag[S]].runtimeClass)

    val p = Promise[Either[ServiceFailure, ServiceSuccess]]

    ApiDefaults.defaultActorSystem.actorOf(Props(classOf[ApiRequestActor], targetProps, init, octx, message, p))

    p.future

  }
}
