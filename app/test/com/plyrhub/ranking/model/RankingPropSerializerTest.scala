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

package com.plyrhub.ranking.model

import org.specs2.mutable._
import org.specs2.specification.Scope
import play.api.libs.json._
import play.api.libs.functional.syntax._

class RankingPropSerializerTest extends Specification {

  trait RankingPropScope extends Scope {

    case class TestRankingProp(rp:RankingProp)

    object TestRankingProp {
      implicit val testRankingPropFormat = Json.format[TestRankingProp]
    }
  }

  "RankingProp" should {

    "be deserialized " in new RankingPropScope() {

      val trpJson =
        """
          |{"rp":{"prop":"time","value":"eternal"}}
        """.stripMargin
      val trpJsValue = Json.parse(trpJson)
      val trp = Json.fromJson[TestRankingProp](trpJsValue).get

      TestRankingProp(RankingPropIsEternal()) must equalTo(trp)

    }

    "fail desearialization - due to prop" in new RankingPropScope {

      val trpJson =
        """
          |{"rp":{"prop1":"time","value":"eternal"}}
        """.stripMargin
      val trpJsValue = Json.parse(trpJson)
      val trp = Json.fromJson[TestRankingProp](trpJsValue)

      trp must beAnInstanceOf[JsError]

      val jserror = trp.asInstanceOf[JsError]
      jserror.errors.head._2.head.message must equalTo("plyrhub.error.non.valid.property")

    }

    "fail desearialization - due to value" in new RankingPropScope {

      val trpJson =
        """
          |{"rp":{"prop":"time","value1":"eternal"}}
        """.stripMargin
      val trpJsValue = Json.parse(trpJson)
      val trp = Json.fromJson[TestRankingProp](trpJsValue)

      trp must beAnInstanceOf[JsError]

      val jserror = trp.asInstanceOf[JsError]
      jserror.errors.head._2.head.message must equalTo("plyrhub.error.non.valid.property")

    }

    "be serialized" in new RankingPropScope {

      val trp = TestRankingProp(RankingPropIsMonthly())

      val trpJson = Json.toJson[TestRankingProp](trp).toString()

      trpJson must equalTo("""{"rp":{"prop":"time","value":"monthly"}}""")

    }

  }


}

