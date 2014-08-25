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
import com.plyrhub.ranking.conf.RankingConfig.ModelConstraints._
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

object MongoSchema {

  val keyMaker = (owner: String, ranking: String) => s"ow:$owner:rnk:$ranking"

  val RANKING_KEY = keyMaker

  val COLLECTIONS = "owner_cols"
  val RANKINGS = "owner_rankings"

  val MEMBERS = "owner_members"
}

import MongoSchema._

// MongoRankingCollections
// _id -> owner + rnk
case class MongoOwnerCollections(_id: String, collections: Seq[String])

object MongoOwnerCollections {

  def build(owner: String, collections: Seq[RankingCollection]) = {

    val mongoCols = collections.map(_.collection)

    MongoOwnerCollections(key(owner), mongoCols)
  }

  def key(owner: String) = keyMaker(owner, "col")

  // Serializers
  implicit val mongoOwnerCollectionsFormat: Format[MongoOwnerCollections] = Json.format[MongoOwnerCollections]
}

// MongoRanking
// _id -> owner + rnk
case class MongoRanking(_id: String, ranking:String, collections: Option[Seq[String]], platforms: Option[Seq[RankingPlatform]], properties: Option[Seq[RankingProp]], state: Option[State], opId: Option[String])

object MongoRanking {

  def build(owner: String, ranking: String, data: Ranking, opId: String) = {

    MongoRanking(
      keyMaker(owner, ranking),
      ranking,
      Some(data.collections.map(_.collection)), Some(data.platforms),
      data.properties,
      Some(data.status),
      Some(opId))

  }

  def key(owner: String, ranking: String) = keyMaker(owner, ranking)

  // Serializer
  implicit val mongoRankingWrites: Writes[MongoRanking] = Json.writes[MongoRanking]

  implicit val mongoRankingReads: Reads[MongoRanking] = (
    (__ \ "_id").read[String] and
      (__ \ "ranking").read[String] and
      (__ \ "collections").readNullable[Seq[String]] and
      (__ \ "platforms").readNullable[Seq[RankingPlatform]] and
      (__ \ "properties").readNullable[Seq[RankingProp]] and
      (__ \ "status").readNullable[State] and
      (__ \ "opId").readNullable[String]
    )(MongoRanking.apply _)
}

// MongoMember
// id ->> owner + member
case class MongoMember(_id: String, rankings:Option[Seq[String]], opId:Option[String])

object MongoMember {

  def build(owner: String, member: String, rankings:Seq[String], opId: String) = {
    MongoMember(keyMaker(owner, member), Some(rankings), Some(opId))
  }

  // Serializers
  implicit val mongoMemberWrites: Writes[MongoMember] = Json.writes[MongoMember]

  implicit val mongoMemberReads: Reads[MongoMember] = (
    (__ \ "_id").read[String] and
      (__ \ "rankings").readNullable[Seq[String]] and
      (__ \ "opId").readNullable[String]
    )(MongoMember.apply _)
}




