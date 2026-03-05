package domain.shared

trait Generator[T] {
    def generate: T
}
