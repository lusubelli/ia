package fr.usubelli.ia.admin.service

import fr.usubelli.ia.admin.Auth


interface AuthService {

    fun login(auth: Auth): User?

}


data class User(val email: String)