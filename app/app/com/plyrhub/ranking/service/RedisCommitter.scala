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

import java.util.concurrent.{Executors, ThreadPoolExecutor, ExecutorService}

import akka.actor.{Actor, ActorLogging, Props}
import com.plyrhub.core.protocol.{ServiceSuccess, ServiceFailure}

import scala.concurrent.ExecutionContext

object RedisCommitter {

  case class Commit(owner: String, member: String, rankings: String, opId: String)

  def props(): Props = Props(classOf[RedisCommitter])

  implicit val newExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(2))

}

import com.plyrhub.ranking.service.RedisCommitter._

class RedisCommitter extends Actor with ActorLogging {

  override def receive = {

    case commit: Commit => commitScoreForRanking(commit)

  }

  def commitScoreForRanking(commit: Commit) = {

    val owner = commit.owner
    val member = commit.member
    val opId = commit.opId



      // It was annotated, so tell Mongo it was done to pull it out from the reconstruction process
      RankingRepo
        .commitScoreForRanking(owner, member, commit.rankings, opId)
        .map(result =>
        result.fold(manageMongoFailure, manageMongoSuccess))
  }

  def manageMongoFailure(f:ServiceFailure) = {
    //log.error("Failure")
  }

  def manageMongoSuccess(s:ServiceSuccess) = {
    //log.error("Success")
  }

}
