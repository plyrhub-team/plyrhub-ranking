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

package com.plyrhub.core.context

import com.plyrhub.core.model.Lang
import com.plyrhub.core.utils.Misc

import scala.util.Try

sealed trait ApiOwner

case class Owner(id: String) extends ApiOwner

object Owner {
  def apply(apiOwner: ApiOwner): Option[String] = {
    Try {
      val owner = apiOwner.asInstanceOf[Owner]
      Some(owner.id)
    }.getOrElse {
      None
    }
  }
}

case class NoOwner() extends ApiOwner

case class NotAllowedOwner() extends ApiOwner

sealed trait ApiProfile

case class Profile(scopes: Map[String, String]) extends ApiProfile

case class NoProfile() extends ApiProfile

sealed trait ApiUser

case class User(id: String, profile: Profile) extends ApiUser

case class NoUser() extends ApiUser

case class NotAllowedUser() extends ApiUser

sealed trait ApiOperationContext

case class OperationContext(owner: ApiOwner, user: ApiUser, lang: Lang) extends ApiOperationContext {

  val id = Misc.uniqueID

}

case class NoOperationContext() extends ApiOperationContext







