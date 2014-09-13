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

import com.plyrhub.core.PlyrhubRT
import com.plyrhub.ranking.service.RedisCommitter.Commit
import com.plyrhub.ranking.service.RedisScorer.Score
import com.plyrhub.ranking.service.gc.MisterWolf
import com.plyrhub.ranking.service.gc.MisterWolf.FixMeMisterWorlf

object RankingServiceRT {

  lazy val misterWolf = PlyrhubRT.actorSystem.actorOf(MisterWolf.props(), "mister-wolf")

  def fixme(fxm: FixMeMisterWorlf) = misterWolf ! fxm

  lazy val redisScorer = PlyrhubRT.actorSystem.actorOf(RedisScorer.props(), "redis-scorer")

  def score(score:Score) = redisScorer ! score

  lazy val redisCommitter = PlyrhubRT.actorSystem.actorOf(RedisCommitter.props(), "redis-committer")

  def commit(commit:Commit) = redisCommitter ! commit

}
