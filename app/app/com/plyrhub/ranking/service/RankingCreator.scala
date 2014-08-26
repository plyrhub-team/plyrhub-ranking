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
import com.plyrhub.ranking.model.Ranking
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

import scala.concurrent.Future

// TODO: Review Implicits

import scala.concurrent.ExecutionContext.Implicits.global

// TODO: review implicits

object RankingCreator {

  case class RankingCreationMsg(ranking: String, data: Ranking) extends ServiceMessage

  object RankingCreationMsg {

    // Serialization with combinators
    implicit val rankingCreationMsgReads: Reads[RankingCreationMsg] = (
      (__ \ "ranking").read[String]
        (minLength[String](ModelConstraints.rnkIdMinLength) keepAnd maxLength[String](ModelConstraints.rnkIdMaxLength)) and
        (__ \ "data").read[Ranking]
      )(RankingCreationMsg.apply _)
  }


  case class RankingCreated(rnk: String) extends ServiceSuccess

  case class RankingAlreadyExist(rnk: String) extends ServiceSuccess

  case class RankingGenericError(rnk: String, cause: String) extends ServiceSuccess

}


class RankingCreator extends Actor with ActorLogging {

  import com.plyrhub.ranking.service.RankingCreator._

  override def receive = {

    case StartOperation(ctx: OperationContext, message: RankingCreationMsg) =>

      startOperation(sender(), ctx, message)

  }

  def startOperation(sender: ActorRef, ctx: OperationContext, message: RankingCreationMsg) = {

    /*
        - create ranking in MongoDB
        - If fail
          -> report error to client
        - If everything is OK
          -> report Success to client
     */

    // Saving data to Mongo
    val fCreateRanking = RankingRepo.createRanking(Owner(ctx.owner).get, message.ranking, message.data, Misc.uniqueID)

    def fResult(result: ServiceSuccess) = Future {
      result match {
        case r@(RankingCreated(_) | RankingAlreadyExist(_)) => r
        case RankingGenericError(ranking, cause) => SimpleFailure(cause)
      }
    }

    // Combine
    val fServiceResult = for {
      success <- fCreateRanking
      result <- fResult(success)
    } yield result

    // Return
    fServiceResult.map(Complete(sender, _))

  }
}
