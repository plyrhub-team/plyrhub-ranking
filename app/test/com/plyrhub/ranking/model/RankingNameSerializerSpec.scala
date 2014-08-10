/*
 * Copyright (C) 2014  Enrique Aguilar Esnaola
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.plyrhub.ranking.model

import com.plyrhub.api.model.StateActive
import org.specs2.mutable._
import org.specs2.specification.Scope
import play.api.data.validation.ValidationError
import play.api.libs.json._

class RankingNameSerializerSpec extends Specification {

  trait RankingNameScope extends Scope {

    val rnJson =
      """
        |{
        |      "lang" : "es",
        |      "shortName" : "Todos",
        |      "longName" : "Ranking para todos los usuarios",
        |      "default" : true,
        |      "status" : "active"
        |}
      """.stripMargin

    val rnJsonWrong =
      """
        |{
        |      "lang" : "es",
        |      "longName" : "Ranking para todos los usuarios",
        |      "default" : true,
        |      "status" : "active"
        |}
      """.stripMargin


    def errToJson(errors: Seq[(JsPath, Seq[ValidationError])]): JsValue = {
      val jsonErrors: Seq[(String, JsValue)] = errors map {
        case (path, errs) => path.toJsonString -> Json.toJson(errs.map(_.message))
      }
      JsObject(jsonErrors)
    }

  }

  "RankingName" should {

    "be deserialized " in new RankingNameScope() {

      val jo = Json.parse(rnJson)
      val rn = Json.fromJson[RankingName](jo)

      rn must beAnInstanceOf[JsSuccess[RankingName]]

    }

    "be serialized" in new RankingNameScope {

      val rn = RankingName("es", "Todos", "Ranking para todos los usuarios", true, StateActive())

      val jo = Json.toJson[RankingName](rn)

      jo must beAnInstanceOf[JsObject]

    }

    "fail deserialization - missed fields[shortName]" in new RankingNameScope() {

      val jo = Json.parse(rnJsonWrong)
      val rn = Json.fromJson[RankingName](jo)

      rn must beAnInstanceOf[JsError]

      val errs = rn.asInstanceOf[JsError]

      val jsErr = errToJson(errs.errors)

    }

  }


}

