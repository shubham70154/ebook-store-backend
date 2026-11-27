package com.ebookstore;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EbookStoreFunctionalTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void publicBookCatalogIsAccessibleAndSearchWorks() throws Exception {
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].active").value(true));

        mockMvc.perform(get("/api/books/search").param("keyword", "Java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Java Fundamentals"));

        mockMvc.perform(get("/api/books/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void registrationValidatesInputAndRejectsDuplicates() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"x\",\"email\":\"bad-email\",\"password\":\"1\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));

        registerCustomer("functional_customer", "functional@example.com", "password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"functional_customer\",\"email\":\"other@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Username is already registered"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"another_customer\",\"email\":\"functional@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email is already registered"));
    }

    @Test
    void customerCanRegisterAndLoginWithHttpBasic() throws Exception {
        registerCustomer("login_customer", "login@example.com", "password123");

        mockMvc.perform(post("/api/auth/login")
                        .with(httpBasic("login_customer", "password123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("login_customer"))
                .andExpect(jsonPath("$.roles[0]").value("CUSTOMER"));

        mockMvc.perform(post("/api/auth/login")
                        .with(httpBasic("login_customer", "wrong-password")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void cartEndpointsRequireCustomerRole() throws Exception {
        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/cart")
                        .with(httpBasic("admin", "Admin@123")))
                .andExpect(status().isForbidden());

        registerCustomer("cart_customer", "cart@example.com", "password123");
        mockMvc.perform(get("/api/cart")
                        .with(httpBasic("cart_customer", "password123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.totalQuantity").value(0));
    }

    @Test
    void adminCanCreateUpdateAndDeactivateBookButCustomerCannot() throws Exception {
        String createJson = """
                {"title":"JUnit in Practice","author":"Test Author","description":"A test book","price":499.50,"totalQuantity":7,"category":"Testing","coverImageUrl":"https://example.com/book.jpg"}
                """;

        registerCustomer("admin_create_customer", "admincreate@example.com", "password123");

        mockMvc.perform(post("/api/admin/books")
                        .with(httpBasic("admin_create_customer", "password123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isForbidden());

        MvcResult created = mockMvc.perform(post("/api/admin/books")
                        .with(httpBasic("admin", "Admin@123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("JUnit in Practice"))
                .andExpect(jsonPath("$.availableQuantity").value(7))
                .andReturn();

        JsonNode book = objectMapper.readTree(created.getResponse().getContentAsString());
        long bookId = book.get("id").asLong();

        String updateJson = """
                {"title":"JUnit in Practice Updated","author":"Test Author","description":"Updated description","price":599.00,"category":"Testing","coverImageUrl":"https://example.com/updated.jpg"}
                """;

        mockMvc.perform(put("/api/admin/books/{id}", bookId)
                        .with(httpBasic("admin", "Admin@123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("JUnit in Practice Updated"));

        mockMvc.perform(delete("/api/admin/books/{id}", bookId)
                        .with(httpBasic("admin", "Admin@123")))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/books/{id}", bookId))
                .andExpect(status().isNotFound());
    }

    @Test
    void customerCartSupportsAddUpdateRemoveAndClear() throws Exception {
        registerCustomer("cart_flow_customer", "cartflow@example.com", "password123");

        MvcResult booksResult = mockMvc.perform(get("/api/books/search").param("keyword", "Java"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode books = objectMapper.readTree(booksResult.getResponse().getContentAsString());
        long bookId = books.get(0).get("id").asLong();

        MvcResult addResult = mockMvc.perform(post("/api/cart/items")
                        .with(httpBasic("cart_flow_customer", "password123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\":" + bookId + ",\"quantity\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalQuantity").value(2))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andReturn();

        JsonNode cart = objectMapper.readTree(addResult.getResponse().getContentAsString());
        long itemId = cart.get("items").get(0).get("id").asLong();

        mockMvc.perform(put("/api/cart/items/{itemId}", itemId)
                        .with(httpBasic("cart_flow_customer", "password123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalQuantity").value(3));

        mockMvc.perform(delete("/api/cart/items/{itemId}", itemId)
                        .with(httpBasic("cart_flow_customer", "password123")))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/cart/items")
                        .with(httpBasic("cart_flow_customer", "password123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\":" + bookId + ",\"quantity\":1}"))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/cart")
                        .with(httpBasic("cart_flow_customer", "password123")))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/cart")
                        .with(httpBasic("cart_flow_customer", "password123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalQuantity").value(0));
    }

    @Test
    void cartRejectsInvalidQuantityAndUnknownBook() throws Exception {
        registerCustomer("invalid_cart_customer", "invalidcart@example.com", "password123");

        mockMvc.perform(post("/api/cart/items")
                        .with(httpBasic("invalid_cart_customer", "password123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\":1,\"quantity\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));

        mockMvc.perform(post("/api/cart/items")
                        .with(httpBasic("invalid_cart_customer", "password123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\":999999,\"quantity\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Book not found with id: 999999"));
    }

    @Test
    void checkoutIsIdempotentAndPaymentUpdatesOrderStatus() throws Exception {
        registerCustomer("checkout_customer", "checkout@example.com", "password123");

        MvcResult booksResult = mockMvc.perform(get("/api/books/search").param("keyword", "Java"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode books = objectMapper.readTree(booksResult.getResponse().getContentAsString());
        long bookId = books.get(0).get("id").asLong();

        mockMvc.perform(post("/api/cart/items")
                        .with(httpBasic("checkout_customer", "password123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\":" + bookId + ",\"quantity\":1}"))
                .andExpect(status().isOk());

        MvcResult firstCheckout = mockMvc.perform(post("/api/orders/checkout")
                        .with(httpBasic("checkout_customer", "password123"))
                        .header("Idempotency-Key", "checkout-key-1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"))
                .andExpect(jsonPath("$.items").isArray())
                .andReturn();

        JsonNode order = objectMapper.readTree(firstCheckout.getResponse().getContentAsString());
        long orderId = order.get("id").asLong();

        mockMvc.perform(post("/api/orders/checkout")
                        .with(httpBasic("checkout_customer", "password123"))
                        .header("Idempotency-Key", "checkout-key-1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(orderId));

        mockMvc.perform(post("/api/payments/{orderId}/process", orderId)
                        .with(httpBasic("checkout_customer", "password123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"paymentMethod\":\"CREDIT_CARD\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId))
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        mockMvc.perform(get("/api/orders/{orderId}", orderId)
                        .with(httpBasic("checkout_customer", "password123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));

        mockMvc.perform(get("/api/orders")
                        .with(httpBasic("checkout_customer", "password123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(orderId));
    }

    @Test
    void emptyCartCannotBeCheckedOutAndUnknownOrderCannotBePaid() throws Exception {
        registerCustomer("order_error_customer", "ordererror@example.com", "password123");

        mockMvc.perform(post("/api/orders/checkout")
                        .with(httpBasic("order_error_customer", "password123"))
                        .header("Idempotency-Key", "empty-cart-key"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Your cart is empty"));

        mockMvc.perform(post("/api/payments/999999/process")
                        .with(httpBasic("order_error_customer", "password123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"paymentMethod\":\"DEBIT_CARD\"}"))
                .andExpect(status().isConflict());
    }

    private void registerCustomer(String username, String email, String password) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"email\":\"" + email
                                + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(username));
    }
}
