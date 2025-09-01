//usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 21
//DEPS dev.data-star.kotlin:kotlin-sdk:1.0.0-RC1
//DEPS io.quarkus.platform:quarkus-bom:3.26.1@pom
//DEPS io.quarkus:quarkus-rest
//DEPS io.quarkus:quarkus-kotlin
//JAVAC_OPTIONS -parameters
//JAVA_OPTIONS -Djava.util.logging.manager=org.jboss.logmanager.LogManager

import io.quarkus.runtime.Quarkus
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType

class CounterApp {

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/")
    fun index(name: String): String {
        return "hello $name"
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            Quarkus.run(*args)
        }
    }
}