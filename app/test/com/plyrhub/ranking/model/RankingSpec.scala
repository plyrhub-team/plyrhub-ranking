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

package com.plyrhub.ranking.model

import com.plyrhub.api.model.StateActive
import com.plyrhub.test.utils.{ConfigUtils, JsonUtils}
import org.specs2.mutable._
import org.specs2.specification.Scope
import play.api.libs.json._

class RankingSpec extends Specification {

  "Ranking serialization / deserialization" should {

    "Serialization Succeed" in new RankingScope {

      // Json minified
      val json = JsonUtils.jsonFromFile("/com/plyrhub/ranking/model/ranking-ok.json")
      val fromFileMin = Json.stringify(json)

      // Build a Ranking Object
      val collections = List(RankingCollection("col1"))
      val name1 = RankingName("es", "rnk1", "long rnk1", true, StateActive())
      val platforms = List(RankingPlatform("desktop", List(name1), true, StateActive()))
      val props = List(RankingPropIsWeekly())
      val rnk = Ranking(collections, platforms, Some(props), StateActive())

      val rnkJson = Json.toJson(rnk)
      val fromMemoryMin = Json.stringify(rnkJson)

      fromFileMin must beEqualTo(fromMemoryMin)

    }

    "Deserialization Fail - Ranking collections/platforms/properties length exceeded upper limit" in new RankingScope {

      val json = JsonUtils.jsonFromFile("/com/plyrhub/ranking/model/ranking-collections-platforms-ranking-lenght-exceeded.json")

      val rnk = json.validate[Ranking]

      // Check the "errors" are the expected ones
      val listError = JsonUtils.errToList(rnk)

      // collections, platforms, properties
      listError must have size 3

      forallWhen(listError) { case (p, m) => {
        m.head must beEqualTo("error.maxLength")
        p must beAnyOf("/collections", "/platforms", "/properties")
      }
      }

    }

    "Deserialization Fail - Ranking with wrong state" in new RankingScope {

      val json = JsonUtils.jsonFromFile("/com/plyrhub/ranking/model/ranking-wrong-state.json")

      val rnk = json.validate[Ranking]

      // Check the "errors" are the expected ones
      val listError = JsonUtils.errToList(rnk)

      listError must have size 1
      listError.head._2.head must beEqualTo("plyrhub.error.not.allowed.value.state")

    }

    "Deserialization Succeed for valid properties - Fail for the wrong ones" in new RankingScope {

      val json = JsonUtils.jsonFromFile("/com/plyrhub/ranking/model/ranking-properties.json")

      val rnk = json.validate[Ranking]

      // Check the "errors" are the expected ones
      val listError = JsonUtils.errToList(rnk)

      listError must have size 1
      listError.head._2.head must beEqualTo("plyrhub.error.non.valid.property")

      // Check the test contains all the valid properties
      val valuesInTest = json \ "properties" \\ "value"
      RankingProp.validProps.map(p => {
        valuesInTest.contains(JsString(p)) must beTrue
      })
    }

    "Deserialization Fail - Fields length limits wrong - lower limit" in new RankingScope {

      val json = JsonUtils.jsonFromFile("/com/plyrhub/ranking/model/ranking-length-limits-wrong-lower-limit.json")

      val rnk = json.validate[Ranking]

      // Check the "errors" are the expected ones
      val listError = JsonUtils.errToList(rnk)

      listError must have size 5

      forallWhen(listError) {
        case (p, m) => {
          p match {
            case s: String if s.startsWith("/collection") => m.head must beEqualTo("plyrhub.error.non.valid.collection.length")
            case _ =>
              p must endWith("lang") or endWith("shortName") or endWith("longName") or endingWith("platform")
              m.head must beEqualTo("error.minLength")
          }
        }
      }

    }

    "Deserialization Fail - Fields length limits wrong - upper limit" in new RankingScope {

      val json = JsonUtils.jsonFromFile("/com/plyrhub/ranking/model/ranking-length-limits-wrong-upper-limit.json")

      val rnk = json.validate[Ranking]

      // Check the "errors" are the expected ones
      val listError = JsonUtils.errToList(rnk)

      listError must have size 5

      forallWhen(listError) { case (p, m) => {

        p match {
          case s: String if s.startsWith("/collection") => m.head must beEqualTo("plyrhub.error.non.valid.collection.length")
          case _ =>
            p must endWith("lang") or endWith("shortName") or endWith("longName") or endingWith("platform")
            m.head must beEqualTo("error.maxLength")
        }
      }
      }

    }

    "Deserialization Fail - the received json is wrong - non custom elements" in new RankingScope {

      val json = JsonUtils.jsonFromFile("/com/plyrhub/ranking/model/ranking-faulty.json")

      val rnk = json.validate[Ranking]

      // Check the "errors" are the expected ones
      val listError = JsonUtils.errToList(rnk)

      listError must have size 8

      forallWhen(listError) {
        case (p, m) => m.head must beEqualTo("error.path.missing")
      }
    }

    "Deserialization Fail - the received json is wrong - custom elements" in new RankingScope {

      val json = JsonUtils.jsonFromFile("/com/plyrhub/ranking/model/ranking-faulty-collections-and-props.json")

      val rnk = json.validate[Ranking]

      // Check the "errors" are the expected ones
      val listError = JsonUtils.errToList(rnk)

      listError must have size 2

      forallWhen(listError) {
        case (p, m) if p.endsWith("collection") => m.head must beEqualTo("plyrhub.error.non.valid.collection")
        case (p, m) if p.endsWith(("prop"))=> m.head must beEqualTo("plyrhub.error.non.valid.property")
      }
    }
  }

  trait RankingScope extends Scope {

    ConfigUtils.setConfigOrigin("config.test.properties")

  }

}

