package com.example.routes

import com.example.data.MemoryRepository
import com.example.data.Todo
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun Route.todoRoute(){

    val repository = MemoryRepository()

    get ("/todos"){
        call.respond(repository.getAllTodos())
    }

    get("/todos/{id}"){
        val id = call.parameters["id"]?.toIntOrNull()
        if(id == null){
            call.respond(HttpStatusCode.BadRequest, "id값이 잘못 입력되었습니다.")
            return@get
        }

        val todo = repository.getTodo(id)
        if(todo == null){
            call.respond(HttpStatusCode.NotFound, "해당 id에 데이터가 없습니다.")
        }else{
            call.respond(todo)
        }
    }

    post ("/todos"){
        val todoDraft = call.receive<Todo>()

        val todo = repository.addTodo(todoDraft)
        call.respond(todo)
    }

    put ("/todos/{id}"){
        val todoId = call.parameters["id"]?.toIntOrNull()
        if(todoId == null){
            call.respond(HttpStatusCode.BadRequest, "id값이 유효하지 않습니다.")
            return@put
        }

        val todoDraft = call.receive<Todo>()
        val updated = repository.updateTodo(todoId, todoDraft)
        if(updated){
            call.respond(HttpStatusCode.OK)
        }else {
            call.respond(HttpStatusCode.NotFound, "$todoId id값을 찾지 못했습니다.")
        }
    }

    delete ("/todos/{id}"){
        val todoId = call.parameters["id"]?.toIntOrNull()
        if (todoId == null){
            call.respond(HttpStatusCode.BadRequest, "id값이 유효하지 않습니다.")
            return@delete
        }

        val deleted = repository.deleteTodo(todoId)
        if(deleted){
            call.respond(HttpStatusCode.OK)
        }else{
            call.respond(HttpStatusCode.NotFound, "$todoId id값을 찾지 못했습니다.")
        }
    }
}

