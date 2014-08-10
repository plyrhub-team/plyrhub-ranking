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

import com.plyrhub.api.model.State
import play.api.data.validation.ValidationError
import play.api.libs.json._

import scala.util.{Try, Failure, Success}

// JSON library
import play.api.libs.json.Reads._ // Custom validation helpers
import play.api.libs.functional.syntax._ // Combinator syntax


case class RankingCollection(collection: String)
object RankingCollection {

  // Implicit Json-Inception
  implicit val collectionFormat = Json.format[RankingCollection]
}

case class RankingName(lang: String, shortName: String, longName: String, default: Boolean, status: State)
object RankingName {

  // Serialization with combinators
  implicit val rankingNameReads:Reads[RankingName] = (
    (__ \ "lang").read[String] and
      (__ \ "shortName").read[String] and
      (__ \ "longName").read[String] and
      (__ \ "default").read[Boolean] and
      (__ \ "status").read[State]
    )(RankingName.apply _)

  implicit val rankingNameWrites = Json.writes[RankingName]
}

case class RankingPlatform(platform: String, names: Seq[RankingName], default: Boolean, status: State)
object RankingPlatform {

  // Serialization with combinators
  implicit val rankingPlatformReads:Reads[RankingPlatform] = (
    (__ \ "platform").read[String] and
      (__ \ "names").read[Seq[RankingName]] and
      (__ \ "default").read[Boolean] and
      (__ \ "status").read[State]
    )(RankingPlatform.apply _)

  implicit val rankingPlatformWrites = Json.writes[RankingPlatform]
}


sealed abstract class RankingProp(val prop: String, val value: String = "")

// Empty prop
case class RankingPropNoop() extends RankingProp("noop")

// Time properties
abstract class RankingPropIsTime(private val whatTime:String ) extends RankingProp("time", whatTime)
case class RankingPropIsEternal() extends RankingPropIsTime("eternal")
case class RankingPropIsDaily() extends RankingPropIsTime("daily")
case class RankingPropIsWeekly() extends RankingPropIsTime("weekly")
case class RankingPropIsMonthly() extends RankingPropIsTime("monthly")
case class RankingPropIsAnnual() extends RankingPropIsTime("annual")

object RankingProp {

  private val noop = RankingPropNoop()
  private val isEternal = RankingPropIsEternal()
  private val isDaily = RankingPropIsDaily()
  private val isWeekly = RankingPropIsWeekly()
  private val isMonthly = RankingPropIsMonthly()
  private val isAnnual = RankingPropIsAnnual()

  private val mapTimeProps = Map(
    isEternal.value -> isEternal,
    isDaily.value -> isDaily,
    isWeekly.value -> isWeekly,
    isMonthly.value -> isMonthly,
    isAnnual.value -> isAnnual
  )

  def apply(prop:String, value:String):Option[RankingProp] = {

    for(p <- Option(prop);
        v <- Option(value);
        theProp <- resolveProp(p, v))
      yield theProp
  }

  private def resolveProp(prop:String, value:String) = {
    mapTimeProps.get(Option(value).getOrElse("no-key").trim)
  }

  def unapply(rp: RankingProp) = {
    Some(rp.prop, rp.value)
  }

  // Serialization with converters
  implicit object RankingPropReads extends Reads[RankingProp] {
    override def reads(json: JsValue) = {

      Try{

        ((json \ "prop").as[String], (json \ "value").as[String])

      } match {
        case Success((p,v)) => {
          RankingProp(p, v) match {
            case Some(rp) => JsSuccess(rp)
            case _ => JsError(Seq(JsPath() -> Seq(ValidationError("plyrhub.error.non.valid.property"))))
          }
        }
        case Failure(thrown) =>  JsError(Seq(JsPath() -> Seq(ValidationError("plyrhub.error.non.valid.property"))))
      }

    }
  }

  implicit val rankingPropWrites = Json.writes[RankingProp]

}

case class Ranking(collections: Seq[RankingCollection], platforms: Seq[RankingPlatform], properties: Seq[RankingProp], status: State)

object Ranking {

  implicit val rankingFormat = Json.format[Ranking]

}
