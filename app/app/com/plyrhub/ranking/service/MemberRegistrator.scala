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

    val fVerifyRankings = RankingRepo.verifyRankingsForMember(owner, member, rankings, uniqueRepoId)

    def fRegisterMemberInRankings(memberRegistered: ServiceSuccess, existingRankings: ServiceSuccess): Future[ServiceSuccess] = {

      (memberRegistered, existingRankings) match {
        case (MemberRegistered(_), ExistingRankingsForMember(rankingsFound)) => {

          if (rankingsFound.length != rankings.length) {
            // There are rankings that do not exist -->> so we don't do the registration
            Future.successful(MemberNonValidRankings(member, rankings.diff(rankingsFound)))
          } else {
            // Register the rankings in Db
            Future.successful(MemberRegisteredInRankings(member))
          }
        }
        case (MemberRegistered(_), MemberGenericError(_, _)) => Future.successful(existingRankings)
        case _ => Future.successful(SimpleIntermediateStep())
      }
    }

    def fResult(memberRegistrationResult: ServiceSuccess, registrationInRankingsResult: ServiceSuccess) = Future {

      (memberRegistrationResult, registrationInRankingsResult) match {

        case (MemberRegistered(_), MemberRegisteredInRankings(_)) => memberRegistrationResult
        case (mrr, rrr) => {

          // Launch MisterWolf
          RankingServiceRT.misterWolf ! FixMemberRegistration(owner, member, message.data, uniqueRepoId)

          // Identify the type of error to return
          (mrr, rrr) match {
            case (MemberRegistered(_), _) => registrationInRankingsResult
            case (MemberAlreadyExist(_), _) => memberRegistrationResult
            case (MemberGenericError(_, _), _) => memberRegistrationResult
            case (_, _) => registrationInRankingsResult
          }
        }
      }

    }

    // Combine
    val fServiceResult = for {
      successMemberRegistered <- fRegisterMember
      existingRankings <- fVerifyRankings
      successRankingsForMember <- fRegisterMemberInRankings(successMemberRegistered, existingRankings)
      result <- fResult(successMemberRegistered, successRankingsForMember)
    } yield result


    // Return
    fServiceResult.map(Complete(sender, _))

  }
}
