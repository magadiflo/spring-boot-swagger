# [Spring Boot 3 + Swagger 3 example (with OpenAPI 3)](https://www.bezkoder.com/spring-boot-swagger-3/)

- Tutorial tomado de la página web de **BezKoder**.
- Sitio web de la [OpenAPI Specification](https://swagger.io/specification/)
- Sitio web del documento de [OpenAPI Specification v3.1.0](https://spec.openapis.org/oas/latest.html)
- Sitio web de [springdoc-openapi v2.3.0](https://springdoc.org/)

---

## Dependencias iniciales

````xml
<!--Spring Boot 3.2.3-->
<!--Java 21-->
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
````

## Configuración inicial

````yaml
server:
  port: 8080
  error:
    include-message: always

spring:
  application:
    name: spring-boot-swagger

  datasource:
    url: jdbc:mysql://localhost:3306/db_spring_data_jpa
    username: admin
    password: magadiflo

  jpa:
    hibernate:
      ddl-auto: create-drop
    properties:
      hibernate:
        format_sql: true

logging:
  level:
    org.hibernate.SQL: DEBUG
````

# Proyecto sin Swagger

A continuación se muestra, a modo de documentación rápida, el crud realizado en este proyecto, por ahora sin el uso
de **Swagger**:

## Entidad

````java

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private Integer quantityAvailable;
    private Double price;
    private Boolean available;
    private LocalDate creationDate;
}
````

## Repositorio

````java
public interface IProductRepository extends JpaRepository<Product, Long> {
}
````

## Capa de servicio

````java
public interface IProductService {
    List<Product> findAllProducts();

    Product findProductById(Long id);

    Product saveProduct(Product product);

    Product updateProduct(Long id, Product product);

    void deleteProductById(Long id);
}
````

````java

@RequiredArgsConstructor
@Slf4j
@Service
public class ProductServiceImpl implements IProductService {

    private final IProductRepository productRepository;

    @Override
    public List<Product> findAllProducts() {
        return this.productRepository.findAll();
    }

    @Override
    public Product findProductById(Long id) {
        return this.productRepository.findById(id)
                .orElseThrow(() -> new ApiException("No existe el producto con el id: " + id, HttpStatus.NOT_FOUND));
    }

    @Override
    public Product saveProduct(Product product) {
        return this.productRepository.save(product);
    }

    @Override
    public Product updateProduct(Long id, Product product) {
        return this.productRepository.findById(id)
                .map(productDB -> {
                    productDB.setName(product.getName());
                    productDB.setQuantityAvailable(product.getQuantityAvailable());
                    productDB.setPrice(product.getPrice());
                    productDB.setAvailable(product.getAvailable());
                    productDB.setCreationDate(product.getCreationDate());
                    return productDB;
                })
                .map(this.productRepository::save)
                .orElseThrow(() -> new ApiException("No existe el producto para actualizar con el id: " + id, HttpStatus.NOT_FOUND));
    }

    @Override
    public void deleteProductById(Long id) {
        this.productRepository.findById(id)
                .map(productDB -> {
                    this.productRepository.deleteById(productDB.getId());
                    return true;
                })
                .orElseThrow(() -> new ApiException("No existe el producto para eliminar con el id: " + id, HttpStatus.NOT_FOUND));
    }
}
````

## Capa Web

````java
public record ResponseMessage<T>(String message,
                                 @JsonInclude(JsonInclude.Include.NON_NULL) T content) {
}
````

````java

@RestControllerAdvice
public class ApiAdvice {
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ResponseMessage<Void>> apiException(ApiException e) {
        ResponseMessage<Void> response = new ResponseMessage<>(e.getMessage(), null);
        return ResponseEntity.status(e.getHttpStatus()).body(response);
    }
}
````

````java

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping(path = "/api/v1/products")
public class ProductRestController {

    private final IProductService productService;

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(this.productService.findAllProducts());
    }

    @GetMapping(path = "/{productId}")
    public ResponseEntity<Product> getProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(this.productService.findProductById(productId));
    }

    @PostMapping
    public ResponseEntity<Product> saveProduct(@RequestBody Product product) {
        Product productDB = this.productService.saveProduct(product);
        URI uri = URI.create("/api/v1/products/" + productDB.getId());
        return ResponseEntity.created(uri).body(productDB);
    }

    @PutMapping(path = "/{productId}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long productId, @RequestBody Product product) {
        return ResponseEntity.ok(this.productService.updateProduct(productId, product));
    }

    @DeleteMapping(path = "/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long productId) {
        this.productService.deleteProductById(productId);
        return ResponseEntity.noContent().build();
    }
}
````

## Exception

````java

@Getter
public class ApiException extends RuntimeException {

    private final HttpStatus httpStatus;

    public ApiException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }
}
````

---

# Trabajando con Swagger

---

## [OpenAPI Specification v3.1.0](https://spec.openapis.org/oas/latest.html)

La `especificación OpenAPI (OAS)` define una descripción de interfaz estándar independiente del lenguaje de programación
para las API HTTP, que permite que tanto humanos como computadoras descubran y comprendan las capacidades de un servicio
sin requerir acceso al código fuente, documentación adicional o inspección del tráfico de la red. Cuando se define
correctamente a través de `OpenAPI`, un consumidor puede comprender e interactuar con el servicio remoto con una
cantidad mínima de lógica de implementación.

Luego, las herramientas de generación de documentación para mostrar la API, las herramientas de generación de código
para generar servidores y clientes en varios lenguajes de programación, las herramientas de prueba y muchos otros casos
de uso
`pueden utilizar una definición de OpenAPI`. [Fuente: swagger.io/specification](https://swagger.io/specification/)

Con la `versión 3.1.0`, la especificación OpenAPI establece un conjunto de pautas para el desarrollo y la documentación
de API, que abarca control de versiones, esquema, estructura de documentos y otros elementos críticos, lo que contribuye
a crear APIs confiables y consistentes.

## [Swagger 3](https://swagger.io/docs/specification/about/)

El `OpenAPI specification` define la especificación estándar de la industria para **diseñar API REST**, mientras que
`Swagger` proporciona una **variedad de herramientas (Swagger Editor, Swagger UI, Swagger Codegen, etc.)**
para respaldar el desarrollo, las pruebas y la documentación de estas API.

> Entonces podemos pensar en `Swagger 3` como una `implementación` de la `OpenAPI 3 Specification`.

## Swagger 3 vs OpenAPI 3

- `Todas las herramientas Swagger`, compatibles con el software **SmartBear**, utilizan la `especificación OpenAPI`.
- Pero no todas las herramientas `OpenAPI` son herramientas `Swagger`. Hay muchas herramientas profesionales y de código
  abierto que no están relacionadas con Swagger y son compatibles con la `especificación OpenAPI 3`.

## [springdoc-openapi v2.3.0](https://springdoc.org/)

`Springdoc-openapi` es una librería que se integra con el `framework Spring Boot` para **generar automáticamente
documentación OpenAPI para APIs REST.** Permite a los desarrolladores describir sus endpoints y modelos de API
mediante anotaciones y genera una especificación OpenAPI en formato `JSON/YAML y HTML`.

También es **compatible con varias funciones de la especificación OpenAPI 3**, como las definiciones de seguridad, la
validación de esquemas y la autenticación JSON Web Token (JWT).

Además, **se integra con otras bibliotecas de Spring Boot, como Spring WebMvc/WebFlux, Spring Data Rest,
Spring Security y Spring Cloud Function Web**, para generar también automáticamente documentación para estos
componentes.

Esta librería admite:

- OpenAPI 3
- Spring Boot 3
- JSR-303, específicamente para @NotNull, @Min, @Max y @Size.
- Swagger-ui
- OAuth 2
- GraalVM native images

**IMPORTANTE**
> `springdoc-openapi v1.7.0` es la última versión de código abierto que admite `Spring Boot 2.x y 1.x`.
> Ya está disponible un soporte ampliado para el proyecto `springdoc-openapi v1` para organizaciones que necesiten
> soporte más allá de 2023.

## Getting Started

Para la integración entre `spring-boot y swagger-ui`, agregue la biblioteca a la lista de dependencias de su proyecto
`(no se necesita configuración adicional)`.

`Para Spring Boot 3`

````xml

<dependencies>
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        <version>2.3.0</version>
    </dependency>
</dependencies>
````

`Para Spring Boot 2`

````xml

<dependencies>
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-ui</artifactId>
        <version>1.7.0</version>
    </dependency>
</dependencies>
````

Esto implementará automáticamente `swagger-ui` en una aplicación de `Spring Boot`:

- La documentación estará disponible en formato HTML, utilizando los archivos oficiales swagger-ui.
- La página de la interfaz de usuario de Swagger estará disponible en:<br>
  `http://server:port/context-path/swagger-ui.html`
- La descripción de OpenAPI estará disponible en la siguiente URL en formato json:<br>
  `http://server:port/context-path/v3/api-docs`.
    - server: El nombre del servidor o IP.
    - port: El puerto del servidor.
    - context-path: la ruta de contexto de la aplicación.
- La documentación **también puede estar disponible en formato yaml**, en la siguiente ruta: `/v3/api-docs.yaml`

Para obtener una ruta personalizada de la documentación de swagger en formato HTML, agregue una propiedad springdoc
personalizada en su archivo de configuración spring-boot:

````properties
# swagger-ui custom path
springdoc.swagger-ui.path=/swagger-ui.html
````

## Ejecutando proyecto y comprobando

Sin haber realizado ninguna configuración, tan solo agregando la dependencia de `swagger` mostrada en el párrafo
superior, ejecutamos el proyecto `Spring Boot`, abrimos el navegador e ingresamos la siguiente dirección:

````bash
$ http://localhost:8080/swagger-ui.html
````

Al dar enter, seremos redireccionados a la siguiente dirección (podríamos haber ingresado directamente a la url de
abajo, pero lo hice para ver de dónde es que sale):

````bash
$ http://localhost:8080/swagger-ui/index.html
````

En la imagen siguiente observamos la interfaz gráfica de `Swagger-ui`:

![01.swagger-ui.png](./assets/01.swagger-ui.png)

Si ingresamos a la siguiente dirección veremos el documento en formato json:

````bash
$ http://localhost:8080/v3/api-docs
````

![api-docs-json](./assets/02.api-docs-json.png)

El json completo sería el siguiente:

````json
{
  "openapi": "3.0.1",
  "info": {
    "title": "OpenAPI definition",
    "version": "v0"
  },
  "servers": [
    {
      "url": "http://localhost:8080",
      "description": "Generated server url"
    }
  ],
  "paths": {
    "/api/v1/products/{productId}": {
      "get": {
        "tags": [
          "product-rest-controller"
        ],
        "operationId": "getProduct",
        "parameters": [
          {
            "name": "productId",
            "in": "path",
            "required": true,
            "schema": {
              "type": "integer",
              "format": "int64"
            }
          }
        ],
        "responses": {
          "200": {
            "description": "OK",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/Product"
                }
              }
            }
          }
        }
      },
      "put": {
        "tags": [
          "product-rest-controller"
        ],
        "operationId": "updateProduct",
        "parameters": [
          {
            "name": "productId",
            "in": "path",
            "required": true,
            "schema": {
              "type": "integer",
              "format": "int64"
            }
          }
        ],
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "$ref": "#/components/schemas/Product"
              }
            }
          },
          "required": true
        },
        "responses": {
          "200": {
            "description": "OK",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/Product"
                }
              }
            }
          }
        }
      },
      "delete": {
        "tags": [
          "product-rest-controller"
        ],
        "operationId": "deleteProduct",
        "parameters": [
          {
            "name": "productId",
            "in": "path",
            "required": true,
            "schema": {
              "type": "integer",
              "format": "int64"
            }
          }
        ],
        "responses": {
          "200": {
            "description": "OK"
          }
        }
      }
    },
    "/api/v1/products": {
      "get": {
        "tags": [
          "product-rest-controller"
        ],
        "operationId": "getAllProducts",
        "responses": {
          "200": {
            "description": "OK",
            "content": {
              "*/*": {
                "schema": {
                  "type": "array",
                  "items": {
                    "$ref": "#/components/schemas/Product"
                  }
                }
              }
            }
          }
        }
      },
      "post": {
        "tags": [
          "product-rest-controller"
        ],
        "operationId": "saveProduct",
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "$ref": "#/components/schemas/Product"
              }
            }
          },
          "required": true
        },
        "responses": {
          "200": {
            "description": "OK",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/Product"
                }
              }
            }
          }
        }
      }
    }
  },
  "components": {
    "schemas": {
      "Product": {
        "type": "object",
        "properties": {
          "id": {
            "type": "integer",
            "format": "int64"
          },
          "name": {
            "type": "string"
          },
          "quantityAvailable": {
            "type": "integer",
            "format": "int32"
          },
          "price": {
            "type": "number",
            "format": "double"
          },
          "available": {
            "type": "boolean"
          },
          "creationDate": {
            "type": "string",
            "format": "date"
          }
        }
      }
    }
  }
}
````

## Personalización de configuraciones

- Para obtener una ruta personalizada de la documentación de swagger en formato HTML, agregue una propiedad springdoc
  personalizada en su archivo de configuración spring-boot:

  ````properties
  # swagger-ui custom path
  springdoc.swagger-ui.path=/custom-path-swagger-ui
  
  # openApi custom path
  springdoc.api-docs.path=/custom-api-docs
  ````

  En el ejemplo anterior definimos el path `/custom-path-swagger-ui` para ver el html de swagger, es decir, para acceder
  mediante el navegador a la interfaz gráfica de swagger ya no usaremos el path por defecto `/swagger-ui.html`, sino más
  bien usaremos el que acabamos de definir en el `.properties` o `.yml`. **NOTA**. Usando el nuevo path o el que
  viene por defecto, siempre seremos redirigidos al path `http://localhost:8080/swagger-ui/index.html`.


- Para deshabilitar el endpoint `springdoc-openapi` (`/v3/api-docs` de forma predeterminada), utilice la siguiente
  propiedad:

  ````properties
  # Disabling the /v3/api-docs endpoint
  springdoc.api-docs.enabled=false
  ````
  Ahora, si tratamos de ingresar a `http://localhost:8080/v3/api-docs`, obtendremos una página de error.


- Para deshabilitar swagger-ui, use la siguiente propiedad:

  ````properties
  # Disabling the swagger-ui
  springdoc.swagger-ui.enabled=false
  ````
  Ahora, si tratamos de ingresar a `http://localhost:8080/swagger-ui.html`, obtendremos una página de error y si
  tratamos de ingresar directamente a `http://localhost:8080/swagger-ui/index.html` obtendremos una página en blanco.


- Para que se incluya la lista de paquetes, utilice la siguiente propiedad:
  ````properties
  # Packages to include
  springdoc.packagesToScan=com.package1, com.package2
  ````
  **NOTA.** Solo serán visibles en el html los endpoints de los `packages` que coincidan, **los demás no se mostrarán.**


- Para incluir la lista de rutas, utilice la siguiente propiedad:
  ````properties
  # Paths to include
  springdoc.pathsToMatch=/v1, /api/balance/**
  ````
  **NOTA.** Solo serán visibles en el html los endpoints de los `Paths` que coincidan, **los demás no se mostrarán.**

## Descripción de la API de Spring Boot y Swagger

Antes de empezar a documentar la API, puedes definir primero la descripción de la API con su información básica, que
incluye la URL base (entorno de desarrollo y producción), título, versión, contacto del autor, descripción, licencia...

Ahora define la clase `OpenAPIConfig` con el bean `OpenAPI` como el siguiente código:

````java

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
````

En el `application.yml` definimos los servidores a usar en desarrollo y producción:

````yaml
magadiflo:
  openapi:
    dev-url: http://localhost:8080
    prod-url: http://magadiflo-api.com
````

Si ejecutamos la aplicación veremos toda la información configurada en el ui de swagger:

![Open api basic information](./assets/03.open-api-basic-information.png)

**NOTA.** Esa misma información debería verse si accedemos a la url `/v3/api-docs` pero en formato `json`.

## Configuraciones swagger para documentar endpoints

Para realizar una descripción del API para Rest Controller o cada solicitud HTTP, continuamos con las anotaciones
Swagger.

De forma predeterminada, el nombre del grupo de endpoints es `product-rest-controller`. Podemos cambiarlo a `Products`
(con descripción) usando la anotación `@Tag`.

Simplemente actualice `ProductRestController.java`:

````java
import io.swagger.v3.oas.annotations.tags.Tag;
/* other imports */

@Tag(name = "Products", description = "API de gestión de productos")
@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping(path = "/api/v1/products")
public class ProductRestController {
    /* code */
}
````

Al ejecutar la aplicación y acceder al swagger ui veremos reflejada la configuración:

![Endpoint config](./assets/04.endpoint-config.png)

## Ejemplo de response de Spring Boot y Swagger

Si abre un endpoint API, verá una estructura del `request` y `response` como esta:

![05.structure-request-response.png](./assets/05.structure-request-response.png)

Usaremos anotaciones Swagger 3 para personalizar la descripción con más detalles.

````java

@Tag(name = "Products", description = "API de gestión de productos")
@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping(path = "/api/v1/products")
public class ProductRestController {

    private final IProductService productService;

    @Operation(tags = {"reading"})
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        /* code */
    }

    @Operation(
            summary = "Recupera un producto por su id",
            description = "Obtiene un objeto de Product especificando su id. " +
                    "La respuesta es un objeto Product con id, name, quantityAvailability " +
                    "price, availability y creationDate.",
            tags = {"reading"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = Product.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "404", description = "El product con el id dado no fue encontrado", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "500", content = {@Content(schema = @Schema())}),
    })
    @GetMapping(path = "/{productId}")
    public ResponseEntity<Product> getProduct(@PathVariable Long productId) {
        /* code */
    }

    @Operation(tags = {"modification"})
    @PostMapping
    public ResponseEntity<Product> saveProduct(@RequestBody Product product) {
        /* code */
    }

    @Operation(tags = {"modification"})
    @PutMapping(path = "/{productId}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long productId, @RequestBody Product product) {
        /* code */
    }

    @Operation(tags = {"modification"})
    @DeleteMapping(path = "/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long productId) {
        /* code */
    }
}
````

Verificamos nuevamente la interfaz de swagger y vemos más información.

![endpoint config](./assets/06.endpoint-config.png)

## Operaciones de agrupación con etiquetas

Puede asignar una lista de etiquetas a cada operación de API. Las herramientas y bibliotecas pueden manejar las
operaciones etiquetadas de manera diferente. Por ejemplo, Swagger UI utiliza etiquetas para agrupar las operaciones
mostradas.

En el código anterior, vimos que usamos las siguientes configuraciones en los distintos endpoints:

- `@Operation(tags = {"modification"})`
- `@Operation(tags = {"reading"})`

Esa configuración nos permite agrupar los endpoints, tal como se ve a continuación:

![group tags](./assets/07.group-tags.png)

Para obtener más detalles y practicar sobre las anotaciones Swagger 3, visite:
[Anotaciones Swagger 3 en Spring Boot](https://www.bezkoder.com/swagger-3-annotations/)

## Mostrando solo los tags reading y modification

Si observamos la imagen anterior, vemos que estamos mostrando tres grupos: `Products`, `reading` y `modification`. El
tag `Products` está mostrando todos los endpoints de nuestro rest controller, **¿cómo podemos mostrar únicamente los
tags reading y modification?**

La respuesta es muy sencilla, dejamos todas las configuraciones como están y únicamente eliminamos la siguiente
configuración, en mi caso, solo lo voy a comentar:

````java
//@Tag(name = "Products", description = "API de gestión de productos") //<-- Eliminar este código
@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping(path = "/api/v1/products")
public class ProductRestController {
    /* code */
}
````

Como observamos, el `@Tag` anterior está anotado a nivel de clase, es por eso que todos los endpoints del
controlador se agrupan con esa etiqueta `Products` y además, como cada endpoint tienen dentro de la anotación
`@Operation` etiquetado con `modification` y `reading`, ahora solo esos grupos se mostrarán:

![08.tag-endpoints.png](./assets/08.tag-endpoints.png)