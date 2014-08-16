/*
 * Copyright (C) 2014  Enrique Aguilar Esnaola
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.File

import com.netflix.config.{DynamicPropertyFactory, ConfigurationManager}
import com.plyrhub.api.utils.HttpResults._
import com.plyrhub.core.log.Loggable
import com.plyrhub.ranking.conf.Starter
import play.api.Mode.Mode
import play.api.mvc.RequestHeader
import play.api.{Application, Configuration, GlobalSettings}

import scala.concurrent.Future

object Global extends GlobalSettings with Loggable {

  override def onStart(app: Application) = {

    // warm-up application
    log.debug("Warming up app ...")
    Starter.warmup()

    val r = ConfigurationManager.isConfigurationInstalled

    //ConfigurationManager.loadPropertiesFromResources("config.properties")

    val r1 = ConfigurationManager.isConfigurationInstalled

    val uno = DynamicPropertyFactory.getInstance().getStringProperty("archaius.first", "")


    // do the default behaviour
    super.onStart(app)
  }

  override def onLoadConfig(config: Configuration, path: File, classloader: ClassLoader, mode: Mode) = {
    super.onLoadConfig(config, path, classloader, mode)
  }

  override def onBadRequest(request: RequestHeader, error: String) = {

/*
    At this time Play only return the first error found in the queryString even though it evaluates all of them.
    In https://github.com/playframework/playframework/blob/master/framework/src/play/src/main/scala/play/core/router/Router.scala:
      - The Routes.call(...)
              (for (a1 <- pa1.value.right; a2 <- pa2.value.right)
                  yield (a1, a2))
                .fold(badRequest, { case (a1, a2) => generator(a1, a2) })

    Has a1,a2,ax evaluated, however only pases the "first failure" due to the us of "fold"
*/
    Future.successful(API_GLOBAL_ERROR(Seq(error)))

  }


  override def onHandlerNotFound(request: RequestHeader) = {

    // TODO: provide error for not-found request
    // Log the request
    super.onHandlerNotFound(request)
  }

  override def onError(request: RequestHeader, ex: Throwable) = {

    // TODO: provide error for non-expected situations
    // Log the request and the exception
    super.onError(request, ex)
  }


}
