package com.invicta.model;

import java.util.Set;

public record OrganizationAccount(long orgId, String name, String email, int orgType, boolean profileCompleted,
                                  Set<String> roles) {}
