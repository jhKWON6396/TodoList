package com.example.data

class MemoryRepository: Repository{
    private  val list = mutableListOf(
        Todo(1, "출근", false),
        Todo(2, "점심식사", false),
        Todo(3, "회의", false),
        Todo(4,"퇴근", false)
    )

    override fun getAllTodos(): List<Todo> {
        return list
    }

    override fun getTodo(id: Int): Todo? {
        return list.firstOrNull{ it.id == id }
    }

    override fun addTodo(todo: Todo): Todo {
        todo.id = list.size + 1
        list.add(todo)
        return todo
    }

    override fun deleteTodo(id: Int): Boolean {
        return list.removeIf{ it.id == id }
    }

    override fun updateTodo(id: Int, todo: Todo): Boolean {
        val findingTodo = list.firstOrNull{ it.id == id } ?: return false
        findingTodo.title = todo.title
        findingTodo.done = todo.done
        return true
    }
}