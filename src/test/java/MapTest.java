import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class MapTest {

    @Test
    void name2() {
        Map<User, Integer> bank = new HashMap<>();

        bank.put(new User("zz"), 100);
        bank.put(new User("zz"), 100);
        bank.put(null, 100);

        bank.forEach((k, v) -> System.out.println(k + " : " + v));
    }

    @Data
    @AllArgsConstructor
    private static class User {
        private String name;
    }
}
