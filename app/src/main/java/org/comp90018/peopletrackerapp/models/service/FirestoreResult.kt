package org.comp90018.peopletrackerapp.models.service

sealed class FirestoreResult<out T> {
    data class OnSuccess<out T>(val data: T) : FirestoreResult<T>()
    data class OnError(val error: Throwable) : FirestoreResult<Nothing>()
}