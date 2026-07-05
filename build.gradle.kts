plugins {
    id("java")
    id("application")
    id("com.gradleup.shadow") version "8.3.6" //외부 라이브러리까지 싹다 가지고옴
}

group = "io.dbflow"
version = "1.0-SNAPSHOT"

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
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}

tasks.shadowJar {
    archiveFileName.set("dbflow.jar")
    destinationDirectory.set(
        file("../DBFlowTest/lib")
    )
    manifest {
        attributes["Main-Class"] = "io.dbflow.DBFlowApplication"
    }
    exclude("docs/**")
}