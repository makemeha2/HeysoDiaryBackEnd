package heyso.HeysoDiaryBackEnd.auth.service;

import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import lombok.Getter;

import heyso.HeysoDiaryBackEnd.user.model.User;

@Getter
@AllArgsConstructor
public class AuthenticatedToken {
    private final Claims claims;
    private final User user;
    private final String role;
    private final String scope;
}
