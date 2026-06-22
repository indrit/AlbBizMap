// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.utils

import com.google.firebase.auth.FirebaseAuth

object AuthGate {

    fun isLoggedIn(): Boolean {
        return FirebaseAuth.getInstance().currentUser != null
    }

    /**
     * Runs [action] only if the user is logged in.
     * Otherwise calls [onNotLoggedIn] (typically navigates to AuthScreen).
     */
    fun requireLogin(onNotLoggedIn: () -> Unit, action: () -> Unit) {
        if (isLoggedIn()) {
            action()
        } else {
            onNotLoggedIn()
        }
    }
}