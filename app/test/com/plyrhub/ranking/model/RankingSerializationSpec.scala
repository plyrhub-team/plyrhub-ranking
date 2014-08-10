package com.plyrhub.ranking.model

import com.plyrhub.api.model.StateActive
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import play.api.libs.json.Json

class RankingSerializationSpec extends Specification {

  "Ranking object" should {
    "be deserialized" in new RankingsScope {

      val s = Json.prettyPrint(rnkJson)

      println(s)

    }
  }


  trait RankingsScope extends Scope {

    val collections = List(RankingCollection("all"), RankingCollection("under-25"))

    val name1 = RankingName("es", "Todos", "Ranking para todos los usuarios", true, StateActive())
    val name2 = RankingName("en", "AllUsers", "The ranking of all Users", true, StateActive())

    val platforms = List(RankingPlatform("web", List(name1, name2), true, StateActive()),
      RankingPlatform("iphone", List(name1, name2), false, StateActive()))

    val props = List(RankingPropIsEternal(), RankingPropNoop())

    val rnk = Ranking(collections, platforms, props, StateActive())

    val rnkJson = Json.toJson(rnk)

    val z = 0

  }
}
