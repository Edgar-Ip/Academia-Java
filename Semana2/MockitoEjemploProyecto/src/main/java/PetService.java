public class PetService {
    private final PetRepository petRepository;

    public PetService(PetRepository petRepository){
        this.petRepository = petRepository;
    }

    public String getPetSound(String type){
        Pet pet = petRepository.findByType(type);
        return  pet != null ? pet.getSound() : "Uknown";
    }
}
