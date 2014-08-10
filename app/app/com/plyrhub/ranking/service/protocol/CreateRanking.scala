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

package com.plyrhub.ranking.service.protocol

import com.plyrhub.api.model.State
import com.plyrhub.core.protocol.{ServiceSuccess, ServiceMessage}
import com.plyrhub.ranking.model.RankingName
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

case class CreateRanking(ranking: String, fromTop: Int, fromBottom: Int, name: RankingName) extends ServiceMessage

object CreateRanking {

  // Serialization with combinators
  implicit val createRankingReads: Reads[CreateRanking] = (
    (__ \ "ranking").read[String] and
      (__ \ "fromTop").read[Int] and
      (__ \ "fromBottom").read[Int] and
      (__ \ "name").read[RankingName]
    )(CreateRanking.apply _)
}

case class CreateRanking2() extends ServiceMessage



case class RankingCreated(rn:RankingName) extends ServiceSuccess


