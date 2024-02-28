package dev.magadiflo.swagger.app.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${magadiflo.openapi.dev-url}")
    private String devUrl;
    @Value("${magadiflo.openapi.prod-url}")
    private String prodUrl;

    @Bean
    public OpenAPI openApiBasicInformation() {
        Server devServer = new Server();
        devServer.setUrl(this.devUrl);
        devServer.setDescription("URL de servidor en entorno de desarrollo");

        Server prodServer = new Server();
        prodServer.setUrl(this.prodUrl);
        prodServer.setDescription("URL de servidor en entorno de producción");

        Contact contact = new Contact()
                .email("magadiflo@gmail.com")
                .name("Martín")
                .url("http://www.magadiflo.com");

        License license = new License()
                .name("Licencia MIT")
                .url("https://choosealicense.com/licenses/mit/");

        Info info = new Info()
                .title("Tutorial, documentando API")
                .version("1.0")
                .contact(contact)
                .description("Este API expone endpoints para administrar productos")
                .termsOfService("https://www.magadiflo.com/terms")
                .license(license);

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer, prodServer));
    }
}
