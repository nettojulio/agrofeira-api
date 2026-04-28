package br.edu.ufape.agrofeira.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.PathItem.HttpMethod
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springdoc.core.customizers.OpenApiCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {
    private val securitySchemeName = "bearerAuth"

    @Bean
    fun customOpenAPI(): OpenAPI =
        OpenAPI()
            .info(
                Info()
                    .title("Agro Feira API")
                    .version("1.0")
                    .description("API REST para o sistema de gestão agroecológica Agro Feira."),
            ).components(
                Components()
                    .addSecuritySchemes(
                        securitySchemeName,
                        SecurityScheme()
                            .name(securitySchemeName)
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                            .description("Insira o token JWT para autenticar."),
                    ),
            )

    @Bean
    fun openApiCustomizer(): OpenApiCustomizer =
        OpenApiCustomizer { openApi ->
            openApi.paths?.forEach { (path, pathItem) ->
                pathItem.readOperationsMap().forEach { (httpMethod, operation) ->
                    val isPublic = isPublicOperation(path, httpMethod)
                    if (!isPublic) {
                        operation.addSecurityItem(SecurityRequirement().addList(securitySchemeName))
                    }
                }
            }
        }

    private fun isPublicOperation(
        path: String,
        httpMethod: HttpMethod,
    ): Boolean {
        if (path.startsWith("/api/v1/auth")) return true
        if (path.startsWith("/actuator")) return true
        if (path.startsWith("/api/feiras") && httpMethod == HttpMethod.GET) return true
        return false
    }
}
