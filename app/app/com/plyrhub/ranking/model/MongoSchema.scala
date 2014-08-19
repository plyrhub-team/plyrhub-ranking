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

package com.plyrhub.ranking.model

import com.plyrhub.api.model.State
import play.api.libs.json.{Format, Json}

object MongoSchema {

  val keyMaker = (owner: String, objectId: String) => s"ow::$owner::id::$objectId"

  val COLLECTIONS = "owner_cols"
  val RANKINGS = "owner_rankings"

}

import MongoSchema._

// MongoRankingCollections
// _id -> owner
case class MongoOwnerCollections(_id:String, collections:Seq[String])
object MongoOwnerCollections{

  def build(owner:String, collections:Seq[RankingCollection]) = {

    val mongoCols = collections.map(_.collection)

    MongoOwnerCollections(key(owner), mongoCols)
  }

  def key(owner:String) = keyMaker(owner, "col")

  // Serializers
  implicit val mongoOwnerCollections:Format[MongoOwnerCollections] = Json.format[MongoOwnerCollections]
}

// MongoRanking
// _id -> owner + rnk
case class MongoRanking(_id:String, collections: Seq[String], platforms: Seq[RankingPlatform], properties: Option[Seq[RankingProp]], state: State)

object MongoRanking {

  def build(owner:String, ranking:String, data:Ranking) = {

    MongoRanking(keyMaker(owner, ranking), data.collections.map(_.collection) , data.platforms, data.properties, data.status)

  }

  def key(owner:String, ranking:String) = keyMaker(owner, ranking)

  // Serializer
  implicit val mongoRanking:Format[MongoRanking] = Json.format[MongoRanking]
}





