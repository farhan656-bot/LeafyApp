package com.example.leafy.data

class UserRepository(private val userDao: UserDao) {

    suspend fun registerUser(email: String, password: String): Boolean {
        val existing = userDao.getUserByEmail(email)
        return if (existing == null) {
            userDao.insertUser(UserEntity(email = email, password = password))
            true
        } else false
    }

    suspend fun loginUser(email: String, password: String): Boolean {
        val user = userDao.getUserByEmail(email)
        return user?.password == password
    }
}
