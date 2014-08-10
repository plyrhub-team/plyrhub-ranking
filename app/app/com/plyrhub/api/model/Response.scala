
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

package com.plyrhub.api.model

import com.plyrhub.core.log.Loggable
import play.api.libs.json.{JsValue, Json}

//  {
//    "result": {
//      "code": "string",
//      "extendedCodes": [
//    {
//      "extendedCode": "string"
//    }
//      ]
//    },
//    "data": {
//      "ranking": [
//    {
//      "rnk": "string",
//      "name": "string",
//      "desc": "string",
//      "status": "string"
//    }
//      ]
//    },
//    "pagination": {
//      "pgtk": "string",
//      "size": "long",
//      "moreResults": "boolean"
//    }
//  }

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