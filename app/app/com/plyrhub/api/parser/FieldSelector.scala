
package com.plyrhub.api.parser

import play.api.libs.json._

// FieldParser and FieldSelector are taken from netflix.FieldSelectorParser and adapted to work with JsValue objects.

object FieldSelectorExpr {

  case class Result2(objectMatches: Boolean, newValue: Option[JsValue])

  val NoMatch2 = Result2(objectMatches = false, None)
}

import FieldSelectorExpr._

sealed trait FieldSelectorExpr {

  def select(value: JsValue): Option[JsValue] = {
    _select(value) match {
      case Result2(true, result) => result
      case Result2(false, Some(_)) => None
      case NoMatch2 => Some(value)
    }
  }

  def _select(value: JsValue): Result2 = value match {

    case jsArray: JsArray => {

      val results = jsArray.value.map(checkValue).filter(r => {
        r.objectMatches && r.newValue.isDefined && r.newValue.get.asInstanceOf[JsObject].fields.size > 0
      })

      Result2(objectMatches = true, Some(JsArray(results.map(_.newValue.get))))
    }

    case v => checkValue(v)
  }

  def checkValue(value: JsValue): Result2
}

case object MatchAnyExpr$ extends FieldSelectorExpr {
  def checkValue(value: JsValue): Result2 = Result2(objectMatches = true, Some(value))
}

case class FixedExpr(matches: Boolean) extends FieldSelectorExpr {
  def checkValue(value: JsValue): Result2 = Result2(matches, Some(value))
}

case class KeySelectExpr(keys: Map[String, FieldSelectorExpr])
  extends FieldSelectorExpr {

  def checkValue(value: JsValue): Result2 = value match {
    case jsObject: JsObject => {

      val newMap = jsObject.fields
        .filter(t => keys.contains(t._1))
        .map(t =>
        t._1 -> keys(t._1.toString)._select(t._2)).toMap

      val matches = !newMap.values.exists(!_.objectMatches)
      val resultMap = newMap.filter(_._2.newValue.isDefined).map(t => {
        t._1 -> t._2.newValue.get.asInstanceOf[JsValue]
      }).toList
      Result2(matches, Some(JsObject(resultMap)))
    }
    case _ => NoMatch2
  }
}

private object JsValueExtractorHelper {

  def extractValue(value:JsValue) = value match {
    case JsString(s) => s
    case JsNumber(n) => n
    case JsBoolean(b) => b
  }
}
import JsValueExtractorHelper._

case class EqualExpr(desiredValue: Any) extends FieldSelectorExpr {
  def checkValue(value: JsValue): Result2 = {

    Result2(extractValue(value) == desiredValue, Some(value))
  }
}

case class NotEqualExpr(desiredValue: Any) extends FieldSelectorExpr {
  def checkValue(value: JsValue): Result2 = {
    Result2(extractValue(value) != desiredValue, Some(value))
  }
}

case class RegexExpr(regex: String, invert: Boolean) extends FieldSelectorExpr {

  import java.util.regex.Pattern

  private val pattern = Pattern.compile(regex)

  def checkValue(value: JsValue): Result2 = {
    val matches = pattern.matcher(extractValue(value).toString).find
    Result2(matches ^ invert, Some(value))
  }
}


