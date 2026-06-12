package rcc.service;

import rcc.model.UserJson;

public interface UsersClient {

    UserJson createUser(String username, String password);
}
