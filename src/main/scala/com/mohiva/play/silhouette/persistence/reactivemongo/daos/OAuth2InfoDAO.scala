/**
 * Copyright 2015 Mohiva Organisation (license at mohiva dot com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mohiva.play.silhouette.persistence.reactivemongo.daos

import javax.inject.Inject

import com.mohiva.play.silhouette.persistence.reactivemongo.exceptions.MongoException
import com.mohiva.play.silhouette.persistence.reactivemongo.util.json.Formats._
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.OAuth2Info
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import play.api.Configuration
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.{ JsObject, Json }
import play.modules.reactivemongo.ReactiveMongoApi
import play.modules.reactivemongo.json._
import play.modules.reactivemongo.json.collection.JSONCollection

/**
 * Give access to the [[com.mohiva.play.silhouette.impl.providers.OAuth2Info]] object.
 *
 * @param reactiveMongoApi The ReactiveMongo API.
 * @param config The Play configuration.
 */
final class OAuth2InfoDAO @Inject() (reactiveMongoApi: ReactiveMongoApi, config: Configuration)
  extends DelegableAuthInfoDAO[OAuth2Info] with MongoDAO {

  /**
   * The collection to use for JSON queries.
   */
  val jsonCollection = reactiveMongoApi.db[JSONCollection](
    config.getString("silhouette.persistence.reactivemongo.collection.auth.info.oauth2").getOrElse("auth.info.oauth2")
  )

  /**
   * Finds the auth info which is linked with the specified login info.
   *
   * @param loginInfo The linked login info.
   * @return The retrieved auth info or None if no auth info could be retrieved for the given login info.
   */
  def find(loginInfo: LoginInfo) = {
    jsonCollection.find(Json.obj("_id" -> loginInfo)).projection(Json.obj("_id" -> 0)).one[OAuth2Info]
  }

  /**
   * Adds new auth info for the given login info.
   *
   * @param loginInfo The login info for which the auth info should be added.
   * @param authInfo The auth info to add.
   * @return The added auth info.
   */
  def add(loginInfo: LoginInfo, authInfo: OAuth2Info) = {
    onSuccess(jsonCollection.insert(merge(loginInfo, authInfo)), authInfo)
  }

  /**
   * Updates the auth info for the given login info.
   *
   * @param loginInfo The login info for which the auth info should be updated.
   * @param authInfo The auth info to update.
   * @return The updated auth info.
   */
  def update(loginInfo: LoginInfo, authInfo: OAuth2Info) = {
    updated(jsonCollection.update(Json.obj("_id" -> loginInfo), merge(loginInfo, authInfo))).map {
      case num if num > 0 => authInfo
      case _ => throw new MongoException("Could not update OAuth2 info for login info: " + loginInfo)
    }
  }

  /**
   * Saves the auth info for the given login info.
   *
   * This method either adds the auth info if it doesn't exists or it updates the auth info
   * if it already exists.
   *
   * @param loginInfo The login info for which the auth info should be saved.
   * @param authInfo The auth info to save.
   * @return The saved auth info.
   */
  def save(loginInfo: LoginInfo, authInfo: OAuth2Info) = {
    onSuccess(jsonCollection.update(Json.obj("_id" -> loginInfo), merge(loginInfo, authInfo), upsert = true), authInfo)
  }

  /**
   * Removes the auth info for the given login info.
   *
   * @param loginInfo The login info for which the auth info should be removed.
   * @return A future to wait for the process to be completed.
   */
  def remove(loginInfo: LoginInfo) = onSuccess(jsonCollection.remove(Json.obj("_id" -> loginInfo)), ())

  /**
   * Merges the [[LoginInfo]] and the [[OAuth2Info]] into one Json object.
   *
   * @param loginInfo The login info to merge.
   * @param authInfo The auth info to merge.
   * @return A Json object consisting of the [[LoginInfo]] and the [[OAuth2Info]].
   */
  private def merge(loginInfo: LoginInfo, authInfo: OAuth2Info) =
    Json.obj("_id" -> loginInfo).deepMerge(Json.toJson(authInfo).as[JsObject])
}
