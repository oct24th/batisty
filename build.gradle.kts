plugins {
	java
}

group = "com.github.oct24th"
version = "1.0.0"

java {
	sourceCompatibility = JavaVersion.VERSION_11
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
	compileOnly("org.springframework:spring-web:5.3.27")
	compileOnly("org.springframework:spring-webmvc:5.3.27")
	compileOnly("org.slf4j:slf4j-api:1.7.36")
	compileOnly("org.mybatis:mybatis-spring:2.1.2")

	compileOnly("org.projectlombok:lombok:1.18.26")

	annotationProcessor("org.projectlombok:lombok:1.18.26")
}

java {
	withJavadocJar()
}

tasks.withType<Javadoc> {
	val options = options as StandardJavadocDocletOptions;
	options.locale = "ko_KR"
	options.encoding = "UTF-8"
	options.charSet = "UTF-8"
	options.docEncoding = "UTF-8"
}

tasks.register<Jar>("sourcesJar") {
	archiveClassifier.set("sources")
	from(sourceSets.main.get().allSource)
}

artifacts {
	add("archives", tasks["sourcesJar"])
}
