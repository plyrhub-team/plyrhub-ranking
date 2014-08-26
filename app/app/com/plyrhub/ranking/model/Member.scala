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

import com.plyrhub.ranking.front.conf.RankingConfig.ModelConstraints._
import play.api.data.validation.ValidationError
import play.api.libs.json._

import scala.util.{Failure, Success, Try}

case class MemberRankings(rankings: Seq[String])

object MemberRankings {

  implicit object memberRankingsReads extends Reads[MemberRankings] {
    override def reads(json: JsValue) = {

      Try {
        val rankings = (json \ "rankings").as[Seq[String]]

        if (rankings.length < memberRankingsMin || rankings.length > memberRankingsMax)
          throw new IllegalArgumentException("No valid field length")

        rankings

      } match {
        case Success(s) => JsSuccess(MemberRankings(s))
        case Failure(e: IllegalArgumentException) => JsError(Seq(JsPath() -> Seq(ValidationError("plyrhub.member.error.non.valid.rankings.length"))))
        case Failure(thrown) =>
          JsError(Seq(JsPath() -> Seq(ValidationError("plyrhub.member.error.non.valid.rankings"))))
      }

    }

  }

  implicit val memberRankingsWrites = Json.writes[MemberRankings]
}

case class MemberScore(score:Int, rankings:MemberRankings)

object MemberScore {

  implicit val memberScoreReads: Reads[MemberScore] = Json.reads[MemberScore]
  implicit val memberScoreWrites = Json.writes[MemberScore]
}
