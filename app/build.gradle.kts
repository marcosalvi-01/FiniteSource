plugins {
	id("com.android.application")
	id("org.jetbrains.kotlin.android")
	id("com.google.devtools.ksp")
	id("com.google.dagger.hilt.android")
}

android {
	namespace = "com.example.finitesource"
	compileSdk = 34

	defaultConfig {
		applicationId = "com.example.finitesource"
		minSdk = 26
		targetSdk = 34
		versionCode = 1
		versionName = "1.0"

		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
	}
	buildTypes {
		release {
			isMinifyEnabled = false
			proguardFiles(
				getDefaultProguardFile("proguard-android-optimize.txt"),
				"proguard-rules.pro"
			)
		}
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_1_8
		targetCompatibility = JavaVersion.VERSION_1_8
	}
	kotlinOptions {
		jvmTarget = "1.8"
	}
	sourceSets {
		sourceSets {
			getByName("main").java.srcDirs("${rootDir}/build/generate-resources/main/src")
		}
	}
	tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
		dependsOn("openApiGenerate")
	}
}

dependencies {
	// room
	val roomVersion = "2.6.0"
	implementation("androidx.room:room-runtime:$roomVersion")
	implementation("androidx.room:room-ktx:$roomVersion")
	annotationProcessor("androidx.room:room-compiler:$roomVersion")
	ksp("androidx.room:room-compiler:$roomVersion")

	// hilt
	val hiltVersion = "2.48.1"
	implementation("com.google.dagger:hilt-android:$hiltVersion")
	ksp("com.google.dagger:hilt-android-compiler:$hiltVersion")

	// lifecycle
	val lifecycleVersion = "2.6.2"
	implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
	implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")

	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.10")
	implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.10")
	implementation("com.squareup.moshi:moshi-kotlin:1.15.0")
	implementation("com.squareup.moshi:moshi-adapters:1.15.0")
	implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
	implementation("com.squareup.retrofit2:retrofit:2.9.0")
	implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
	implementation("com.squareup.retrofit2:converter-scalars:2.9.0")
	testImplementation("io.kotlintest:kotlintest-runner-junit5:3.4.2")

	// other
	implementation("androidx.core:core-ktx:1.12.0")
	implementation("androidx.appcompat:appcompat:1.6.1")
	implementation("com.google.android.material:material:1.10.0")
	implementation("androidx.constraintlayout:constraintlayout:2.1.4")

	// testing
	testImplementation("junit:junit:4.13.2")
	androidTestImplementation("androidx.test.ext:junit:1.1.5")
	androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}