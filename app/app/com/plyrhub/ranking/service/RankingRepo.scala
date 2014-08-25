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
import com.plyrhub.ranking.model.{MongoMember, MongoRanking, MongoSchema, Ranking}
import com.plyrhub.ranking.service.protocol._
import play.api.libs.json.{JsString, JsArray, Json}
import reactivemongo.core.errors.DatabaseException

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.{Future, Promise}

object RankingRepo extends MongoConfig with RedisConfig {

  import MongoSchema._

  def createRanking(owner: String, ranking: String, data: Ranking, opId: String): Future[ServiceSuccess] = {

    def rankingsCol: JSONCollection = mongoDB.collection[JSONCollection](RANKINGS)

    val p = Promise[ServiceSuccess]
    val f = rankingsCol
      .insert(MongoRanking.build(owner, ranking, data, opId))
      .map(lastError => p.success(RankingCreated(ranking)))

    f.onFailure {
      case de: DatabaseException if de.code.isDefined && de.code.get == 11000 => p.success(RankingAlreadyExist(ranking))
      case x => {
        p.success(RankingGenericError(ranking, x.getMessage))
      }
    }

    p.future

  }

  def registerMember(owner: String, member: String, rankings: Seq[String], opId: String): Future[ServiceSuccess] = {

    def membersCol: JSONCollection = mongoDB.collection[JSONCollection](MEMBERS)

    val p = Promise[ServiceSuccess]
    val f = membersCol
      .insert(MongoMember.build(owner, member, rankings, opId))
      .map(lastError => p.success(MemberRegistered(member)))

    f.onFailure {
      case de: DatabaseException if de.code.isDefined && de.code.get == 11000 => p.success(MemberAlreadyExist(member))
      case x => {
        p.success(MemberGenericError(member, x.getMessage))
      }
    }

    p.future

  }

  def findRankingsForMember(owner: String, member: String, rankings: Seq[String], opId: String): Future[ServiceSuccess] = {

    def rankingsCol: JSONCollection = mongoDB.collection[JSONCollection](RANKINGS)

    val p = Promise[ServiceSuccess]

    val selector = Json.obj("_id" ->  Json.obj("$in" -> JsArray(Seq(rankings.map(r => JsString(RANKING_KEY(owner, r)))).flatten)))
    val projection = Json.obj("ranking" -> 1)

    val f =
      rankingsCol
        .find(selector, projection)
        .cursor[MongoRanking]
        .collect[Seq]()
        .map(lmr => p.success(ExistingRankingsForMember(lmr.map(mr => mr.ranking))))

    f.onFailure {
      case x =>
        p.success(MemberGenericError(member, x.getMessage))
    }

    p.future

  }

  def storePosition(ranking: String, member: String, score: Long): Future[Long] = {

    //import redis.dispatcher
    redis.zAdd(ranking, (member, score))

  }
}


