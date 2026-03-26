plugins {
    `java-library`
    `maven-publish`
    signing
    id("org.jreleaser") version "1.22.0"
}

// Project metadata from gradle.properties
group = property("group") as String
version = property("version") as String

val projectName: String by project
val projectDescription: String by project
val projectUrl: String by project
val projectScmUrl: String by project
val licenseName: String by project
val licenseUrl: String by project
val developerName: String by project
val developerEmail: String by project
val developerOrganization: String by project
val developerOrganizationUrl: String by project

val sonatypeUsername: String by project
val sonatypePassword: String by project

// Dependency versions
val jspecifyVersion: String by project
val vertxVersion: String by project
val keelCoreVersion: String by project
val keelTestVersion: String by project

repositories {
    // Internal Nexus repository for SNAPSHOT and internal dependencies (optional)
    val nexusUrl = findProperty("internalNexusPublicUrl") as String?
    if (nexusUrl != null) {
        maven {
            name = "InternalNexus"
            url = uri(nexusUrl)
            credentials {
                username = findProperty("internalNexusUsername") as String?
                password = findProperty("internalNexusPassword") as String?
            }
        }
    }

    mavenCentral()
}

dependencies {
    // Core dependency (provided scope)
    api("io.github.sinri:keel-core:$keelCoreVersion")

    // Vert.x MySQL client
    api("io.vertx:vertx-mysql-client:$vertxVersion")

    // API dependency (transitive)
    // https://mvnrepository.com/artifact/org.jspecify/jspecify
    compileOnly("org.jspecify:jspecify:${jspecifyVersion}")
    testCompileOnly("org.jspecify:jspecify:${jspecifyVersion}")

    // Test dependencies (from pom.xml)
    testImplementation("io.github.sinri:keel-test:$keelTestVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
    withSourcesJar()
    withJavadocJar()
}

tasks.compileJava {
    options.encoding = "UTF-8"
    options.release.set(17)
    // Gradle will automatically compile Java modules
}

tasks.compileTestJava {
    options.encoding = "UTF-8"
    options.release.set(17)
}

// Configure resources (exclude config.properties like Maven)
tasks.processResources {
    exclude("config.properties")
}

// Configure test task (matching Maven surefire configuration)
tasks.test {
    useJUnitPlatform()
    include("io/github/sinri/keel/**/*Test.class")
}

// Configure JavaDoc (matching Maven javadoc plugin configuration)
tasks.javadoc {
    options.encoding = "UTF-8"
    if (options is StandardJavadocDocletOptions) {
        val stdOptions = options as StandardJavadocDocletOptions
        stdOptions.charSet = "UTF-8"
        stdOptions.docEncoding = "UTF-8"
        stdOptions.memberLevel = JavadocMemberLevel.PROTECTED
        stdOptions.docTitle = "$projectName $version Document"
        stdOptions.windowTitle = "$projectName $version Document"
        stdOptions.addBooleanOption("html5", true)
        stdOptions.addStringOption("Xdoclint:-missing", "-quiet") // 提示缺失的注释
    }
}

// Publishing configuration
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            pom {
                name.set(projectName)
                description.set(projectDescription)
                url.set(projectUrl)

                licenses {
                    license {
                        name.set(licenseName)
                        url.set(licenseUrl)
                    }
                }

                developers {
                    developer {
                        name.set(developerName)
                        email.set(developerEmail)
                        organization.set(developerOrganization)
                        organizationUrl.set(developerOrganizationUrl)
                    }
                }

                scm {
                    url.set(projectScmUrl)
                }
            }
        }
    }

    repositories {
        maven {
            val versionStr = version.toString()
            val nexusUsername = findProperty("internalNexusUsername") as String?
            val nexusPassword = findProperty("internalNexusPassword") as String?

            if (versionStr.endsWith("SNAPSHOT")) {
                val snapshotsUrl = findProperty("internalNexusSnapshotsUrl") as String?
                    ?: error("Publishing SNAPSHOT requires 'internalNexusSnapshotsUrl' in gradle.properties")
                url = uri(snapshotsUrl)
                credentials {
                    username = nexusUsername
                    password = nexusPassword
                }
            } else if (versionStr.contains(Regex("-[A-Za-z]+"))) {
                val releasesUrl = findProperty("internalNexusReleasesUrl") as String?
                    ?: error("Publishing pre-release requires 'internalNexusReleasesUrl' in gradle.properties")
                url = uri(releasesUrl)
                credentials {
                    username = nexusUsername
                    password = nexusPassword
                }
            } else {
                url = uri(layout.buildDirectory.dir("staging-deploy"))
            }
        }
    }
}

// 在 publishing 配置块之后添加
tasks.named("publish") {
    // 仅当版本是正式版本时，自动触发 jreleaserDeploy
    if (!version.toString().endsWith("SNAPSHOT") &&
        !version.toString().contains(Regex("-[A-Za-z]+"))
    ) {
        doFirst {
            logger.lifecycle(">>> Publishing release version $version")
            logger.lifecycle(">>> Will automatically deploy to Maven Central after staging")
        }
        finalizedBy("jreleaserDeploy")
    }
}

// Signing configuration
signing {
    // Use GnuPG command for signing (configured in gradle.properties)
    useGpgCmd()

    // Only sign if not a SNAPSHOT and signing credentials are available
    setRequired({
        !version.toString().endsWith("SNAPSHOT") && gradle.taskGraph.hasTask("publish")
    })
    sign(publishing.publications["mavenJava"])
}

// JReleaser 配置
jreleaser {
    signing {
        pgp {
            active.set(org.jreleaser.model.Active.ALWAYS)
            armored.set(true)
        }
    }
    deploy {
        maven {
            mavenCentral {
                active = org.jreleaser.model.Active.RELEASE
                register("sonatype") { // "sonatype" 为自定义名称
                    active.set(org.jreleaser.model.Active.ALWAYS)
                    // 如果使用新的 Central Portal (https://central.sonatype.com)
                    url.set("https://central.sonatype.com/api/v1/publisher")
                    // 指定制品暂存目录，JReleaser 会从这里读取 POM 和 JAR
                    stagingRepository("build/staging-deploy")

                    // 认证信息通常通过环境变量提供，或在这里显式设置
                    username.set(sonatypeUsername)
                    password.set(sonatypePassword)

                    enabled.set(true)
                }
            }
        }
    }
}