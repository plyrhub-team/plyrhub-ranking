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

import com.plyrhub.core.protocol.{ServiceFailure, ServiceSuccess, SimpleFailure, SimpleSuccess}
import com.plyrhub.core.store.mongo.{JSONCollection, MongoConfig}
import com.plyrhub.core.store.redis.RedisConfig
import com.plyrhub.ranking.model._
import com.plyrhub.ranking.service.MemberRegistrator._
import com.plyrhub.ranking.service.MemberScorer._
import com.plyrhub.ranking.service.RankingCreator._
import play.api.libs.iteratee.Enumerator
import play.api.libs.json._
import reactivemongo.core.errors.DatabaseException

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

// TODO: log non-business errors

object RankingRepo extends MongoConfig with RedisConfig {

  import com.plyrhub.ranking.model.MongoSchema._
  import com.plyrhub.ranking.model.RedisSchema._

  val RANKINGS_COLLECTION = (owner: String) => mongoDB.collection[JSONCollection](RANKINGS_COLLECTION_MAKER(owner))
  val MEMBERS_COLLECTION = (owner: String) => mongoDB.collection[JSONCollection](MEMBERS_COLLECTION_MAKER(owner))
  val SCORES_COLLECTION = (owner: String) => mongoDB.collection[JSONCollection](SCORES_COLLECTION_MAKER(owner))

  def createRanking(owner: String, ranking: String, data: Ranking, opId: String): Future[ServiceSuccess] = {

    def rankingsCol: JSONCollection = RANKINGS_COLLECTION(owner)

    val p = Promise[ServiceSuccess]
    val f = rankingsCol
      .insert(MongoRanking.build(ranking, data, opId))
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

    def membersCol: JSONCollection = MEMBERS_COLLECTION(owner)

    val p = Promise[ServiceSuccess]
    val f = membersCol
      .insert(MongoMember.build(member, rankings, opId))
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

    def rankingsCol: JSONCollection = RANKINGS_COLLECTION(owner)

    val p = Promise[ServiceSuccess]

    val selector = Json.obj("_id" -> Json.obj("$in" -> JsArray(Seq(rankings.map(r => JsString(r))).flatten)))
    val projection = Json.obj("_id" -> 1)

    val f =
      rankingsCol
        .find(selector, projection)
        .cursor[MongoRanking]
        .collect[Seq]()
        .map(lmr =>
        p.success(ExistingRankingsForMember(lmr.map(mr => mr._id))))

    f.onFailure {
      case x =>
        p.success(MemberGenericError(member, x.getMessage))
    }

    p.future

  }

  def saveScoreForRankings(owner: String, member: String, rankings: Seq[String], score: Int, opId: String) = {

    def scoresCol: JSONCollection = SCORES_COLLECTION(owner)

    val p = Promise[ServiceSuccess]

    val scores = rankings.map(r => MongoScore.build(member, r, score, false, opId))

    val f = scoresCol
      .bulkInsert(Enumerator.enumerate(scores))
      .map(inserted => if (inserted == rankings.length) p.success(SimpleSuccess()) else p.success(ScoreRegistrationForSomeRankingsFailed()))

    f.onFailure {
      case x =>
        p.success(ScoreGenericError(member, x.getMessage))
    }

    p.future
  }

  def commitScoreForRanking(owner: String, member: String, ranking: String, opId: String) = {

    def scoresCol: JSONCollection = SCORES_COLLECTION(owner)

    val p = Promise[Either[ServiceFailure, ServiceSuccess]]

    val selector = Json.obj(
      "member" -> member,
      "ranking" -> ranking,
      "opId" -> opId
    )

    val update = Json.obj(
      "$set" -> Json.obj("confirmed" -> true)
    )

    val f = scoresCol
      .update(selector, update)

    f.onSuccess {
      case lastError =>
        if (lastError.n != 1)
        // This should not happen -> log and stop, the reconstruction proccess will solve it
        // Maybe call MisterWolf -> think about
          p.success(Right(SimpleSuccess()))
        else
          p.success(Right(SimpleSuccess()))

    }

    f.onFailure {
      case x =>
        p.success(Left(SimpleFailure(x.getMessage)))
    }

    p.future
  }


  def verifyRankingsOnMember(owner: String, member: String, rankings: Seq[String]) = {

    def membersCol: JSONCollection = MEMBERS_COLLECTION(owner)

    val p = Promise[ServiceSuccess]

    val selector = Json.obj(
      "_id" -> member,
      "rankings" -> Json.obj("$all" -> JsArray(Seq(rankings.map(r => JsString(r))).flatten))
    )
    val projection = Json.obj("_id" -> 1)

    val f =
      membersCol
        .find(selector, projection)
        .cursor[MongoMember]
        .collect[Seq]()

    f.onSuccess {
      case m => if (m.length > 0) p.success(SimpleSuccess()) else p.success(SomeRankingsNotRegisteredWithMember())
    }

    f.onFailure {
      case x =>
        p.success(ScoreGenericError(member, x.getMessage))
    }

    p.future
  }

  def annotateScore(owner: String, member: String, ranking: String, score: Int) = {

    val p = Promise[Either[ServiceFailure, ServiceSuccess]]

    redisDB
      .zIncrBy(RANKING_ID(owner, ranking), member, score)
      .onComplete {
      case Success(_) => p.success(Right(SimpleSuccess()))
      case Failure(x) =>
        p.success(Left(SimpleFailure(x.getMessage)))
    }

    p.future
  }

  def retrieveRankingTopMembers(owner: String, ranking: String, topSize: Int) = {

    redisDB
      .zRevRangeWithScores(RANKING_ID(owner, ranking), 0, topSize - 1)
      .map(_.iterator.map(i => (i._1.toString, i._2)).toList)
  }

  def retrieveRankingBottomMembers(owner: String, ranking: String, bottomSize: Int) = {

    redisDB
      .zRangeWithScores(RANKING_ID(owner, ranking), 0, bottomSize - 1)
      .map(_.iterator.map(i => (i._1.toString, i._2)).toList.reverse)
  }

  def retrieveRankingCardinality(owner:String, ranking:String) = {

    redisDB.zCard(RANKING_ID(owner, ranking))
  }

  def retrieveMemberPosition(owner:String, ranking:String, member:String) = {

    redisDB.zRevRank(RANKING_ID(owner, ranking), member)
  }

  def retrieveMemberScore(owner:String, ranking:String, member:String) = {

    redisDB.zScore(RANKING_ID(owner, ranking), member)
  }

}


