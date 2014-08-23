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
import com.plyrhub.ranking.service.protocol.{CreateOrUpdateRankingMsg, RankingAlreadyExist, RankingCreated, RankingGenericError}

import scala.concurrent.Future

// TODO: Review Implicits

import scala.concurrent.ExecutionContext.Implicits.global

// TODO: review implicits

class RankingCreatorOrUpdater extends Actor with ActorLogging {

  override def receive = {

    case StartOperation(ctx: OperationContext, message: CreateOrUpdateRankingMsg) =>

      startOperation(sender(), ctx, message)

  }

  def startOperation(sender: ActorRef, ctx: OperationContext, message: CreateOrUpdateRankingMsg) = {

    // Saving data to Mongo
    val fCreateOnMongo = RankingRepo.createRanking(Owner(ctx.owner).get, message.rnk, message.data)
    val fStoreNoUser = RankingRepo.storePosition(message.rnk, "no-member", 0)

    def fResult(result: ServiceSuccess, elements: Long) = Future {
      result match {
        case r @ (RankingCreated(_) | RankingAlreadyExist(_)) => r  // send to MisterWolf !!!!
        case r @ RankingGenericError(ranking) => SimpleFailure()    // send to MisterWolf !!!!
        case _ => SimpleFailure()
      }

    }

    // Combine
    val fServiceResult = for {
      lastError <- fCreateOnMongo
      elements <- fStoreNoUser
      result <- fResult(lastError, elements)
    } yield result

    // Return
    fServiceResult.map(Complete(sender, _))

  }
}
