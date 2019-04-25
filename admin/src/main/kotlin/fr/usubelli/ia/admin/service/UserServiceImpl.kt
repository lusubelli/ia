package fr.usubelli.ia.admin.service

import fr.usubelli.ia.admin.repository.CacheUser
import fr.usubelli.ia.admin.repository.UserRepository

class UserServiceImpl(private val userRepository: UserRepository) : UserService {

    override fun create(user: CacheUser): CacheUser {
        return userRepository.save(CacheUser(
            user.email,
            user.password
        ))
    }

    override fun update(email: String, user: CacheUser): CacheUser {
        return userRepository.save(CacheUser(
                email,
            user.password
        ))
    }

    override fun load(email: String): CacheUser? {
        return userRepository.load(email)
    }

    override fun remove(email: String): CacheUser? {
        return userRepository.remove(email)
    }

    override fun load(): List<CacheUser> {
        return userRepository.load()
    }

}
