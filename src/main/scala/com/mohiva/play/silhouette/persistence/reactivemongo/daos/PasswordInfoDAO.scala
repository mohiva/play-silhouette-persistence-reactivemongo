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
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import play.api.Configuration
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{ JsObject, Json }
import play.modules.reactivemongo.ReactiveMongoApi
import play.modules.reactivemongo.json._
import play.modules.reactivemongo.json.collection.JSONCollection

/**
 * Give access to the [[com.mohiva.play.silhouette.api.util.PasswordInfo]] object.
 *
 * @param reactiveMongoApi The ReactiveMongo API.
 * @param config The Play configuration.
 */
final class PasswordInfoDAO @Inject() (reactiveMongoApi: ReactiveMongoApi, config: Configuration)
  extends DelegableAuthInfoDAO[PasswordInfo] with MongoDAO {

  /**
   * The collection to use for JSON queries.
   */
  val jsonCollection = reactiveMongoApi.db[JSONCollection](
    config.getString("silhouette.persistence.reactivemongo.collection.auth.info.passwords").getOrElse("auth.info.passwords")
  )

  /**
   * Finds the auth info which is linked to the specified login info.
   *
   * @param loginInfo The linked login info.
   * @return The found auth info or None if no auth info could be found for the given login info.
   */
  def find(loginInfo: LoginInfo) = {
    jsonCollection.find(Json.obj("_id" -> loginInfo)).projection(Json.obj("_id" -> 0)).one[PasswordInfo]
  }

  /**
   * Adds new auth info for the given login info.
   *
   * @param loginInfo The login info for which the auth info should be added.
   * @param authInfo The auth info to add.
   * @return The added auth info.
   */
  def add(loginInfo: LoginInfo, authInfo: PasswordInfo) = {
    onSuccess(jsonCollection.insert(merge(loginInfo, authInfo)), authInfo)
  }

  /**
   * Updates the auth info for the given login info.
   *
   * @param loginInfo The login info for which the auth info should be updated.
   * @param authInfo The auth info to update.
   * @return The updated auth info.
   */
  def update(loginInfo: LoginInfo, authInfo: PasswordInfo) = {
    updated(jsonCollection.update(Json.obj("_id" -> loginInfo), merge(loginInfo, authInfo))).map {
      case num if num > 0 => authInfo
      case _ => throw new MongoException("Could not update password info for login info: " + loginInfo)
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
  def save(loginInfo: LoginInfo, authInfo: PasswordInfo) = {
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
   * Merges the [[LoginInfo]] and the [[PasswordInfo]] into one Json object.
   *
   * @param loginInfo The login info to merge.
   * @param authInfo The auth info to merge.
   * @return A Json object consisting of the [[LoginInfo]] and the [[PasswordInfo]].
   */
  private def merge(loginInfo: LoginInfo, authInfo: PasswordInfo) =
    Json.obj("_id" -> loginInfo).deepMerge(Json.toJson(authInfo).as[JsObject])
}
