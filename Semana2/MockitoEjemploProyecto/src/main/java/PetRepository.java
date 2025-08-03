public interface PetRepository {
    Pet findByType(String type); //"dog", "cat", "turtle"
}
