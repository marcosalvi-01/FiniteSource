plugins {
	id("com.android.application")
	id("org.jetbrains.kotlin.android")
	id("com.google.devtools.ksp")
	id("com.google.dagger.hilt.android")
	id("org.openapi.generator")
	id("kotlin-parcelize")
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
		sourceCompatibility = JavaVersion.VERSION_11
		targetCompatibility = JavaVersion.VERSION_11
	}
	kotlinOptions {
		jvmTarget = "11"
	}
	sourceSets {
		getByName("main").java.srcDirs("${buildDir}/generate-resources/main/src")
	}
	buildFeatures {
		viewBinding = true
	}

}

dependencies {
	// room
	val roomVersion = "2.6.1"
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
	implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")

	// OpenAPI generator
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.10")
	implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.10")
	implementation("com.squareup.moshi:moshi-kotlin:1.15.0")
	implementation("com.squareup.moshi:moshi-adapters:1.15.0")
	ksp("com.squareup.moshi:moshi-kotlin-codegen:1.15.0")
	implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
	implementation("com.squareup.retrofit2:retrofit:2.9.0")
	implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
	implementation("com.squareup.retrofit2:converter-scalars:2.9.0")
	testImplementation("io.kotlintest:kotlintest-runner-junit5:3.4.2")

	// osmdroid
//	implementation("com.github.MKergall:osmbonuspack:6.9.0")
//	implementation("org.osmdroid:osmdroid-android:6.1.17")

	// other
	implementation("androidx.core:core-ktx:1.12.0")
	implementation("androidx.appcompat:appcompat:1.6.1")
	implementation("com.google.android.material:material:1.11.0")
	implementation("androidx.constraintlayout:constraintlayout:2.1.4")
	implementation("androidx.activity:activity-ktx:1.8.2")
	implementation("androidx.fragment:fragment-ktx:1.6.2")
	implementation("androidx.datastore:datastore-preferences:1.0.0")
	implementation("androidx.datastore:datastore:1.0.0")
	implementation("com.davemorrissey.labs:subsampling-scale-image-view-androidx:3.10.0")
	implementation("com.google.android.flexbox:flexbox:3.0.0")
	implementation("com.github.bumptech.glide:glide:4.15.1")
	annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")
	implementation("org.osmdroid:osmdroid-android:6.1.17")
	implementation("com.opencsv:opencsv:5.5.2")


	// testing
	testImplementation("io.mockk:mockk:1.13.8")
	testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
	testImplementation("androidx.test.ext:junit-ktx:1.1.5")
	testImplementation("androidx.test:core-ktx:1.5.0")
	testImplementation("org.robolectric:robolectric:4.11.1")
	testImplementation("junit:junit:4.13.2")
	testImplementation("androidx.arch.core:core-testing:2.2.0")

	androidTestImplementation("androidx.test.ext:junit:1.1.5")
	androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
	androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
}


openApiGenerate {
	inputSpec.set("$rootDir/openapi/finite_source_api.yaml")
	generatorName.set("kotlin")
	library.set("jvm-retrofit2")
}

tasks.preBuild {
	dependsOn("openApiGenerate")
}