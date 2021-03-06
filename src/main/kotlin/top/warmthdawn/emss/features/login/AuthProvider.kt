package top.warmthdawn.emss.features.login

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import top.warmthdawn.emss.config.AppConfig
import java.util.*

/**
 * @author takanashi
 * @since 2021-07-19
 */

object AuthProvider{
    val issuer = "https://emss.warmthdawn.top"
    val audience = "emss-jwt-audience"
    val realm = "Eyjafjalla Minecraft Server Supervisor"

    val subject = "Authentication"

    val expiresIn: Long = 3600*1000*24

//    val jwkProvider = JwkProviderBuilder(issuer)
//        .cached(10, expiresIn, TimeUnit.MILLISECONDS)
//        .rateLimited(10, 1, TimeUnit.MINUTES)
//        .build()


    val map = mapOf("typ" to "JWT", "alg" to "HS256")


    // 签发证书
    fun sign(userId: Long,secretKey: String): Map<String, String> {
        return mapOf("token" to
                JWT.create()
                    .withHeader(map)
                    .withIssuer(issuer)
                    .withSubject(subject)
                    .withAudience(audience)
                    .withClaim("userId", userId)
                    .withIssuedAt(Date())
                    .withExpiresAt(Date(System.currentTimeMillis() + expiresIn))
                    .sign(Algorithm.HMAC256(secretKey)))
    }

    fun validate(credential: JWTCredential): Principal
    {
        if (credential.payload.issuer.equals(AuthProvider.issuer)
            && credential.payload.subject.equals(AuthProvider.subject)
            && credential.payload.audience.contains(AuthProvider.audience)
            && credential.payload.expiresAt.after(Date(System.currentTimeMillis()))
        ) {
            return JWTPrincipal(credential.payload)
        } else {
            throw JWTException(JWTExceptionMsg.CREDENTIAL_WRONG)
        }
    }

}

//val JWTPrincipal.userId get() = this.payload.getClaim("userId")