package dev.mobase.core.usecase

internal abstract class UseCase<T, R> {
    suspend operator fun invoke(param: T): Result<R> {
        return runCatching {
            execute(param)
        }
    }

    abstract suspend fun execute(param: T): R
}