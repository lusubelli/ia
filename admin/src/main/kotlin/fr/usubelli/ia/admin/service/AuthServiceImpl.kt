package fr.usubelli.ia.admin.service

import fr.usubelli.ia.admin.Auth
import fr.usubelli.ia.admin.repository.UserRepository


class AuthServiceImpl(private val userRepository: UserRepository) : AuthService {

    override fun login(auth: Auth): User? {
        val userDao = userRepository.load(auth.email)
        return if (userDao != null && auth.password == userDao.password) {
            User(userDao.email)
        } else null
    }

}
