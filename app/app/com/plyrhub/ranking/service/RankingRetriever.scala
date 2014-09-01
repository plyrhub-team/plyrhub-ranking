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
import com.plyrhub.ranking.front.conf.RankingConfig.{ParametersQSConstraints, ModelConstraints}
import com.plyrhub.ranking.model._
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import scala.concurrent.Future

import scala.concurrent.ExecutionContext.Implicits._

object RankingRetriever {

  case class RankingQueryMsg(ranking: String, member: String, fromTop: Int, fromBottom: Int, platform: Option[String]) extends ServiceMessage

  object RankingQueryMsg {

    // Serialization with combinators
    implicit val rankingQueryMsgReads: Reads[RankingQueryMsg] = (
      (__ \ "ranking").read[String]
        (minLength[String](ModelConstraints.rnkIdMinLength) keepAnd maxLength[String](ModelConstraints.rnkIdMaxLength)) and
        (__ \ "member").read[String]
          (minLength[String](ModelConstraints.memberIdMinLength) keepAnd maxLength[String](ModelConstraints.memberIdMaxLength)) and
        (__ \ "fromTop").read[Int]
          (min[Int](ParametersQSConstraints.fromTopMin) keepAnd max[Int](ParametersQSConstraints.fromTopMax)) and
        (__ \ "fromBottom").read[Int]
          (min[Int](ParametersQSConstraints.fromBottomMin) keepAnd max[Int](ParametersQSConstraints.fromBottomMax)) and
        (__ \ "platform").readNullable[String]
          (minLength[String](ModelConstraints.rnkPlatformIdMin) keepAnd maxLength[String](ModelConstraints.rnkPlatformIdMax))
      )(RankingQueryMsg.apply _)
  }

  case class RankingsQueryResult(membersInRanking: List[MemberInRanking]) extends ServiceSuccess

}

class RankingRetriever extends Actor with ActorLogging {

  import com.plyrhub.ranking.service.RankingRetriever._

  override def receive = {

    case StartOperation(ctx: OperationContext, message: RankingQueryMsg) =>

      startOperation(sender(), ctx, message)
  }

  def startOperation(sender: ActorRef, ctx: OperationContext, message: RankingQueryMsg) = {

    val owner = Owner(ctx.owner).get
    val memberQ = message.member
    val rankingQ = message.ranking
    val fromTopQ = message.fromTop
    val fromBottomQ = message.fromBottom


    // Data from Redis -> info about positions of members in ranking
    val fTopMembers = RankingRepo.retrieveRankingTopMembers(owner, rankingQ, fromTopQ)
    val fBottomMembers = RankingRepo.retrieveRankingBottomMembers(owner, rankingQ, fromBottomQ)
    val fCardinality = RankingRepo.retrieveRankingCardinality(owner, rankingQ)
    val fMemberPosition = RankingRepo.retrieveMemberPosition(owner, rankingQ, memberQ)
    val fMemberScore = RankingRepo.retrieveMemberScore(owner, rankingQ, memberQ)

    // Data from Mongo -> info about the ranking (names...)


    def fResult(
                 topMembers: List[(String, Double)],
                 bottomMembers: List[(String, Double)],
                 cardinality: Long,
                 memberPosition: Option[Long],
                 memberScore: Option[Double]) = Future {

      val buildMember: (String, Double) = memberScore.fold((memberQ, 0D))(s => (memberQ, s))
      val mbrsFromDB = (topMembers.filter(_._1 != memberQ) ::: List(buildMember) ::: bottomMembers.filter(_._1 != memberQ)).sortBy(_._2).distinct

      val zipHelper =
        if ((fromTopQ + fromBottomQ) < mbrsFromDB.length) {

          def interpolateMemberPosition: (Int, PlaceInRanking) =
            (memberPosition.fold(fromTopQ + 1)(_.toInt), PlaceMember())

          (getTopRange(fromTopQ) :+ interpolateMemberPosition) ++ getBottomRange(fromBottomQ, cardinality.toInt)
        }
        else {
          val half = mbrsFromDB.length / 2
          val zones = if ((mbrsFromDB.length % 2) == 0) (half, half) else (half + 1, half)

          getTopRange(zones._1) ++ getBottomRange(zones._2, mbrsFromDB.length)
        }

      // (member, score),(position, placeInRanking)
      def toMember(m: ((String, Double), (Int, PlaceInRanking))) = MemberInRanking(m._1._1, m._1._2.toInt, m._2._1, m._2._2)

      RankingsQueryResult(mbrsFromDB.zip(zipHelper).map(toMember))
    }

    val fServiceResult = for {
      topMembers <- fTopMembers
      bottomMembers <- fBottomMembers
      totalMembers <- fCardinality
      memberPosition <- fMemberPosition
      memberScore <- fMemberScore
      result <- fResult(topMembers, bottomMembers, totalMembers, memberPosition, memberScore)
    } yield result

    // Return
    fServiceResult
      .map(Complete(sender, _))
      .recover {
      case _ => Complete(sender, SimpleFailure("not.defined"))
    }

  }

  private def getTopRange(size: Int) = {
    Range(1, size + 1).map((_, PlaceTop()))
  }

  private def getBottomRange(size: Int, cardinality: Int) = {
    Range(cardinality - size + 1, cardinality + 1).map((_, PlaceBottom()))
  }

}

