package learn.tripsplit.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import learn.tripsplit.data.GroupRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class GroupControllerTest {

    @MockBean
    GroupRepository repository;

    @Autowired
    MockMvc mvc;

    ObjectMapper jsonMapper = new ObjectMapper();

    @Test
    @WithMockUser(username = "bobj", roles = {"User"})
    void findByIdShouldReturn404WhenNotFound() throws Exception {
        when(repository.findById(9999)).thenReturn(null);

        mvc.perform(get("/api/group/9999"))
                .andExpect(status().isNotFound());
    }

}
