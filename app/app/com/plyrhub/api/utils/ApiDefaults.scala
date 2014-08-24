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

package com.plyrhub.api.utils

import com.plyrhub.api.request._
import com.plyrhub.core.context._
import com.plyrhub.core.model.Lang
import play.api.Play.current
import play.api.libs.concurrent.Akka
import play.api.mvc.RequestHeader

import scala.concurrent.Future

object ApiDefaults {

  object i18nDefaults{

    // Default Language
    val defaultLang = Lang("en")

  }

  object ActionDefaults{

    // Usual Params
    val RANKING_ID = Param[P, String]("ranking")
    val MEMBER_ID = Param[P, String]("member")
    val BODY = Param[B, B]("data")
    val FROM_TOP = Param[Q, Int]("fromTop", 5)
    val FROM_BOTTOM = Param[Q, Int]("fromBottom", 5)
    val STATE_TRUE = Param[H, Boolean]("state", true)
    val STATE_FALSE = Param[H, Boolean]("state", false)

    // Usual Actions
    // TODO: "inject" this

    // Owner Identification
    val ownerLoader: RequestHeader => Future[ApiOwner] =
      request =>
        Future.successful(Owner("DEV-OWNER"))

    val noOwnerLoader: RequestHeader => Future[ApiOwner] =
      request =>
        Future.successful(NotAllowedOwner())

    val OwnerIdentification = OwnerIdentificationBuilder(ownerLoader)

    // User Identification
    val userLoader: RequestHeader => Future[ApiUser] =
      request =>
        Future.successful(User("DEV-USER", Profile(Map())))

    val noUserLoader: RequestHeader => Future[ApiUser] =
      request =>
        Future.successful(NotAllowedUser())

    val UserIdentification = UserIdentificationBuilder(userLoader)


    // Default Action
    val AuthAction = OwnerIdentification andThen UserIdentification

  }



}
