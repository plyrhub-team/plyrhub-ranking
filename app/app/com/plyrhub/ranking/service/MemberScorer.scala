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
import com.plyrhub.ranking.model.MemberScore
import com.plyrhub.ranking.service.gc.MisterWolf.{FixMemberScoring, FixMemberRegistration}
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

import scala.concurrent.Future

// TODO: Review Implicits

import scala.concurrent.ExecutionContext.Implicits.global

// TODO: review implicits

object MemberScorer {

  case class MemberScoreMsg(member: String, data: MemberScore) extends ServiceMessage

  object MemberScoreMsg {

    // Serialization with combinators
    implicit val memberScoreMsgReads: Reads[MemberScoreMsg] = (
      (__ \ "member").read[String]
        (minLength[String](ModelConstraints.memberIdMinLength) keepAnd maxLength[String](ModelConstraints.memberIdMaxLength)) and
        (__ \ "data").read[MemberScore]
      )(MemberScoreMsg.apply _)
  }

  case class ScoreForSomeRankingsFailed() extends ServiceSuccess()

  case class ScoreForSomeRankingsNotFoundInMember(rankings: Seq[String]) extends ServiceSuccess()

  case class ScoreGenericError(member: String, cause: String) extends ServiceSuccess

}

class MemberScorer extends Actor with ActorLogging {

  import MemberScorer._

  override def receive = {

    case StartOperation(ctx: OperationContext, message: MemberScoreMsg) =>

      startOperation(sender(), ctx, message)

  }

  def startOperation(sender: ActorRef, ctx: OperationContext, message: MemberScoreMsg) = {

    val uniqueRepoId = Misc.uniqueID
    val owner = Owner(ctx.owner).get
    val member = message.member
    val score = message.data.score
    val rankings = message.data.rankings.rankings

    // save to mongo (with indicator of opId + id (Incremental)
    // verify rankings
    // Ok ->>> return
    // Ok ->>> sent to Redis Scorer
    // Fail ->>> Send to MisterWolf

    // Save score for rankings
    val fScoreForRankings = RankingRepo.saveScoreForRankings(owner, member, rankings, score, uniqueRepoId)

    // Look for the provided rankings to see in they exist
    val fRankingsVerification = RankingRepo.verifyRankingsOnMember(owner, member, rankings)

    def fResult(scoringResult: ServiceSuccess, verificationResult: ServiceSuccess) = Future {

      (scoringResult, verificationResult) match {

        case (SimpleSuccess(), SimpleSuccess()) => {
          // Tell redis to score
          RankingServiceRT.score(RedisScorer.Score(owner, member, rankings, score, uniqueRepoId))
          SimpleSuccess()
        }

        case (scoringMaybeError, verificactionMaybeError) => {

          // Notify MisterWolf
          RankingServiceRT.fixme(FixMemberScoring(owner, member, message.data, uniqueRepoId))

          // Identify the error
          (scoringMaybeError, verificactionMaybeError) match {
            case (ScoreForSomeRankingsFailed(), _) => scoringMaybeError
            case (ScoreGenericError(_, cause), _) => SimpleFailure(cause)
            case (_, ScoreForSomeRankingsNotFoundInMember(_)) => verificactionMaybeError
            case (_, ScoreGenericError(_, cause)) => SimpleFailure(cause)
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
      scoringResult <- fScoreForRankings
      verificationResult <- fRankingsVerification
      result <- fResult(scoringResult, verificationResult)
    } yield result

    // Return
    fServiceResult.map(Complete(sender, _))

  }

}
