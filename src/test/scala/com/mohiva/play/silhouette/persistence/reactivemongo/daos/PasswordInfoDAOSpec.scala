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

import com.mohiva.play.silhouette.persistence.reactivemongo.{ MongoScope, MongoSpecification, WithMongo }
import com.mohiva.play.silhouette.persistence.reactivemongo.exceptions.MongoException
import com.mohiva.play.silhouette.persistence.MongoScope
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import play.api.test.{ PlaySpecification, WithApplication }

/**
 * Test case for the [[PasswordInfoDAO]] class.
 */
class PasswordInfoDAOSpec extends PlaySpecification with MongoSpecification {

  "The `find` method" should {
    "find a password info for the given login info" in new WithMongo with Context {
      val result = await(dao.find(loginInfo))

      result must beSome(passwordInfo)
    }

    "return None if no password info for the given login info exists" in new WithMongo with Context {
      val result = await(dao.find(loginInfo.copy(providerKey = "new.key")))

      result should beNone
    }
  }

  "The `add` method" should {
    "add a new password info" in new WithMongo with Context {
      await(dao.add(loginInfo.copy(providerKey = "new.key"), passwordInfo)) must be equalTo passwordInfo
      await(dao.find(loginInfo.copy(providerKey = "new.key"))) must beSome(passwordInfo)
    }

    "throws exception if a password info already exists" in new WithMongo with Context {
      await(dao.add(loginInfo, passwordInfo)) must throwA[MongoException]
    }
  }

  "The `update` method" should {
    "update an existing password info" in new WithMongo with Context {
      val updatedInfo = passwordInfo.copy(password = "updated")

      await(dao.update(loginInfo, updatedInfo)) must be equalTo updatedInfo
      await(dao.find(loginInfo)) must beSome(updatedInfo)
    }

    "throw exception if no password info for the given login info exists" in new WithMongo with Context {
      await(dao.update(loginInfo.copy(providerKey = "new.key"), passwordInfo)) must throwA[MongoException]
    }
  }

  "The `save` method" should {
    "insert a new password info" in new WithMongo with Context {
      await(dao.save(loginInfo.copy(providerKey = "new.key"), passwordInfo)) must be equalTo passwordInfo
      await(dao.find(loginInfo.copy(providerKey = "new.key"))) must beSome(passwordInfo)
    }

    "update an existing password info" in new WithMongo with Context {
      val updatedInfo = passwordInfo.copy(password = "updated")

      await(dao.update(loginInfo, updatedInfo)) must be equalTo updatedInfo
      await(dao.find(loginInfo)) must beSome(updatedInfo)
    }
  }

  "The `remove` method" should {
    "remove a password info" in new WithMongo with Context {
      await(dao.remove(loginInfo))
      await(dao.find(loginInfo)) must beNone
    }
  }

  /**
   * The context.
   */
  trait Context extends MongoScope {
    self: WithApplication =>

    /**
     * The test fixtures to insert.
     */
    override val fixtures = Map(
      "auth.info.passwords" -> Seq("password.info.json")
    )

    /**
     * The password info DAO implementation.
     */
    lazy val dao = app.injector.instanceOf[PasswordInfoDAO]

    /**
     * A login info.
     */
    lazy val loginInfo = LoginInfo(CredentialsProvider.ID, "akkie")

    /**
     * A password info.
     */
    lazy val passwordInfo = PasswordInfo(
      hasher = "bcrypt",
      password = "$2a$10$bCBXbqjTaEcxXcjwc.kCXe.sI1b8.bTgV25gTD71KM00XdVd5MG6K"
    )
  }
}
