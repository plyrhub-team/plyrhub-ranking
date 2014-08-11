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

package com.plyrhub.ranking.service

import akka.actor.Actor
import com.plyrhub.api.model.StateActive
import com.plyrhub.core.context.OperationContext
import com.plyrhub.core.protocol.{SimpleFailure, Complete, StartOperation}
import com.plyrhub.ranking.model.RankingName
import com.plyrhub.ranking.service.protocol.{CreateRanking2, CreateRanking, RankingCreated}
import com.plyrhub.core.store.RedisStore

// TODO: review implicits
import play.api.libs.concurrent.Execution.Implicits._

class RankingCreator extends Actor {

  override def receive = {
    case StartOperation(ctx: OperationContext, message: CreateRanking) =>
      println("telling everything is done")

      val theSender = sender()

      val f = RedisStore.storePosition("ranking1", "m1", 9)
      f.onSuccess{
        case l => theSender ! Complete(RankingCreated(RankingName("es--rnk1", "ss", "ll", true, StateActive())))
      }
      f.onFailure{
        case t => theSender ! Complete(SimpleFailure())
      }

      //sender ! OperationCompleted(Right(SimpleSuccess()))
      //sender ! Complete(RankingCreated(RankingName("es", "ss", "ll", true, StateActive())))

    case StartOperation(ctx: OperationContext, message: CreateRanking2) =>
      println("telling everything is done")

      val theSender = sender()

      val f = RedisStore.storePosition("ranking2", "m2", 9)
      f.onSuccess{
        case l => theSender ! Complete(RankingCreated(RankingName("es--rnk2", "ss", "ll", true, StateActive())))
      }
      f.onFailure{
        case t => theSender ! Complete(SimpleFailure())
      }

      //sender ! OperationCompleted(Right(SimpleSuccess()))
      //sender ! Complete(RankingCreated(RankingName("es", "ss", "ll", true, StateActive())))
  }

}
