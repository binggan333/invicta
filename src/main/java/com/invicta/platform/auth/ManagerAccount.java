package com.invicta.platform.auth;

import java.util.Set;

public record ManagerAccount(long id, String name, String email, int managerType, long orgId, Set<String> roles) {}
