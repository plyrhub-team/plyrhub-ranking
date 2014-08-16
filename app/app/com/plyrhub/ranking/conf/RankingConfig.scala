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

package com.plyrhub.ranking.conf

import com.netflix.config.scala.DynamicIntProperty

object RankingConfig {

  object ModelConstraints {
    // Ranking
    val rnkColsMin = DynamicIntProperty("model.ranking.collections.min", 1).get
    val rnkColsMax = DynamicIntProperty("model.ranking.collections.max", 5).get

    val rnkPlatformsMin = DynamicIntProperty("model.ranking.platforms.min", 1).get
    val rnkPlatformsMax = DynamicIntProperty("model.ranking.platforms.max", 5).get

    val rnkPropertiesMin = DynamicIntProperty("model.ranking.properties.min", 0).get
    val rnkPropertiesMax = DynamicIntProperty("model.ranking.properties.max", 10).get

    // RankingCollection
    val rnkCollectionsMinLength = DynamicIntProperty("model.ranking.collection.min.length", 3).get
    val rnkCollectionsMaxLength = DynamicIntProperty("model.ranking.collection.max.length", 10).get

    // RankingName
    val rnkNameLangMinLength = DynamicIntProperty("model.rankingName.lang.min.lenght", 3).get
    val rnkNameLangMaxLength = DynamicIntProperty("model.rankingName.lang.max.lenght", 5).get

    val rnkNameShortNameMinLength = DynamicIntProperty("model.rankingName.shortname.min.lenght", 3).get
    val rnkNameShortNameMaxLength = DynamicIntProperty("model.rankingName.shortname.max.lenght", 5).get

    val rnkNameLongNameMinLength = DynamicIntProperty("model.rankingName.longname.min.lenght", 3).get
    val rnkNameLongNameMaxLength = DynamicIntProperty("model.rankingName.longname.max.lenght", 5).get

    // RankingPlatform
    val rnkPlatformIdMin = DynamicIntProperty("model.rankingPlatform.platform.min.length", 5).get
    val rnkPlatformIdMax = DynamicIntProperty("model.rankingPlatform.platform.max.length", 10).get

    val rnkPlatformNamesMin = DynamicIntProperty("model.rankingPlatform.names.min", 1).get
    val rnkPlatformNamesMax = DynamicIntProperty("model.rankingPlatform.names.max", 10).get
  }

}
