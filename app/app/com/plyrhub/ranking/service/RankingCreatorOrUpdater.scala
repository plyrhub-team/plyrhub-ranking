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

import akka.actor.{ActorRef, Actor}
import com.plyrhub.core.context.OperationContext
import com.plyrhub.core.protocol.{Complete, StartOperation}
import com.plyrhub.core.store.mongo.JSONCollection
import com.plyrhub.core.store.redis.RedisStore
import com.plyrhub.ranking.model.Ranking
import com.plyrhub.ranking.service.protocol.{CreateOrUpdateRankingMsg, RankingCreated}
import com.plyrhub.core.store.mongo.MongoStore

// TODO: Review Implicits
import scala.concurrent.ExecutionContext.Implicits.global

// TODO: review implicits

class RankingCreatorOrUpdater extends Actor {

  override def receive = {

    case StartOperation(ctx: OperationContext, message: CreateOrUpdateRankingMsg) =>

      startOperation(sender(), ctx, message)

  }

  def startOperation(sender: ActorRef, ctx: OperationContext, message: CreateOrUpdateRankingMsg) = {

    println("Ranking Creation Requets Received...")
    println(
      """
        |Actions to do:\n
        |\t Save the ranking to MongoDB
        |\t\t Check it is possible to create the ranking: the raning does not exist OR exists but has NO MEMBERS
        |\t Save the "score" to Redis
        |\t
      """.stripMargin)

    // Saving data to Mongo

    val db = MongoStore.db
    def collection: JSONCollection = db.collection[JSONCollection]("rankings")

    val ranking:Ranking = message.data

    collection.insert(ranking).map(lastError =>
      Complete(sender, RankingCreated(message.rnk))
    )

  }


}
