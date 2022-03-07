# ktor-kodein-exposed
***
#### 해당 프로젝트는 `ktor`(Netty), `Exposed` ORM, `Kodein` 을 사용한다.

## Ktor Framework?
`Ktor Framework`는 주로 코틀린을 대상으로한 `Web Framework`입니다.
특히 라우팅을 하는 부분에서부터 `suspend` 함수로 작성되어 있어 쉽게 코루틴을 적용할 수 있는 환경이 마련되어 있고,
많은 함수들이 `inline function`으로 정의되어 있기 때문에 스프링보다 가벼운 개발이 가능하다.
또한 수식 객체 지정 람다와 같은 문법을 통해 읽기 좋은 코드 작성을 할 수 있다.

## 애플리케이션 구성
```
fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        routing {...}
    }.start(wait = true)
}
```
`Ktor`의 기본적인 애플리케이션 구성은 다음과 같다. 웹서버, 포트 번호, 호스트 주소를 설정할 수 있게 되어 있고 라우팅을 설정할 수 있도록 수신 객체 지정 람다를 통해 `Application` 객체의 메소드에 접근할 수 있다.
웹서버의 경우 코드를 통해 서버 파라미터 값을 구성하고 가장 빠르게 애플리케이션 구동을 할 수 있게 해주는 `embeddedServer`
```
fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module(testing: Boolean = false) {
    routing {...}
}
```
서버 구성을 더 유연하게 할 수 있게 스프링부트처럼 설정을 애플리케이션 시작 부분에서 담당하지 않고 라우팅과 관련된 설정도 모듈을 통해 따로 빼놓을 수도 있다.
이런 경우에는 애플리케이션에 대한 설정을 스프링부트에서 `appication.yml`에 설정했던 것처럼 `appication.conf` 파일에 설정 정보들을 저장하는 `EngineMain`
```
ktor {
    deployment {
        port = 8080
    }
    application {
        modules = [ com.example.ApplicationKt.module ]
    }
}
```

## 라우팅
```
fun Route.todoRoute(){

    val repository = MemoryRepository()

    get ("/todos"){
        call.respond(repository.getAllTodos())
    }

    post ("/todos"){
        val todoDraft = call.receive<Todo>()

        val todo = repository.addTodo(todoDraft)
        call.respond(todo)
    }
}
```
라우터를 설정할 때는 `Route`의 확장 함수로 정의하면 된다.
`Route`의 메소드에는 `get()`,`post()`와 같은 `HTTP Method`들이 존재하고 `path`와 `method`를 통해 `API Router`를 구현할 수 있다.

## Reauest Handling
`HTTP Method`로 이루어진 메소드는 `PipelineContext<Unit, ApplicationCall>.(R) -> Unit` 을 매개변수로 받는다.
여기에는 `call`이라는 변수가 존재하는데 이를 이용해 `Request`와 `Response`를 조작할 수 있다.

예를 들어 `call.receive<>()`를 통해 `Reqeest Body`를 가져올 수 있고, `call.parameter[...]`으로 `Path Variable`을 가져올 수 있다.

## Response 보내기
`Response`도 `Request`와 마찬가지로 `call` 변수를 통해 조작이 가능하다.
`Response Body` 없이 상태 코드만 전달하고 싶다면
```
call.respond(HttpStatusCode.Created)
```
위처럼 사용하면 되고 `Response Body`를 전달하고 싶다면 다음과 같이 사용하면 된다.
```
call.respond(
    status = HttpStatusCode.OK,
    message = Object(),
)
```

## 예외처리
```
fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {

        install(ContentNegotiation) {
            jackson()
        }
        install(CallLogging)
        install(StatusPages) {
            exception<CommonException> { e ->
                call.respond(
                    status = e.httpStatusCode,
                    message = CommonExceptionResponse(
                        error = CommonExceptionResponse.ExceptionAttribute(
                            code = e.errorCode,
                            message = e.errorMessage,
                        )
                    )
                )
            }
        }

        di {
            bindServices()
        }

        routing {
            apiRoute()
        }
    }.start(wait = true)
}
```
스프링에서는 예외처리를 위한 핸들러를 따로 구축해야 했지만 `ktor`에서는 애플리케이션을 설정하는 부분에서 이를 제어할 수 있다. `install()`구문에서 `StatusPages`를 가지는 구문이 예외처리를 담당하는 구문이다.
마찬가지로 `CallLogging`은 `Request`를 로깅하는 역할을 하며, `ContentNegotiation`은 `JSON`으로 변환할 때 어떤 컨버터를 사용할지를 선택한다.

## Dependenct Injection
```
fun DI.MainBuilder.bindServices() {
    bind<LibraryCreationService>() with singleton { LibraryCreationService(libraryDataAccessor) }
}
```
제어 흐름을 역전시키기 위해 중간 제어자(컨테이너)를 통한 `Dependency Injection`이 필요하다.
여기서는 `Kodein`이라는 `DI Framework`를 이용하여 `IOC`를 실현한다.
예를 들어 다음과 같이 의존성 주입을 받는 객체를 사용할 수 있다.
```
val todoService by closestDI().instance<TodoService>()
```

## Ktor 프로젝트 구조
```
src
  ㄴ main
      ㄴ kotlin.com.example
          ㄴ data
              ㄴ MemoryRepository
              ㄴ Repository
              ㄴ Todo
          ㄴ plugins    
              ㄴ HTTP.kt
              ㄴ Routing.kt
              ㄴ Serialization.kt
          ㄴ routes
              ㄴ TodoRoute.kt
          ㄴ Application.kt
      ㄴ resources
          ㄴ logback.xml
```

## Library & Framework

- Kotlin 1.6.10
- Ktor 2.0.0-beta-1
- Kodein
- Logback 1.2.3