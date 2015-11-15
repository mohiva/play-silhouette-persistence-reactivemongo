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

import com.mohiva.play.silhouette.persistence.reactivemongo.exceptions.MongoException
import play.api.libs.concurrent.Execution.Implicits._
import reactivemongo.api.commands.WriteResult

import scala.concurrent.Future

/**
 * Base Mongo DAO.
 */
trait MongoDAO {

  /**
   * Returns some result on success and None on error.
   *
   * @param result The last result.
   * @param entity The entity to return.
   * @tparam T The type of the entity.
   * @return The entity on success or an exception on error.
   */
  def onSuccess[T](result: Future[WriteResult], entity: T): Future[T] = result.recoverWith {
    case e => Future.failed(new MongoException("Got exception from MongoDB", e.getCause))
  }.map { r =>
    r.inError match {
      case true => throw new MongoException(r.message, r.getCause)
      case false => entity
    }
  }

  /**
   * Returns the number of updated documents on success and None on error.
   *
   * @param result The last result.
   * @return The number of updated documents on success or an exception on error.
   */
  def updated(result: Future[WriteResult]): Future[Int] = result.recoverWith {
    case e => Future.failed(new MongoException("Got exception from MongoDB", e.getCause))
  }.map { r =>
    r.inError match {
      case true => throw new MongoException(r.message, r.getCause)
      case false => r.n
    }
  }
}
