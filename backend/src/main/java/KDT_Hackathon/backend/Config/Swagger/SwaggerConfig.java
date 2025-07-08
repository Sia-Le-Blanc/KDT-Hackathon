package KDT_Hackathon.backend.Config.Swagger;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "NoJealousy Platform API",
                description = "서울 안 부러운, 협업 기반 공간 플랫폼 'NoJealousy'의 백엔드 API 문서입니다.",
                version = "1.0.0"
        )
)
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new io.swagger.v3.oas.models.info.Info().title("NoJealousy Platform API")
                        .version("1.0")
                        .description("NoJealousy Platform API 문서"));
    }
}
