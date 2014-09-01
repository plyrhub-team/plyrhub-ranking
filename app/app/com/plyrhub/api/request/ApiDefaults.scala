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

import com.plyrhub.core.context._
import play.api.mvc.RequestHeader

import scala.concurrent.Future

object ApiDefaults {

  object ActionDefaults{

    // Usual Params
    val RANKING_ID = Param[P, String]("ranking")
    val MEMBER_ID = Param[P, String]("member")
    val BODY = Param[B, B]("data")
    def FROM_TOP(default:Int) = Param[Q, Int]("fromTop", default)
    def FROM_BOTTOM(default:Int) = Param[Q, Int]("fromBottom", default)
    val PLATFORM = Param[Q, String]("platform", "")
    val STATE_TRUE = Param[H, Boolean]("state", true)
    val STATE_FALSE = Param[H, Boolean]("state", false)

    // Usual Actions
    // TODO: "inject" this

    // Owner Identification
    val ownerLoader: RequestHeader => Future[ApiOwner] =
      request =>
        Future.successful(Owner("DEV_OWNER"))

    val noOwnerLoader: RequestHeader => Future[ApiOwner] =
      request =>
        Future.successful(NotAllowedOwner())

    val OwnerIdentification = OwnerIdentificationBuilder(ownerLoader)

    // User Identification
    val userLoader: RequestHeader => Future[ApiUser] =
      request =>
        Future.successful(User("DEV_USER", Profile(Map())))

    val noUserLoader: RequestHeader => Future[ApiUser] =
      request =>
        Future.successful(NotAllowedUser())

    val UserIdentification = UserIdentificationBuilder(userLoader)


    // Default Action
    val AuthAction = OwnerIdentification andThen UserIdentification

  }



}
