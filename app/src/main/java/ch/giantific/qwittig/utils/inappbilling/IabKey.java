package ch.giantific.qwittig.utils.inappbilling;

/**
 * Created by fabio on 25.06.15.
 */
public class IabKey {

    private IabKey() {
        // class cannot be instantiated
    }

    public static String getKey() {
        String part1 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA7ydfgCT7axK95wgIzkHaKEKhAE1bsS5OwRmcfLnGdB3/89iTqP659241TjpV5SLOKwzg85yczv2c8";
        String part2 = "gktIus/B2YvcvcmXDc4YDUhpw46meJb9U0hMWkzdYqrmAknWU3uMvZzTnRPusXBLMkjkAHIIbJeWOPzF/eVOxm5GRSI0IdK2EZx+3yGo/PK1uQMwHnT+cEgije5yM7Y1ZqQ";
        String part3 = "ogPWbNa9xwZrOMzL9VC66kvUewqs2k8CL8OiNtF6Mi8dmZc6GWT1Z3oxwyVu+VjYmK22g520w5TAANqshKRylO09ntw3blHYBWamVf2EAwJS1LKbMFHjn9eIArvaujcWz3poWQIDAQAB";

        return part1 + part2 + part3;
    }
}
