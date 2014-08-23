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

package com.plyrhub.core.protocol

import akka.actor.ActorRef
import com.plyrhub.core.context.ApiOperationContext

trait ServiceMessage

sealed trait ServiceResponse extends ServiceMessage

trait ServiceSuccess extends ServiceResponse

trait ServiceFailure extends ServiceResponse

case class SimpleSuccess() extends ServiceSuccess
case class SimpleFailure() extends ServiceFailure

sealed trait OperationProtocol

case class StartOperation(context: ApiOperationContext, message: ServiceMessage) extends OperationProtocol

case class OperationCompleted(result: Either[ServiceFailure, ServiceSuccess]) extends OperationProtocol

object Complete {

  def apply(sendTo:ActorRef, response: ServiceResponse) = response match {
    case success: ServiceSuccess => sendTo ! OperationCompleted(Right(success))
    case failure: ServiceFailure => sendTo ! OperationCompleted(Left(failure))
  }


}

