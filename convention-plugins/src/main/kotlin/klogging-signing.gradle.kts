/*

   Copyright 2021-2023 Michael Strasser.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

*/

import gradle.kotlin.dsl.accessors._b6bea14fb88fd11e46d6fb1ebe601eab.ext
import gradle.kotlin.dsl.accessors._b6bea14fb88fd11e46d6fb1ebe601eab.publishing
import gradle.kotlin.dsl.accessors._b6bea14fb88fd11e46d6fb1ebe601eab.signing
import java.nio.file.Files
import java.util.Base64
import java.util.Properties

plugins {
    signing
}

// Stub secrets to let the project sync and build without the publication values set up
ext["signing.keyId"] = null
ext["signing.password"] = null
ext["signing.secretKeyRingFile"] = null
ext["ossrhUsername"] = null
ext["ossrhPassword"] = null

// Grabbing secrets from local.properties file or from environment variables, which could be used on CI
val secretPropsFile: File = project.rootProject.file("local.properties")
if (secretPropsFile.exists()) {
    secretPropsFile.reader().use {
        Properties().apply {
            load(it)
        }
    }.onEach { (name, value) ->
        ext[name.toString()] = value
    }
} else {
    ext["signing.keyId"] = System.getenv("SIGNING_KEY_ID")
    ext["signing.password"] = System.getenv("SIGNING_PASSWORD")
    ext["ossrhUsername"] = System.getenv("OSSRH_USERNAME")
    ext["ossrhPassword"] = System.getenv("OSSRH_PASSWORD")

    // Base64-encoded signing key must be in environment variable.
    System.getenv("SIGNING_KEY")?.let { keyRing ->
        val keyRingFilePath = Files.createTempFile("klogger-signing", ".gpg")
        keyRingFilePath.toFile().deleteOnExit()
        Files.write(keyRingFilePath, Base64.getDecoder().decode(keyRing))
        ext["signing.secretKeyRingFile"] = keyRingFilePath.toString()
    }
}

// Signing artifacts. Signing.* extra properties values will be used
signing {
    sign(publishing.publications)
}
