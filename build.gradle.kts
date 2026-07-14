import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.PosixFilePermissions
import java.security.MessageDigest
import java.util.HexFormat
import java.util.Properties
import org.gradle.api.file.RelativePath
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.bundling.Zip

plugins {
    id("java")
    id("application")
    id("com.gradleup.shadow") version "8.3.6" //외부 라이브러리까지 싹다 가지고옴
}

group = "io.dbflow"
val dbflowVersion = providers.gradleProperty("dbflowVersion").orNull
    ?: error("gradle.properties에 dbflowVersion을 설정해야 합니다.")
version = dbflowVersion

application {
    mainClass.set("io.dbflow.DBFlowApplication")
}

repositories {
    mavenCentral()
}

dependencies {

    implementation("org.mybatis:mybatis:3.5.16")
    implementation("org.xerial:sqlite-jdbc:3.50.3.0")
    implementation("info.picocli:picocli:4.7.7")
    implementation("org.postgresql:postgresql:42.7.7")
    implementation("com.github.freva:ascii-table:1.12.1")

    annotationProcessor("info.picocli:picocli-codegen:4.7.7")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
    systemProperty(
        "dbflow.userDataDirectory",
        layout.buildDirectory.dir("dbflow-test").get().asFile.absolutePath
    )
}

tasks.processResources {
    inputs.property("dbflowVersion", dbflowVersion)
    filesMatching("META-INF/dbflow-version.properties") {
        expand("dbflowVersion" to dbflowVersion)
    }
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
    systemProperty("dbflow.sqlLog", "true")
}

tasks.register("downloadJavaRuntime") {
    group = "distribution"
    description = "고정된 Zulu Java 17 macOS ARM64 Runtime ZIP을 다운로드하고 SHA-256을 검증합니다."

    doLast {
        val runtimeDirectory = layout.projectDirectory.dir("packaging/runtime").asFile.toPath()
        val propertiesPath = runtimeDirectory.resolve("runtime.properties")
        val runtimeProperties = Properties().apply {
            Files.newInputStream(propertiesPath).use(::load)
        }

        val archiveName = runtimeProperties.getProperty("runtime.archive")
            ?: error("runtime.archive 설정이 없습니다.")
        val downloadUrl = runtimeProperties.getProperty("runtime.download.url")
            ?: error("runtime.download.url 설정이 없습니다.")
        val expectedChecksum = runtimeProperties.getProperty("runtime.sha256")
            ?.lowercase()
            ?: error("runtime.sha256 설정이 없습니다.")
        val archivePath = runtimeDirectory.resolve(archiveName)
        val temporaryPath = runtimeDirectory.resolve("$archiveName.part")

        fun sha256(path: Path): String {
            val digest = MessageDigest.getInstance("SHA-256")
            Files.newInputStream(path).use { inputStream ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                while (true) {
                    val readCount = inputStream.read(buffer)
                    if (readCount < 0) break
                    digest.update(buffer, 0, readCount)
                }
            }
            return HexFormat.of().formatHex(digest.digest())
        }

        Files.createDirectories(runtimeDirectory)

        if (Files.exists(archivePath)) {
            val actualChecksum = sha256(archivePath)
            if (actualChecksum != expectedChecksum) {
                error(
                    "기존 Java Runtime ZIP의 SHA-256이 일치하지 않습니다: $archivePath\n" +
                            "기대값: $expectedChecksum\n실제값: $actualChecksum"
                )
            }
            logger.lifecycle("검증된 Java Runtime ZIP이 이미 존재합니다: $archivePath")
            return@doLast
        }

        logger.lifecycle("Zulu Java Runtime을 다운로드합니다: $downloadUrl")
        Files.deleteIfExists(temporaryPath)

        try {
            val connection = URI(downloadUrl).toURL().openConnection().apply {
                connectTimeout = 30_000
                readTimeout = 120_000
            }
            connection.getInputStream().use { inputStream ->
                Files.copy(inputStream, temporaryPath)
            }

            val actualChecksum = sha256(temporaryPath)
            if (actualChecksum != expectedChecksum) {
                error(
                    "다운로드한 Java Runtime ZIP의 SHA-256이 일치하지 않습니다.\n" +
                            "기대값: $expectedChecksum\n실제값: $actualChecksum"
                )
            }

            Files.move(
                temporaryPath,
                archivePath,
                StandardCopyOption.ATOMIC_MOVE
            )
            logger.lifecycle("Java Runtime ZIP 다운로드가 완료되었습니다: $archivePath")
        } finally {
            Files.deleteIfExists(temporaryPath)
        }
    }
}

tasks.shadowJar {
    archiveFileName.set("dbflow-${project.version}-SNAPSHOT.jar")
    manifest {
        attributes["Main-Class"] = "io.dbflow.DBFlowApplication"
    }
    exclude("docs/**")
}

val runtimePropertiesFile = layout.projectDirectory.file("packaging/runtime/runtime.properties")
val distributionStagingDirectory = layout.buildDirectory.dir("distribution/dbflow")

val prepareDbFlowDistribution by tasks.registering(Sync::class) {
    group = "distribution"
    description = "JAR, 설치 스크립트와 Zulu Runtime을 DBFlow 배포 구조로 조립합니다."
    dependsOn(tasks.shadowJar)

    val runtimeProperties = Properties().apply {
        runtimePropertiesFile.asFile.inputStream().use(::load)
    }
    val runtimeArchiveName = runtimeProperties.getProperty("runtime.archive")
        ?: error("runtime.archive 설정이 없습니다.")
    val expectedRuntimeChecksum = runtimeProperties.getProperty("runtime.sha256")
        ?.lowercase()
        ?: error("runtime.sha256 설정이 없습니다.")
    val runtimeArchive = layout.projectDirectory.file("packaging/runtime/$runtimeArchiveName")

    doFirst {
        val stagingPath = distributionStagingDirectory.get().asFile.toPath()
        if (Files.exists(stagingPath)) {
            Files.walk(stagingPath).use { paths ->
                paths.filter(Files::isDirectory).forEach { directory ->
                    try {
                        Files.setPosixFilePermissions(
                            directory,
                            PosixFilePermissions.fromString("rwx------")
                        )
                    } catch (_: UnsupportedOperationException) {
                        // POSIX 권한을 지원하지 않는 환경에서는 Gradle 기본 삭제를 사용한다.
                    }
                }
            }
            delete(stagingPath)
        }

        val archivePath = runtimeArchive.asFile.toPath()
        if (!Files.isRegularFile(archivePath)) {
            error(
                "Zulu Java Runtime ZIP이 없습니다: $archivePath\n" +
                        "먼저 ./gradlew downloadJavaRuntime 명령을 실행하세요."
            )
        }

        val digest = MessageDigest.getInstance("SHA-256")
        Files.newInputStream(archivePath).use { inputStream ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val readCount = inputStream.read(buffer)
                if (readCount < 0) break
                digest.update(buffer, 0, readCount)
            }
        }
        val actualChecksum = HexFormat.of().formatHex(digest.digest())
        if (actualChecksum != expectedRuntimeChecksum) {
            error(
                "Zulu Java Runtime ZIP의 SHA-256이 일치하지 않습니다.\n" +
                        "기대값: $expectedRuntimeChecksum\n실제값: $actualChecksum"
            )
        }
    }

    from("packaging/install.sh") {
        filePermissions { unix("rwxr-xr-x") }
    }
    from("packaging/bin/dbf") {
        into("bin")
        filePermissions { unix("rwxr-xr-x") }
    }
    from(tasks.shadowJar.flatMap { it.archiveFile }) {
        into("lib")
    }
    from(zipTree(runtimeArchive)) {
        include("**/Contents/**")
        includeEmptyDirs = false
        filePermissions { unix("rw-r--r--") }
        dirPermissions { unix("rwxr-xr-x") }
        eachFile {
            val contentsIndex = relativePath.segments.indexOf("Contents")
            if (contentsIndex >= 0) {
                relativePath = RelativePath(
                    relativePath.isFile,
                    *(
                            listOf("runtime", "java17") +
                                    relativePath.segments.drop(contentsIndex)
                            ).toTypedArray()
                )
            }
        }
    }
    into(distributionStagingDirectory)

    doLast {
        val runtimeBinDirectory = distributionStagingDirectory.get()
                .dir("runtime/java17/Contents/Home/bin")
                .asFile
                .toPath()
        if (Files.isDirectory(runtimeBinDirectory)) {
            Files.list(runtimeBinDirectory).use { paths ->
                paths.filter(Files::isRegularFile).forEach { executable ->
                    try {
                        Files.setPosixFilePermissions(
                            executable,
                            PosixFilePermissions.fromString("rwxr-xr-x")
                        )
                    } catch (_: UnsupportedOperationException) {
                        executable.toFile().setExecutable(true, false)
                    }
                }
            }
        }
    }
}

tasks.register<Zip>("packageDbFlow") {
    group = "distribution"
    description = "DBFlow macOS ARM64 배포 ZIP을 생성합니다."
    dependsOn(tasks.test, prepareDbFlowDistribution)

    archiveFileName.set("dbflow-${project.version}-macos-arm64.zip")
    destinationDirectory.set(layout.buildDirectory.dir("distributions"))
    from(distributionStagingDirectory) {
        into("dbflow")
    }
}
