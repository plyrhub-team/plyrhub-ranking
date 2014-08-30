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
import com.plyrhub.ranking.front.conf.RankingConfig.ModelConstraints._
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

object MongoSchema {

  private[this] val keyMakerSimple = (owner: String, element:String, value: String) => s"ow:$owner:$element:$value"

  val COLLECTIONS_KEY = keyMakerSimple(_:String,"col", _:String)
  val SCORE_KEY = keyMakerSimple(_:String, "scr", _:String)

  private[this] val collectionKeyMaker = (owner: String, collection:String) => s"${owner}_${collection}"

  val RANKINGS_COLLECTION_MAKER = collectionKeyMaker(_:String, "rankings")
  val MEMBERS_COLLECTION_MAKER = collectionKeyMaker(_:String, "members")
  val SCORES_COLLECTION_MAKER = collectionKeyMaker(_:String, "scores")

  val COLLECTIONS = "owner_cols"

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

  def key(owner: String) = COLLECTIONS_KEY(owner, "col")

  // Serializers
  implicit val mongoOwnerCollectionsFormat: Format[MongoOwnerCollections] = Json.format[MongoOwnerCollections]
}

// MongoRanking
// _id -> ranking
case class MongoRanking(_id: String, collections: Option[Seq[String]], platforms: Option[Seq[RankingPlatform]], properties: Option[Seq[RankingProp]], state: Option[State], opId: Option[String])

object MongoRanking {

  def build(ranking: String, data: Ranking, opId: String) = {

    MongoRanking(
      ranking,
      Some(data.collections.map(_.collection)), Some(data.platforms),
      data.properties,
      Some(data.status),
      Some(opId))
  }

  // Serializer
  implicit val mongoRankingWrites: Writes[MongoRanking] = Json.writes[MongoRanking]

  implicit val mongoRankingReads: Reads[MongoRanking] = (
    (__ \ "_id").read[String] and
      (__ \ "collections").readNullable[Seq[String]] and
      (__ \ "platforms").readNullable[Seq[RankingPlatform]] and
      (__ \ "properties").readNullable[Seq[RankingProp]] and
      (__ \ "status").readNullable[State] and
      (__ \ "opId").readNullable[String]
    )(MongoRanking.apply _)
}

// MongoMember
// id ->> member
case class MongoMember(_id: String, rankings: Option[Seq[String]], opId: Option[String])

object MongoMember {

  def build(member: String, rankings: Seq[String], opId: String) = {
    MongoMember(member, Some(rankings), Some(opId))
  }

  // Serializers
  implicit val mongoMemberWrites: Writes[MongoMember] = Json.writes[MongoMember]

  implicit val mongoMemberReads: Reads[MongoMember] = (
    (__ \ "_id").read[String] and
      (__ \ "rankings").readNullable[Seq[String]] and
      (__ \ "opId").readNullable[String]
    )(MongoMember.apply _)
}

// MongoScore
// id ->> owner + member
case class MongoScore(member: String, ranking: String, score: Int, confirmed: Boolean, opId: String)

object MongoScore {

  def build(member: String, ranking: String, score:Int, confirmed:Boolean, opId: String) = {
    MongoScore(member, ranking, score, confirmed, opId)
  }

  implicit val mongoScoreFormat: Format[MongoScore] = Json.format[MongoScore]
}


