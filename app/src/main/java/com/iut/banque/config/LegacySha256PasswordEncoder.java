package com.iut.banque.config;

import java.util.Objects;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.iut.banque.cryptage.PasswordHasher;

public class LegacySha256PasswordEncoder implements PasswordEncoder {

  @Override
  public String encode(CharSequence rawPassword) {
    return PasswordHasher.hashPassword(Objects.toString(rawPassword, ""));
  }

  @Override
  public boolean matches(CharSequence rawPassword, String encodedPassword) {
    if (encodedPassword == null)
      return false;
    String rawHash = PasswordHasher.hashPassword(Objects.toString(rawPassword, ""));
    return encodedPassword.equals(rawHash);
  }
}
