/**
 * Copyright 2016 Mohiva Organisation (license at mohiva dot com)
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
package com.mohiva.play.silhouette.persistence.daos

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.OAuth1Info
import com.mohiva.play.silhouette.persistence.exceptions.MongoException
import com.mohiva.play.silhouette.persistence.{ MongoScope, MongoSpecification, WithMongo }
import play.api.Configuration
import play.api.libs.json.Json
import play.api.test.{ PlaySpecification, WithApplication }
import play.modules.reactivemongo.ReactiveMongoApi

/**
 * Test case for the [[MongoAuthInfoDAO]] class.
 */
class MongoAuthInfoDAOSpec extends PlaySpecification with MongoSpecification {

  "The `find` method" should {
    "find an auth info for the given login info" in new WithMongo with Context {
      val result = await(dao.find(loginInfo))

      result must beSome(oauth1Info)
    }

    "return None if no auth info for the given login info exists" in new WithMongo with Context {
      val result = await(dao.find(loginInfo.copy(providerKey = "new.key")))

      result should beNone
    }
  }

  "The `add` method" should {
    "add a new auth info" in new WithMongo with Context {
      await(dao.add(loginInfo.copy(providerKey = "new.key"), oauth1Info)) must be equalTo oauth1Info
      await(dao.find(loginInfo.copy(providerKey = "new.key"))) must beSome(oauth1Info)
    }

    "throws exception if an auth info already exists" in new WithMongo with Context {
      await(dao.add(loginInfo, oauth1Info)) must throwA[MongoException]
    }
  }

  "The `update` method" should {
    "update an existing auth info" in new WithMongo with Context {
      val updatedInfo = oauth1Info.copy(secret = "updated")

      await(dao.update(loginInfo, updatedInfo)) must be equalTo updatedInfo
      await(dao.find(loginInfo)) must beSome(updatedInfo)
    }

    "throw exception if no auth info for the given login info exists" in new WithMongo with Context {
      await(dao.update(loginInfo.copy(providerKey = "new.key"), oauth1Info)) must throwA[MongoException]
    }
  }

  "The `save` method" should {
    "insert a new auth info" in new WithMongo with Context {
      await(dao.save(loginInfo.copy(providerKey = "new.key"), oauth1Info)) must be equalTo oauth1Info
      await(dao.find(loginInfo.copy(providerKey = "new.key"))) must beSome(oauth1Info)
    }

    "update an existing auth info" in new WithMongo with Context {
      val updatedInfo = oauth1Info.copy(secret = "updated")

      await(dao.update(loginInfo, updatedInfo)) must be equalTo updatedInfo
      await(dao.find(loginInfo)) must beSome(updatedInfo)
    }
  }

  "The `remove` method" should {
    "remove an auth info" in new WithMongo with Context {
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
      "auth.OAuth1Info" -> Seq("oauth1.info.json")
    )

    implicit lazy val format = Json.format[OAuth1Info]

    /**
     * The auth info DAO implementation.
     */
    lazy val dao = new MongoAuthInfoDAO[OAuth1Info](
      app.injector.instanceOf[ReactiveMongoApi],
      app.injector.instanceOf[Configuration])

    /**
     * A login info.
     */
    lazy val loginInfo = LoginInfo("twitter", "6253282")

    /**
     * A OAuth1 info.
     */
    lazy val oauth1Info = OAuth1Info("my.token", "my.consumer.secret")
  }
}
