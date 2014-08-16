
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

package com.plyrhub.api.model

import com.plyrhub.core.log.Loggable
import play.api.libs.json.{JsValue, Json}

case class Result(code: Int, extendedCode: Option[String] = None, additional: Option[Seq[String]] = None)

case class Data(data: JsValue)

case class Pagination(pgtk: String, size: Long, moreResults: Boolean)

case class Response(result: Result, data: Option[Data] = None, pagination: Option[Pagination] = None) extends Loggable

object Response extends Loggable {

  // Implicit Json-Inception
  implicit val resultWrites = Json.writes[Result]
  implicit val dataWrites = Json.writes[Data]
  implicit val paginationWrites = Json.writes[Pagination]
  implicit val responseWrites = Json.writes[Response]


}