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
    def rnkIdMinLength = DynamicIntProperty("model.ranking.id.min.length",5).get
    def rnkIdMaxLength = DynamicIntProperty("model.ranking.id.max.length",20).get

    def rnkColsMin = DynamicIntProperty("model.ranking.collections.min", 1).get
    def rnkColsMax = DynamicIntProperty("model.ranking.collections.max", 5).get

    def rnkPlatformsMin = DynamicIntProperty("model.ranking.platforms.min", 1).get
    def rnkPlatformsMax = DynamicIntProperty("model.ranking.platforms.max", 5).get

    def rnkPropertiesMin = DynamicIntProperty("model.ranking.properties.min", 0).get
    def rnkPropertiesMax = DynamicIntProperty("model.ranking.properties.max", 10).get

    // RankingCollection
    def rnkCollectionsMinLength = DynamicIntProperty("model.ranking.collection.min.length", 3).get
    def rnkCollectionsMaxLength = DynamicIntProperty("model.ranking.collection.max.length", 10).get

    // RankingName
    def rnkNameLangMinLength = DynamicIntProperty("model.rankingName.lang.min.lenght", 3).get
    def rnkNameLangMaxLength = DynamicIntProperty("model.rankingName.lang.max.lenght", 5).get

    def rnkNameShortNameMinLength = DynamicIntProperty("model.rankingName.shortname.min.lenght", 3).get
    def rnkNameShortNameMaxLength = DynamicIntProperty("model.rankingName.shortname.max.lenght", 10).get

    def rnkNameLongNameMinLength = DynamicIntProperty("model.rankingName.longname.min.lenght", 5).get
    def rnkNameLongNameMaxLength = DynamicIntProperty("model.rankingName.longname.max.lenght", 20).get

    // RankingPlatform
    def rnkPlatformIdMin = DynamicIntProperty("model.rankingPlatform.platform.min.length", 5).get
    def rnkPlatformIdMax = DynamicIntProperty("model.rankingPlatform.platform.max.length", 10).get

    def rnkPlatformNamesMin = DynamicIntProperty("model.rankingPlatform.names.min", 1).get
    def rnkPlatformNamesMax = DynamicIntProperty("model.rankingPlatform.names.max", 10).get
  }

}
