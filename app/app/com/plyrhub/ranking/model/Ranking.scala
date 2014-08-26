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

import com.plyrhub.api.model.State
import com.plyrhub.ranking.front.conf.RankingConfig.ModelConstraints
import play.api.data.validation.ValidationError

import scala.util.{Try, Failure, Success}

// JSON library

import play.api.libs.json._

// Custom validation helpers

import play.api.libs.json.Reads._

// Combinator syntax

import play.api.libs.functional.syntax._


import ModelConstraints._

case class RankingCollection(collection: String)

object RankingCollection {

  implicit object RankingCollectionReads extends Reads[RankingCollection] {
    override def reads(json: JsValue) = {

      Try {
        val collection = (json \ "collection").as[String]

        if (collection.length < rnkCollectionsMinLength || collection.length > rnkCollectionsMaxLength)
          throw new IllegalArgumentException("No valid field length")

        collection

      } match {
        case Success(s) => JsSuccess(RankingCollection(s))
        case Failure(e:IllegalArgumentException) => JsError(Seq(JsPath() -> Seq(ValidationError("plyrhub.error.non.valid.collection.length"))))
        case Failure(thrown) => JsError(Seq(JsPath() -> Seq(ValidationError("plyrhub.error.non.valid.collection"))))
      }

    }

  }

  implicit val rankingCollectionWrites = Json.writes[RankingCollection]
}

case class RankingName(lang: String, shortName: String, longName: String, default: Boolean, status: State)

object RankingName {

  // Serialization with combinators
  implicit val rankingNameReads: Reads[RankingName] = (
    (__ \ "lang").read[String]
      (minLength[String](rnkNameLangMinLength) keepAnd maxLength[String](rnkNameLangMaxLength)) and
      (__ \ "shortName").read[String]
        (minLength[String](rnkNameShortNameMinLength) keepAnd maxLength[String](rnkNameShortNameMaxLength)) and
      (__ \ "longName").read[String]
        (minLength[String](rnkNameLongNameMinLength) keepAnd maxLength[String](rnkNameLongNameMaxLength)) and
      (__ \ "default").read[Boolean] and
      (__ \ "status").read[State]
    )(RankingName.apply _)

  implicit val rankingNameWrites = Json.writes[RankingName]
}

case class RankingPlatform(platform: String, names: Seq[RankingName], default: Boolean, status: State)

object RankingPlatform {

  // Serialization with combinators
  implicit val rankingPlatformReads: Reads[RankingPlatform] = (
    (__ \ "platform").read[String]
      (minLength[String](rnkPlatformIdMin) keepAnd maxLength[String](rnkPlatformIdMax)) and
      (__ \ "names").read[Seq[RankingName]]
        (minLength[Seq[RankingName]](rnkPlatformNamesMin) keepAnd maxLength[Seq[RankingName]](rnkPlatformNamesMax)) and
      (__ \ "default").read[Boolean] and
      (__ \ "status").read[State]
    )(RankingPlatform.apply _)

  implicit val rankingPlatformWrites = Json.writes[RankingPlatform]
}


sealed abstract class RankingProp(val prop: String, val value: String = "")

// Empty prop
case class RankingPropNoop() extends RankingProp("noop")

// Unique per user
abstract class RankingPropIsScope(private val trueFalse: String) extends RankingProp("perUser", trueFalse)

case class RankingPropIsSameForAll() extends RankingPropIsScope("all")
case class RankingPropIsDifferentForEach() extends RankingPropIsScope("each")


// Time properties
abstract class RankingPropIsTime(private val whatTime: String) extends RankingProp("resetPeriod", whatTime)

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

  private val mapRankingProps = Map(
    // Time Props
    isEternal.value -> isEternal,
    isDaily.value -> isDaily,
    isWeekly.value -> isWeekly,
    isMonthly.value -> isMonthly,
    isAnnual.value -> isAnnual
  )

  def apply(prop: String, value: String): Option[RankingProp] = {

    for (p <- Option(prop);
         v <- Option(value);
         theProp <- resolveProp(p, v))
    yield theProp
  }

  private def resolveProp(prop: String, value: String) = {
    mapRankingProps.get(Option(value).getOrElse("no-key").trim)
  }

  def validProps = mapRankingProps.keys;

  def unapply(rp: RankingProp) = {
    Some(rp.prop, rp.value)
  }

  // Serialization with converters
  implicit object RankingPropReads extends Reads[RankingProp] {
    override def reads(json: JsValue) = {

      Try {

        ((json \ "prop").as[String], (json \ "value").as[String])

      } match {
        case Success((p, v)) => {
          RankingProp(p, v) match {
            case Some(rp) => JsSuccess(rp)
            case _ => JsError(Seq(JsPath() -> Seq(ValidationError("plyrhub.error.non.valid.property"))))
          }
        }
        case Failure(thrown) => JsError(Seq(JsPath() -> Seq(ValidationError("plyrhub.error.non.valid.property"))))
      }

    }
  }

  implicit val rankingPropWrites = Json.writes[RankingProp]

}

case class Ranking(collections: Seq[RankingCollection], platforms: Seq[RankingPlatform], properties: Option[Seq[RankingProp]], status: State)

object Ranking {

  implicit val rankingReads: Reads[Ranking] = (
    (__ \ "collections").read[Seq[RankingCollection]]
      (minLength[Seq[RankingCollection]](rnkColsMin) keepAnd maxLength[Seq[RankingCollection]](rnkColsMax)) and
      (__ \ "platforms").read[Seq[RankingPlatform]]
        (minLength[Seq[RankingPlatform]](rnkPlatformsMin) keepAnd maxLength[Seq[RankingPlatform]](rnkPlatformsMax)) and
      (__ \ "properties").readNullable[Seq[RankingProp]]
        (maxLength[Seq[RankingProp]](rnkPropertiesMax)) and
      (__ \ "status").read[State]
    )(Ranking.apply _)


  implicit val rankingWrites = Json.writes[Ranking]

}
