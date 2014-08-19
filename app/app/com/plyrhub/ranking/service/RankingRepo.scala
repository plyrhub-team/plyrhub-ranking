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

import com.plyrhub.core.protocol.ServiceSuccess
import com.plyrhub.core.store.mongo.{JSONCollection, MongoConfig}
import com.plyrhub.core.store.redis.RedisConfig
import com.plyrhub.ranking.model.{MongoRanking, MongoSchema, Ranking}
import com.plyrhub.ranking.service.protocol.{RankingAlreadyExist, RankingCreated}

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.{Future, Promise}

object RankingRepo extends MongoConfig with RedisConfig {

  def createRanking(owner: String, ranking: String, data: Ranking): Future[ServiceSuccess] = {

    def rankings: JSONCollection = mongoDB.collection[JSONCollection](MongoSchema.RANKINGS)

    val p = Promise[ServiceSuccess]
    val f =rankings
      .insert(MongoRanking.build(owner, ranking, data))
      .map(lastError => p.success(RankingCreated(ranking)))

    f.onFailure{
      case _ => p.success(RankingAlreadyExist(ranking))
    }

    p.future

  }

  def storePosition(ranking: String, member: String, score: Long): Future[Long] = {

    //import redis.dispatcher
    redis.zAdd(ranking, (member, score))

  }
}


