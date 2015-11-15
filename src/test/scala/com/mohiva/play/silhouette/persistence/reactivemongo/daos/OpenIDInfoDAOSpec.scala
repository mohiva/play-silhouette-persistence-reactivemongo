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
import com.mohiva.play.silhouette.impl.providers.OpenIDInfo
import play.api.test.{ PlaySpecification, WithApplication }

/**
 * Test case for the [[OpenIDInfoDAO]] class.
 */
class OpenIDInfoDAOSpec extends PlaySpecification with MongoSpecification {

  "The `find` method" should {
    "find an OpenID info for the given login info" in new WithMongo with Context {
      val result = await(dao.find(loginInfo))

      result must beSome(openIDInfo)
    }

    "return None if no OpenID info for the given login info exists" in new WithMongo with Context {
      val result = await(dao.find(loginInfo.copy(providerKey = "new.key")))

      result should beNone
    }
  }

  "The `add` method" should {
    "add a new OpenID info" in new WithMongo with Context {
      await(dao.add(loginInfo.copy(providerKey = "new.key"), openIDInfo)) must be equalTo openIDInfo
      await(dao.find(loginInfo.copy(providerKey = "new.key"))) must beSome(openIDInfo)
    }

    "throws exception if a OpenID info already exists" in new WithMongo with Context {
      await(dao.add(loginInfo, openIDInfo)) must throwA[MongoException]
    }
  }

  "The `update` method" should {
    "update an existing OpenID info" in new WithMongo with Context {
      val updatedInfo = openIDInfo.copy(attributes = openIDInfo.attributes.updated("fullname", "updated"))

      await(dao.update(loginInfo, updatedInfo)) must be equalTo updatedInfo
      await(dao.find(loginInfo)) must beSome(updatedInfo)
    }

    "throw exception if no OpenID info for the given login info exists" in new WithMongo with Context {
      await(dao.update(loginInfo.copy(providerKey = "new.key"), openIDInfo)) must throwA[MongoException]
    }
  }

  "The `save` method" should {
    "insert a new OpenID info" in new WithMongo with Context {
      await(dao.save(loginInfo.copy(providerKey = "new.key"), openIDInfo)) must be equalTo openIDInfo
      await(dao.find(loginInfo.copy(providerKey = "new.key"))) must beSome(openIDInfo)
    }

    "update an existing OpenID info" in new WithMongo with Context {
      val updatedInfo = openIDInfo.copy(attributes = openIDInfo.attributes.updated("fullname", "updated"))

      await(dao.update(loginInfo, updatedInfo)) must be equalTo updatedInfo
      await(dao.find(loginInfo)) must beSome(updatedInfo)
    }
  }

  "The `remove` method" should {
    "remove an OpenID info" in new WithMongo with Context {
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
      "auth.info.openID" -> Seq("openID.info.json")
    )

    /**
     * The OpenID info DAO implementation.
     */
    lazy val dao = app.injector.instanceOf[OpenIDInfoDAO]

    /**
     * A login info.
     */
    lazy val loginInfo = LoginInfo("yahoo", "https://me.yahoo.com/a/Xs6hPjazdrMvmbn4jhQjkjkhcasdGdsKajq9we")

    /**
     * A OpenID info.
     */
    lazy val openIDInfo = OpenIDInfo("https://me.yahoo.com/a/Xs6hPjazdrMvmbn4jhQjkjkhcasdGdsKajq9we", Map(
      "fullname" -> "Apollonia Vanova",
      "email" -> "apollonia.vanova@watchmen.com",
      "image" -> "https://s.yimg.com/dh/ap/social/profile/profile_b48.png"
    ))
  }
}
