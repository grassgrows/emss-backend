package top.warmthdawn.emss.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.http.auth.*
import org.koin.ktor.ext.inject
import top.warmthdawn.emss.config.AppConfig
import top.warmthdawn.emss.features.login.AuthProvider

fun Application.configureSecurity() {
    val cfg by inject<AppConfig>()
    install(Authentication) {
        jwt("auth-jwt") {
            realm = AuthProvider.realm
//            verifier(AuthProvider.jwkProvider, AuthProvider.jwkIssuer)
            verifier(
                JWT
                    .require(Algorithm.HMAC256(cfg.secretKeyConfig.authKey))
                    .withAudience(AuthProvider.audience)
                    .withIssuer(AuthProvider.issuer)
                    .build()
            )
            authHeader { call ->
                val result = try {
                    call.request.parseAuthorizationHeader()
                } catch (ex: IllegalArgumentException) {
                    null
                }
                if (result == null) {
                    val param = call.request.queryParameters["token"]
                    if (!param.isNullOrEmpty()) {
                        HttpAuthHeader.Single("Bearer", param)
                    } else {
                        null
                    }
                } else {
                    result
                }
            }
            validate {
                AuthProvider.validate(it)
            }
        }
    }
//    authentication {
//        jwt {
//            realm = jwtRealm
//            verifier(makeJwtVerifier(jwtIssuer, jwtAudience))
//            validate { credential ->
//                if (credential.payload.audience.contains(jwtAudience)) JWTPrincipal(credential.payload) else null
//            }
//        }
//    }

}

private val algorithm = Algorithm.HMAC256("secret")
private fun makeJwtVerifier(issuer: String, audience: String): JWTVerifier = JWT
    .require(algorithm)
    .withAudience(audience)
    .withIssuer(issuer)
    .build()
