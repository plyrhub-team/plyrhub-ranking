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

package com.plyrhub.ranking.service.protocol

import com.plyrhub.core.protocol.{ServiceMessage, ServiceSuccess}
import com.plyrhub.ranking.conf.RankingConfig.ModelConstraints
import com.plyrhub.ranking.model.Ranking
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._


case class CreateOrUpdateRankingMsg(rnk: String, data: Ranking) extends ServiceMessage

object CreateOrUpdateRankingMsg {

  // Serialization with combinators
  implicit val createOrUpdateRankingMsgReads: Reads[CreateOrUpdateRankingMsg] = (
    (__ \ "ranking").read[String]
      (minLength[String](ModelConstraints.rnkIdMinLength) keepAnd maxLength[String](ModelConstraints.rnkIdMaxLength)) and
      (__ \ "data").read[Ranking]
    )(CreateOrUpdateRankingMsg.apply _)
}


case class RankingCreated(rnk: String) extends ServiceSuccess

case class RankingUpdated(rnk: String) extends ServiceSuccess

case class RankingAlreadyWithMembers(rnk: String) extends ServiceSuccess