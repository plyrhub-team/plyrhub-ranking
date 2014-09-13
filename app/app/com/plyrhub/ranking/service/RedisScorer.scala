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

import akka.actor.{Actor, ActorLogging, Props}
import com.plyrhub.core.protocol.{ServiceSuccess, ServiceFailure}

import scala.concurrent.ExecutionContext.Implicits._

object RedisScorer {

  case class Score(owner: String, member: String, rankings: Seq[String], score: Int, opId: String)

  def props(): Props = Props(classOf[RedisScorer])

}

import com.plyrhub.ranking.service.RedisScorer._

class RedisScorer extends Actor with ActorLogging {
  override def receive = {

    case score: Score => storeScoreForRanking(score)

  }

  // Redis Scorer
  //  -> score
  //  -> update Mongo (if fail ->) --->> doesn't matter we can reconstruct the ranking for the user (we have the data)
  //                               --->> done through the recollector (looks for the non-confirmed operations and reconstrcut for that user)

  def storeScoreForRanking(score: Score) = {

    val owner = score.owner
    val member = score.member
    val opId = score.opId

    score.rankings.map(r => {

      // Annotate Score
      RankingRepo
        .annotateScore(score.owner, score.member, r, score.score)
        .map(result => {
        result.fold(manageRedisFailure, s => manageRedisSuccess(owner, member, r, opId))
      })

    })
  }


  def manageRedisFailure(f: ServiceFailure) = {

    // log the error
    // Wait a little and repeat
    log.error("Error annotating to redis")

  }

  def manageRedisSuccess(owner: String, member: String, ranking: String, opId: String) = {

    // It was annotated, so tell Mongo it was done to pull it out from the reconstruction process
    RankingRepo
      .commitScoreForRanking(owner, member, ranking, opId)
      .map(result =>
      result.fold(manageMongoFailure, manageMongoSuccess))
  }

  def manageMongoFailure(f:ServiceFailure) = {
    log.error("Failure")
  }

  def manageMongoSuccess(s:ServiceSuccess) = {
    //log.error("Success")
  }

}
