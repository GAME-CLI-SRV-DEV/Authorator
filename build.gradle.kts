plugins {
    id 'java'
}

group 'org.duckdns.anarchyconnect.viaproxy'
version '1.0-SNAPSHOT'

repositories {
    mavenCentral()
    maven { url "https://repo.viaversion.com" }
}

dependencies {
    implementation "net.raphimc:ViaProxy:3.3.3" // Replace x.x.x with the latest version
}

sourceCompatibility = '1.8'
targetCompatibility = '1.8'

jar {
    manifest {
        attributes(
                'Main-Class': 'com.yourname.viaproxyplugin.MainClass' // Replace with your main class
        )
    }
}
