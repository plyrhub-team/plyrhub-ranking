/*
 * Copyright (C) 2014  Enrique Aguilar Esnaola
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation (version 3 of the
 *     License).
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

  def apply(response: ServiceResponse) = response match {
    case success: ServiceSuccess => OperationCompleted(Right(success))
    case failure: ServiceFailure => OperationCompleted(Left(failure))
  }

}

