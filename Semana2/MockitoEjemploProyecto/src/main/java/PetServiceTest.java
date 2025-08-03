import org.testng.annotations.Test;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;


public class PetServiceTest {

    @Test
    public void testDogSound() {
        PetRepository mockRepo = mock(PetRepository.class);
        when(mockRepo.findByType("dog")).thenReturn(new Pet("dog", "Woof"));

        PetService service = new PetService(mockRepo);
        assertEquals("Woof", service.getPetSound("dog"));
    }

    @Test
    public void testCatSound() {
        PetRepository mockRepo = mock(PetRepository.class);
        when(mockRepo.findByType("cat")).thenReturn(new Pet("cat", "Meow"));

        PetService service = new PetService(mockRepo);
        assertEquals("Meow", service.getPetSound("cat"));
    }

    @Test
    public void testUnknownPet() {
        PetRepository mockRepo = mock(PetRepository.class);
        when(mockRepo.findByType("parrot")).thenReturn(null);

        PetService service = new PetService(mockRepo);
        assertEquals("Unknown", service.getPetSound("parrot"));
    }
}

