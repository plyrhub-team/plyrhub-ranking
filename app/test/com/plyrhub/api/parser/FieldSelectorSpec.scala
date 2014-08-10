package com.plyrhub.api.parser

import org.specs2.specification.Scope
import play.api.libs.json._

class FieldSelectorSpec extends org.specs2.mutable.Specification {

  "FieldSelector" should {

    "select b and c" in new Jsons {

      val expr = KeySelectExpr(Map(
        "a" -> FixedExpr(matches = true),
        "b" -> FixedExpr(matches = true),
        "c" -> FixedExpr(matches = true)
      ))

      val s = expr.select(jsonOne)

      s must beSome[JsValue]
      s.get must be equalTo jsonOne

    }

    "select c" in new Jsons {

      val expr = KeySelectExpr(Map(
        "a" -> FixedExpr(matches = true),
        "c" -> FixedExpr(matches = true)
      ))

      val s = expr.select(jsonOne)

      s must beSome[JsValue]
      s.get \ "c" must be equalTo jsonOne_cBranch

    }

    "select c.d" in new Jsons {

      val expr = KeySelectExpr(Map(
        "a" -> FixedExpr(matches = true),
        "c" -> KeySelectExpr(Map(
          "d" -> FixedExpr(matches = true)
        ))
      ))

      val s = expr.select(jsonOne)

      s must beSome[JsValue]
      s.get \ "c" \ "d" must be equalTo jsonOne_cdBranch

    }

    "select c.d and c.e.f" in new Jsons {

      val expr = KeySelectExpr(Map(
        "a" -> FixedExpr(matches = true),
        "c" -> KeySelectExpr(Map(
          "d" -> FixedExpr(matches = true),
          "e" -> KeySelectExpr(Map(
            "f" -> FixedExpr(true)
          ))
        ))
      ))

      val s = expr.select(jsonOne)

      s must beSome[JsValue]
      (s.get \ "c" \ "d" ) must be equalTo jsonOne_cdBranch
      (s.get \ "c" \ "e" \\ "f") must have size 3
      (s.get \ "c" \ "e" \\ "g") must beEmpty

    }

    "select c.d and c.e.f (f=!we)" in new Jsons {

      val expr = KeySelectExpr(Map(
        "a" -> FixedExpr(matches = true),
        "c" -> KeySelectExpr(Map(
          "d" -> FixedExpr(matches = true),
          "e" -> KeySelectExpr(Map(
            "f" -> NotEqualExpr("we")
          ))
        ))
      ))

      val s = expr.select(jsonOne)

      s must beSome[JsValue]
      s must beSome[JsValue]
      (s.get \ "c" \ "d" ) must be equalTo jsonOne_cdBranch
      (s.get \ "c" \ "e" \\ "g") must beEmpty
      (s.get \ "c" \ "e" \\ "f") must haveSize(2)

    }

  }

}


trait Jsons extends Scope {

  val jsonOne = JsObject(
    Seq(
    "b" -> JsNumber(42),
    "c" -> JsObject(
      Seq(
        "d" -> JsString("def"),
        "e" -> JsArray(
          Seq(
            JsObject(Seq("f" -> JsNumber(1), "g" -> JsNumber(27))),
            JsObject(Seq("f" -> JsString("we"), "g" -> JsNumber(27))),
            JsObject(Seq("f" -> JsBoolean(true), "g" -> JsString("isTrue")))
          )
        )
      )
    )
   )
  )

  val t_cBranch = (__ \ "c").json.pick
  val jsonOne_cBranch = jsonOne.transform(t_cBranch).get

  val t_cdBranch = (__ \ "c" \ "d").json.pick
  val jsonOne_cdBranch = jsonOne.transform(t_cdBranch).get

  val t_ceBranch = (__ \ "c" \ "e").json.pick
  val jsonOne_ceBranch = jsonOne.transform(t_ceBranch).get



}

