package com.example.leafy.data

class UserRepository(private val userDao: UserDao) {

    suspend fun registerUser(name: String, email: String, password: String): Boolean {
        val existing = userDao.getUserByEmail(email)
        return if (existing == null) {
            userDao.insertUser(UserEntity(name = name, email = email, password = password))
            true
        } else false
    }

    suspend fun loginUser(email: String, password: String): UserEntity? {
        val user = userDao.getUserByEmail(email)
        return if (user?.password == password) user else null
    }
}
