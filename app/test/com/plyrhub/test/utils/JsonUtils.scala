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

package com.plyrhub.test.utils

import play.api.libs.json._

import scala.io.Source

object JsonUtils {

  def jsonFromFile(resource: String) = {

    val in = getClass.getResourceAsStream(resource)

    val source = Source.fromInputStream(in)
    val lines = source.getLines().mkString
    source.close()

    Json.parse(lines)

  }

  def errToList(errorResult:JsResult[Any]):Seq[(String, Seq[String])] = {

    val errJsError = errorResult.asInstanceOf[JsError]

    errJsError.errors map {
      case (path, errs) => path.toString() -> errs.map(_.message)
    }
  }

}
