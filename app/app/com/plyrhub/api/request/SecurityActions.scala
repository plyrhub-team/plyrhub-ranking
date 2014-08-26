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

import ApiHttpResults._
import com.plyrhub.core.context._
import com.plyrhub.core.log.Loggable
import play.api.i18n.Lang
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

// TODO: review "Execution.Implicits._"

object SecurityActions {

  def requestLang[A](implicit lang:Lang) = lang

}

import com.plyrhub.api.request.SecurityActions._

case class OwnerRequest[A](owner: Owner, request: Request[A]) extends WrappedRequest[A](request)

class OwnerIdentificationBuilder(ownerInfo: RequestHeader => Future[ApiOwner]) extends ActionBuilder[OwnerRequest] with Loggable {

  override def invokeBlock[A](request: Request[A], block: (OwnerRequest[A]) => Future[Result]) = identify(block)(request)

  def identify[A](block: (OwnerRequest[A]) => Future[Result])(implicit request: Request[A]) = {

    // TODO: review, at this time the language seems not to be calculated yet
    implicit val lang = requestLang

    ownerInfo(request).flatMap {
      case o: NotAllowedOwner => Future.successful(ApiUnauthorizedError(Seq("plyrhub.not.authorized.owner")))
      case o: Owner => block(new OwnerRequest(o, request))
      case x => {
        // This should never happen ... but who knows
        log.error(s"A non expected value was received during owner authorization: ${x.toString}")
        Future.successful(ApiUnauthorizedError(Seq("plyrhub.owner.authorization.generic.error")))
      }
    }
  }
}

object OwnerIdentificationBuilder {

  def apply(ownerInfo: RequestHeader => Future[ApiOwner]): OwnerIdentificationBuilder = new OwnerIdentificationBuilder(ownerInfo)

}

case class UserRequest[A](user: ApiUser, owner: Owner, request: Request[A]) extends WrappedRequest[A](request)

object UserRequest {

  def convert[A](request: Request[A]) = request match {
    case r: UserRequest[A] => Some(r)
    case _ => None
  }

}

class UserIdentificationBuilder(userInfo: RequestHeader => Future[ApiUser]) extends ActionBuilder[UserRequest] with Loggable {

  override def invokeBlock[A](request: Request[A], block: (UserRequest[A]) => Future[Result]) = identify(block)(request)

  def identify[A](block: (UserRequest[A]) => Future[Result])(implicit request: Request[A]) = {

    val owner = extractOwner(request)

    // TODO: review, at this time the language seems not to be calculated yet
    implicit val lang = requestLang

    owner.map(owner => userInfo(request).flatMap {

      case u: NotAllowedUser => Future.successful(ApiUnauthorizedError(Seq("plyrhub.not.authorized.user")))
      case u: User => block(new UserRequest(u, owner, request))
      case x => {
        // This should never happen ... but who knows
        log.error(s"A non expected value was received during user authorization: ${x.toString}")
        Future.successful(ApiUnauthorizedError(Seq("plyrhub.user.authorization.generic.error")))
      }
    }
    ).getOrElse(Future.successful(ApiGenericError(Seq("plyrhub.non.expected.authorization.error"))))

  }

  def extractOwner[A](req: Request[A]): Option[Owner] = req match {
    case oRq: OwnerRequest[A] => Some(oRq.owner)
    case _ =>
      log.error(s"Hey!! UserId... after OwnerId...")
      None // This only can be if UserId is not call after OwnerId
  }

}

object UserIdentificationBuilder {

  def apply(userInfo: RequestHeader => Future[ApiUser]): UserIdentificationBuilder = new UserIdentificationBuilder(userInfo)

}
