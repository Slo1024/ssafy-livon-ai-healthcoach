package com.s406.livon.domain.user.enums;

public enum Role {
  COACH("COACH"),
  MEMBER("MEMBER");

  private final String roleName;

  Role(String roleName) {
    this.roleName = roleName;
  }

  public String getRoleName() {
    return roleName;
  }
}
