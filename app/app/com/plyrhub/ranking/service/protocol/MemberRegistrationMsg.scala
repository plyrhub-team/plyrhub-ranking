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
import com.plyrhub.ranking.model.MemberRankings
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

case class MemberRegistrationMsg(member: String, data: MemberRankings) extends ServiceMessage

object MemberRegistrationMsg {

  // Serialization with combinators
  implicit val memberRegistrationMsgMsgReads: Reads[MemberRegistrationMsg] = (
    (__ \ "member").read[String]
      (minLength[String](ModelConstraints.memberIdMinLength) keepAnd maxLength[String](ModelConstraints.memberIdMaxLength)) and
      (__ \ "data").read[MemberRankings]
    )(MemberRegistrationMsg.apply _)
}

case class MemberRegistered(member: String) extends ServiceSuccess

case class MemberAlreadyExist(member: String) extends ServiceSuccess

case class MemberRegisteredInRankings(member:String) extends ServiceSuccess

case class MemberNonValidRankings(member: String, nonValidRankings:Seq[String]) extends ServiceSuccess

case class MemberGenericError(member:String, cause:String) extends ServiceSuccess

case class ExistingRankingsForMember(rankings:Seq[String]) extends ServiceSuccess