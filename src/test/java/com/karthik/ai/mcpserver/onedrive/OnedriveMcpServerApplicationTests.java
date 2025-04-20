package com.karthik.ai.mcpserver.onedrive;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "MICROSOFT_ONEDRIVE_ACCESS_TOKEN=test-token"
})
class OnedriveMcpServerApplicationTests {

    @Test
    void contextLoads() {
    }

}
