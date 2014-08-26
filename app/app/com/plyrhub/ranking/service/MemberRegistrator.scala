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

package com.plyrhub.ranking.service

import akka.actor.{Actor, ActorLogging, ActorRef}
import com.plyrhub.core.context.{OperationContext, Owner}
import com.plyrhub.core.protocol._
import com.plyrhub.core.utils.Misc
import com.plyrhub.ranking.front.conf.RankingConfig.ModelConstraints
import com.plyrhub.ranking.model.MemberRankings
import com.plyrhub.ranking.service.gc.MisterWolf.FixMemberRegistration
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

import scala.concurrent.Future

// TODO: Review Implicits

import scala.concurrent.ExecutionContext.Implicits.global

// TODO: review implicits

object MemberRegistrator {

  case class MemberRegistrationMsg(member: String, data: MemberRankings) extends ServiceMessage

  object MemberRegistrationMsg {

    // Serialization with combinators
    implicit val memberRegistrationMsgReads: Reads[MemberRegistrationMsg] = (
      (__ \ "member").read[String]
        (minLength[String](ModelConstraints.memberIdMinLength) keepAnd maxLength[String](ModelConstraints.memberIdMaxLength)) and
        (__ \ "data").read[MemberRankings]
      )(MemberRegistrationMsg.apply _)
  }

  case class MemberRegistered(member: String) extends ServiceSuccess

  case class MemberAlreadyExist(member: String) extends ServiceSuccess

  case class MemberRegisteredInRankings(member: String) extends ServiceSuccess

  case class MemberNonValidRankings(member: String, nonValidRankings: Seq[String]) extends ServiceSuccess

  case class MemberGenericError(member: String, cause: String) extends ServiceSuccess

  case class ExistingRankingsForMember(rankings: Seq[String]) extends ServiceSuccess

}


class MemberRegistrator extends Actor with ActorLogging {

  import com.plyrhub.ranking.service.MemberRegistrator._

  override def receive = {

    case StartOperation(ctx: OperationContext, message: MemberRegistrationMsg) =>

      startOperation(sender(), ctx, message)

  }

  def startOperation(sender: ActorRef, ctx: OperationContext, message: MemberRegistrationMsg) = {

    val uniqueRepoId = Misc.uniqueID
    val owner = Owner(ctx.owner).get
    val member = message.member
    val rankings = message.data.rankings

    // RegisterMember
    val fRegisterMember = RankingRepo.registerMember(owner, member, rankings, uniqueRepoId)

    // Look for the provided rankings to see in they exist
    val fFindRankings = RankingRepo.findRankingsForMember(owner, member, rankings, uniqueRepoId)

    // Check if there were errors
    // Check if the rankings were in the DB
    // - if FAIL -> notify misterWolf and send back to user with failure
    // - if SUCCESS -> send back to user with success
    def fResult(memberRegistrationResult: ServiceSuccess, rankingsSearchResult: ServiceSuccess) = Future {

      (memberRegistrationResult, rankingsSearchResult) match {

        case (memberRegistered@MemberRegistered(_), ExistingRankingsForMember(rankingsFound)) => {

          val rankingsNotFound = rankings.diff(rankingsFound)
          if (rankingsNotFound.size != 0) {
            // Notify MisterWolf
            RankingServiceRT.fixme(FixMemberRegistration(owner, member, message.data, uniqueRepoId))

            MemberNonValidRankings(member, rankingsNotFound)
          } else
          // SUCCESS
            memberRegistered
        }

        case (mrr, rrr) => {

          // Notify MisterWolf
          RankingServiceRT.fixme(FixMemberRegistration(owner, member, message.data, uniqueRepoId))

          // Identify the type of error to return
          (mrr, rrr) match {
            case (memberAlreadyRegistered@MemberAlreadyExist(_), _) =>
              memberAlreadyRegistered
            case (MemberGenericError(_, cause), _) =>
              SimpleFailure(cause)
            case (MemberRegistered(_), MemberGenericError(_, cause)) =>
              SimpleFailure(cause)
            case (err1, err2) =>
              // This should not happen
              // TODO: log the types
              SimpleFailure("non expected error")
          }
        }
      }
    }

    // Combine Futures
    val fServiceResult = for {
      memberRegistrationResult <- fRegisterMember
      rankingsSearchResult <- fFindRankings
      result <- fResult(memberRegistrationResult, rankingsSearchResult)
    } yield result


    // Return
    fServiceResult.map(Complete(sender, _))

  }
}
