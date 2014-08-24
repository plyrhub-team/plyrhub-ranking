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
import com.plyrhub.ranking.service.protocol.MisterWolfProtocol.FixMemberRegistration
import com.plyrhub.ranking.service.protocol._

import scala.concurrent.Future

// TODO: Review Implicits

import scala.concurrent.ExecutionContext.Implicits.global

// TODO: review implicits

class MemberRegistrator extends Actor with ActorLogging {

  override def receive = {

    case StartOperation(ctx: OperationContext, message: MemberRegistrationMsg) =>

      startOperation(sender(), ctx, message)

  }

  def startOperation(sender: ActorRef, ctx: OperationContext, message: MemberRegistrationMsg) = {

    /*
      Registrate a new member
        -> if already exist -> MemberAlreadyExists
      Verify if the "rankings" already exists
        -> if not return the list of non-existing rankings
     */

    val uniqueRepoId = Misc.uniqueID
    val owner = Owner(ctx.owner).get
    val member = message.member
    val rankings = message.data.rankings

    // Prepare operations
    val fRegisterMember = RankingRepo.registerMember(owner, member, uniqueRepoId)

    val fFindRankings = RankingRepo.findRankingsForMember(owner, member, rankings, uniqueRepoId)

    def fResult(memberRegistrationResult: ServiceSuccess, rankingsSearchResult: ServiceSuccess) = Future {

      (memberRegistrationResult, rankingsSearchResult) match {

        case (MemberRegistered(_), ExistingRankingsForMember(rankingsFound)) => {

          val rankingsNotFound = rankings.diff(rankingsFound)
          if (rankingsNotFound.size !=0) MemberNonValidRankings(member, rankingsNotFound) else memberRegistrationResult

        }

        case (mrr, rrr) => {

          // Launch MisterWolf
          RankingServiceRT.misterWolf ! FixMemberRegistration(owner, member, message.data, uniqueRepoId)

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

    // Combine
    val fServiceResult = for {
      memberRegistrationResult <- fRegisterMember
      rankingsSearchResult <- fFindRankings
      result <- fResult(memberRegistrationResult, rankingsSearchResult)
    } yield result


    // Return
    fServiceResult.map(Complete(sender, _))

  }
}
