import com.vanniktech.maven.publish.SonatypeHost

plugins {
	java
	id("com.vanniktech.maven.publish") version "0.31.0"
}

group = "io.github.oct24th"
version = "4.1.4"

java {
	sourceCompatibility = JavaVersion.VERSION_17
//	withJavadocJar()
	withSourcesJar()
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	compileOnly("org.mybatis:mybatis:3.4.6")
	compileOnly("org.springframework:spring-web:6.2.3")
	compileOnly("org.springframework:spring-webmvc:6.2.3")
	compileOnly("org.springframework:spring-jdbc:6.2.3")
	compileOnly("org.slf4j:slf4j-api:1.7.36")
	compileOnly("org.mybatis:mybatis-spring:2.1.2")
	implementation("org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.4")

    // Spring Web과 호환되는 최신 Jackson 라이브러리
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")

    // Java 8 날짜/시간(LocalDateTime 등)을 지원하려면 추가 권장
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.18.2")

	compileOnly("org.projectlombok:lombok:1.18.26")

	annotationProcessor("org.projectlombok:lombok:1.18.26")
}

tasks.withType<Javadoc> {
	val options = options as StandardJavadocDocletOptions;
	options.locale = "ko_KR"
	options.encoding = "UTF-8"
	options.charSet = "UTF-8"
	options.docEncoding = "UTF-8"
}

tasks.register<Jar>("javadocJar") {
	archiveClassifier.set("javadoc")
	from(tasks.javadoc)
}

mavenPublishing {
	coordinates( // Coordinate(GAV)
		groupId = "io.github.oct24th",
		artifactId = "batisty",
		version = "4.1.4"
	)

	pom {
		name.set("batisty") // Project Name
		description.set("Mybatis Common DAO based on POJO Entity.") // Project Description
		inceptionYear.set("2025") // 개시년도
		url.set("https://github.com/oct24th/batisty") // Project URL

		licenses { // License Information
			license {
				name.set("The Apache License, Version 2.0")
				url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
			}
		}

		developers { // Developer Information
			developer {
				id.set("oct24th")
				name.set("oct24th")
				email.set("oct24th@daum.net")
			}
		}

		scm { // SCM Information
			connection.set("scm:git:git://github.com/oct24th/batisty.git")
			developerConnection.set("scm:git:ssh://github.com/oct24th/batisty.git")
			url.set("https://github.com/oct24th/batisty.git")
		}
	}

	publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

	signAllPublications() // GPG/PGP 서명
}
